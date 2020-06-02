package io.cord3c.rest.server.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.cord3c.rest.client.RunningFlowDTO;
import io.cord3c.rest.client.RunningFlowRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import lombok.SneakyThrows;
import net.corda.core.flows.FlowLogic;
import net.corda.core.messaging.DataFeed;
import net.corda.core.messaging.FlowHandle;
import net.corda.core.messaging.StateMachineTransactionMapping;
import net.corda.core.node.AppServiceHub;
import net.corda.node.services.api.StateMachineRecordedTransactionMappingStorage;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RunningFlowRepositoryImpl extends ResourceRepositoryBase<RunningFlowDTO, String>
		implements RunningFlowRepository {

	private final AppServiceHub serviceHub;

	private static final CordaMapper MAPPER = Mappers.getMapper(CordaMapper.class);

	private final ObjectMapper objectMapper;


	public RunningFlowRepositoryImpl(AppServiceHub serviceHub, ObjectMapper objectMapper) {
		super(RunningFlowDTO.class);
		this.serviceHub = serviceHub;
		this.objectMapper = objectMapper;
	}

	@Override
	@SneakyThrows
	public RunningFlowDTO create(RunningFlowDTO flow) {
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
		flow.setId(flowHandle.getId().getUuid());
		return flow;
	}

	private Constructor<?> findConstructor(Class<?> flowClass) {
		List<Constructor<?>> constructors = Arrays.asList(flowClass.getConstructors());
		List<Constructor<?>> singleParamConstructors = constructors.stream().filter(it -> it.getParameterCount() <= 1).collect(Collectors.toList());
		if (singleParamConstructors.size() != 1) {
			throw new UnsupportedOperationException("must have exactly one constructor with a single or no argument, found:  " + singleParamConstructors + " for " + flowClass);
		}
		return singleParamConstructors.get(0);
	}

	@SneakyThrows
	@Override
	public ResourceList<RunningFlowDTO> findAll(QuerySpec querySpec) {
		// FIXME https://github.com/corda/corda/issues/5931 to get progress and other useful information
		Field serviceHubField = this.serviceHub.getClass().getDeclaredField("serviceHub");
		serviceHubField.setAccessible(true);
		Object internalServiceHub = serviceHubField.get(serviceHub);
		Field stateMachineField = internalServiceHub.getClass().getDeclaredField("stateMachineRecordedTransactionMapping");
		stateMachineField.setAccessible(true);
		StateMachineRecordedTransactionMappingStorage stateMachine = (StateMachineRecordedTransactionMappingStorage) stateMachineField.get(internalServiceHub);

		DataFeed<List<StateMachineTransactionMapping>, StateMachineTransactionMapping> feed = stateMachine.track();
		List<StateMachineTransactionMapping> snapshot = feed.getSnapshot();
		feed.getUpdates().subscribe().unsubscribe();
		// TODO add progress
		List<RunningFlowDTO> list = snapshot.stream()
				.map(it -> MAPPER.map(it))
				.collect(Collectors.toList());
		return querySpec.apply(list);
	}
}
