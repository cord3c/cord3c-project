package io.cord3c.rest.server.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.cord3c.rest.client.FlowExecutionDTO;
import io.cord3c.rest.client.FlowExecutionRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.flows.FlowLogic;
import net.corda.core.messaging.DataFeed;
import net.corda.core.messaging.FlowHandle;
import net.corda.core.node.AppServiceHub;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.Try;
import net.corda.node.internal.AbstractNode;
import net.corda.node.services.statemachine.StateMachineManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class FlowExecutionRepositoryImpl extends ResourceRepositoryBase<FlowExecutionDTO, UUID>
		implements FlowExecutionRepository {

	private static final Duration GC_INTERVAL = Duration.ofMillis(1);

	private static final Duration KEEP_ENDED_FLOW_INTERVAL = Duration.ofMillis(5);

	private final AppServiceHub serviceHub;

	private final CordaMapper cordaMapper;

	private final ObjectMapper objectMapper;

	private Map<UUID, FlowExecutionDTO> flows = new ConcurrentHashMap<>();

	protected Instant lastGc = Instant.now();

	public FlowExecutionRepositoryImpl(AppServiceHub serviceHub, ObjectMapper objectMapper, CordaMapper cordaMapper) {
		this(serviceHub, toStateMachineManager(serviceHub), objectMapper, cordaMapper);
	}

	@SneakyThrows
	private static StateMachineManager toStateMachineManager(AppServiceHub serviceHub) {
		// TODO https://github.com/corda/corda/issues/5931 to get progress and other useful information
		Object internalServiceHub = getField(serviceHub, "serviceHub");
		AbstractNode node = (AbstractNode) getField(internalServiceHub, "this$0");
		return (StateMachineManager) getField(AbstractNode.class, node, "smm");
	}


	public FlowExecutionRepositoryImpl(AppServiceHub serviceHub, StateMachineManager stateMachineManager, ObjectMapper objectMapper, CordaMapper cordaMapper) {
		super(FlowExecutionDTO.class);
		this.serviceHub = serviceHub;
		this.objectMapper = objectMapper;
		this.cordaMapper = cordaMapper;

		setupStateMachineListener(stateMachineManager);
	}

	/**
	 * Keep a cache of all currently running flows to allow fast current and historic repository access.
	 *
	 * @param smm
	 */
	private void setupStateMachineListener(StateMachineManager smm) {
		DataFeed<List<FlowLogic<?>>, StateMachineManager.Change> dataFeed = smm.track();
		for (FlowLogic flow : dataFeed.getSnapshot()) {
			addFlow(flow);
		}
		dataFeed.getUpdates().subscribe(change -> {
			if (change instanceof StateMachineManager.Change.Add) {
				addFlow(change.getLogic());
			} else if (change instanceof StateMachineManager.Change.Removed) {
				StateMachineManager.Change.Removed removed = (StateMachineManager.Change.Removed) change;
				removeFlow(change.getLogic(), removed.getResult());
			}
		});
	}

	protected void addFlow(FlowLogic flowLogic) {
		checkGc();

		UUID id = flowLogic.getRunId().getUuid();
		FlowExecutionDTO flowExecution;
		synchronized (flows) {
			flowExecution = flows.get(id);
			if (flowExecution == null) {
				flowExecution = new FlowExecutionDTO();
				flowExecution.setId(id);
				flowExecution.setFlowClass(flowLogic.getClass().getName());
				flowExecution.setLastModified(Instant.now());
				flows.put(flowExecution.getId(), flowExecution);
			}
		}

		ProgressTracker progressTracker = flowLogic.getProgressTracker();
		if (progressTracker != null) {
			ProgressTracker.Step currentStep = progressTracker.getCurrentStep();
			flowExecution.setCurrentStep(currentStep != null ? currentStep.getLabel() : null);

			FlowExecutionDTO finalFlowExecution = flowExecution;
			progressTracker.getChanges().subscribe(change -> {
				if (change instanceof ProgressTracker.Change.Position) {
					ProgressTracker.Change.Position position = (ProgressTracker.Change.Position) change;
					finalFlowExecution.setCurrentStep(position.getNewStep() != null ? position.getNewStep().getLabel() : null);
					finalFlowExecution.setLastModified(Instant.now());
				}
			});
		}
	}

	protected void removeFlow(FlowLogic<?> flow, Try result) {
		checkGc();

		UUID id = flow.getRunId().getUuid();
		FlowExecutionDTO dto = flows.get(id);
		if (dto != null) {
			dto.setLastModified(Instant.now());
			dto.setCurrentStep(FlowExecutionDTO.ENDED_STEP);
			try {
				dto.setResult(objectMapper.valueToTree(result.getOrThrow()));
			} catch (Exception e) {
				dto.setError(objectMapper.valueToTree(e));
			}
		} else {
			log.warn("failed to find removed flow in local cache: {}", flow);
		}
	}

	/**
	 * Remove ended flows after five minutes
	 */
	protected void checkGc() {
		Instant now = Instant.now();
		if (lastGc.plus(GC_INTERVAL).compareTo(now) < 0) {
			lastGc = now;
			Iterator<FlowExecutionDTO> iterator = flows.values().iterator();
			while (iterator.hasNext()) {
				FlowExecutionDTO dto = iterator.next();
				if (FlowExecutionDTO.ENDED_STEP.equals(dto.getCurrentStep()) && dto.getLastModified().plus(KEEP_ENDED_FLOW_INTERVAL).compareTo(now) < 0) {
					iterator.remove();
				}
			}
		}
	}


	@Override
	@SneakyThrows
	public FlowExecutionDTO create(FlowExecutionDTO flow) {
		// FIXME consider mapping of DTO <-> Corda data structures
		Class<? extends FlowLogic> flowClass = (Class<? extends FlowLogic>) Class.forName(flow.getFlowClass());
		Constructor<?> constructor = findConstructor(flowClass);

		FlowLogic flowLogic;
		if (constructor.getParameterCount() == 1) {
			JsonNode jsonParameters = flow.getParameters();
			if (jsonParameters == null) {
				throw new UnsupportedOperationException("no flow parameters provided");
			}

			ObjectReader reader = objectMapper.readerFor(constructor.getParameterTypes()[0]);
			Object parameters = reader.readValue(jsonParameters);
			flowLogic = (FlowLogic) constructor.newInstance(parameters);
		} else {
			flowLogic = flowClass.newInstance();
		}

		FlowHandle flowHandle = serviceHub.startFlow(flowLogic);

		// due to concurrency not quite clear who will place it in our cache (here or listener)
		synchronized (flows) {
			UUID id = flowHandle.getId().getUuid();

			FlowExecutionDTO runningFlow = flows.get(id);
			if (runningFlow == null) {
				runningFlow = flow;
				runningFlow.setLastModified(Instant.now());
				runningFlow.setId(id);
				runningFlow.setCurrentStep(flowLogic.getProgressTracker().getCurrentStep().toString());
				flows.put(id, flow);
			}
			runningFlow.setParameters(flow.getParameters());
			return runningFlow;
		}
	}

	private Constructor<?> findConstructor(Class<?> flowClass) {
		List<Constructor<?>> constructors = Arrays.asList(flowClass.getConstructors());
		List<Constructor<?>> singleParamConstructors = constructors.stream().filter(it -> it.getParameterCount() <= 1).collect(Collectors.toList());
		if (singleParamConstructors.size() != 1) {
			throw new UnsupportedOperationException("must have exactly one constructor with a single or no argument, found:  " + singleParamConstructors + " for " + flowClass);
		}
		return singleParamConstructors.get(0);
	}

	@Override
	public FlowExecutionDTO findOne(UUID id, QuerySpec querySpec) {
		return flows.get(id);
	}

	@Override
	public ResourceList<FlowExecutionDTO> findAll(QuerySpec querySpec) {
		return querySpec.apply(flows.values());
	}

	private static Object getField(Object object, String name) {
		return getField(object.getClass(), object, name);
	}

	@SneakyThrows
	private static Object getField(Class declaringClass, Object object, String name) {
		Field field = declaringClass.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(object);
	}
}
