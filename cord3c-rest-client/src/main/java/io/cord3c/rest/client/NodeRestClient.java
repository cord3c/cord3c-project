package io.cord3c.rest.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cord3c.rest.client.map.NodeRepository;
import io.cord3c.rest.client.map.NotaryRepository;
import io.cord3c.rest.client.map.PartyRepository;
import io.crnk.client.CrnkClient;
import io.crnk.core.queryspec.QuerySpec;
import lombok.Getter;
import lombok.SneakyThrows;
import net.corda.core.flows.FlowLogic;
import net.jodah.typetools.TypeResolver;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class NodeRestClient {

	private static final Duration FLOW_BACKOFF_TIME_10s = Duration.ofSeconds(1);

	private static final Duration FLOW_BACKOFF_TIME_DEFAULT = Duration.ofSeconds(10);

	@Getter
	private final CrnkClient client;

	public NodeRestClient(String url) {
		this(new CrnkClient(url));
	}

	public NodeRestClient(CrnkClient client) {
		this.client = client;

		ObjectMapper mapper = client.getObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		client.findModules();
	}

	public NotaryRepository getNotaries() {
		return client.getRepositoryForInterface(NotaryRepository.class);
	}

	public VaultStateRepository getVault() {
		return client.getRepositoryForInterface(VaultStateRepository.class);
	}

	public FlowExecutionRepository getFlows() {
		return client.getRepositoryForInterface(FlowExecutionRepository.class);
	}

	public PartyRepository getParties() {
		return client.getRepositoryForInterface(PartyRepository.class);
	}

	public NodeRepository getNodes() {
		return client.getRepositoryForInterface(NodeRepository.class);
	}

	public VCRepository getCredentials() {
		return client.getRepositoryForInterface(VCRepository.class);
	}

	public <T> FlowExecutionDTO<T> invokeFlow(Class<? extends FlowLogic<T>> flowClass, Object input) {
		FlowExecutionDTO flow = new FlowExecutionDTO();
		flow.setFlowClass(flowClass.getName());
		flow.setParameters(toJson(input));
		prepareResultType(flow);
		return getFlows().create(flow);
	}

	@SneakyThrows
	private void prepareResultType(FlowExecutionDTO flow) {
		Class<?> flowClass = Class.forName(flow.getFlowClass());
		Class<?> flowReturnType = TypeResolver.resolveRawArgument(FlowLogic.class, flowClass);
		flow.prepareResultMapper(flowReturnType, client.getObjectMapper());
	}

	private JsonNode toJson(Object value) {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.valueToTree(value);
	}

	public FlowExecutionDTO waitForFlow(FlowExecutionDTO flow, Duration timeout) {
		return waitForFlow(flow.getId(), timeout);
	}

	@SneakyThrows
	public FlowExecutionDTO waitForFlow(UUID flowId, Duration timeout) {
		FlowExecutionDTO currentStatus = null;
		Instant startPhase = Instant.now().plus(Duration.ofSeconds(10));
		while (!timeout.isNegative()) {

			boolean initialPhase = Instant.now().compareTo(startPhase) < 0;
			Duration waitPeriod = initialPhase ? FLOW_BACKOFF_TIME_10s : FLOW_BACKOFF_TIME_DEFAULT;


			FlowExecutionRepository flows = getFlows();
			currentStatus = flows.findOne(flowId, new QuerySpec(FlowExecutionDTO.class));

			if (FlowExecutionDTO.ENDED_STEP.equals(currentStatus.getCurrentStep())) {
				prepareResultType(currentStatus);
				return currentStatus;
			}

			Thread.sleep(waitPeriod.toMillis());
			timeout = timeout.minus(waitPeriod);
		}
		throw new TimeoutException("flow failed to finish in time: " + currentStatus);
	}

	public CrnkClient getCrnk() {
		return client;
	}
}
