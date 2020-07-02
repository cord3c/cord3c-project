package io.cord3c.rest.server.internal;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cord3c.monitor.ping.PingFlow;
import io.cord3c.monitor.ping.PingInput;
import io.cord3c.rest.api.FlowExecutionDTO;
import io.crnk.core.queryspec.QuerySpec;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StateMachineRunId;
import net.corda.core.internal.FlowStateMachine;
import net.corda.core.messaging.DataFeed;
import net.corda.core.messaging.FlowHandle;
import net.corda.core.node.AppServiceHub;
import net.corda.core.utilities.Try;
import net.corda.node.services.statemachine.StateMachineManager;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import rx.Observable;

public class FlowExecutionRepositoryTest implements WithAssertions {

	private AppServiceHub serviceHub;

	private FlowExecutionRepositoryImpl impl;

	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		serviceHub = Mockito.mock(AppServiceHub.class);

		objectMapper = new ObjectMapper();
		CordaMapper cordaMapper = Mockito.mock(CordaMapper.class);
		StateMachineManager smm = Mockito.mock(StateMachineManager.class);

		// TODO improve observable setup to omit value in thenAnswer
		Observable<StateMachineManager.Change> observable = Observable.empty();
		DataFeed<List<FlowLogic<?>>, StateMachineManager.Change> smmFeed = new DataFeed<>(Collections.emptyList(), observable);
		Mockito.when(smm.track()).thenReturn(smmFeed);

		impl = new FlowExecutionRepositoryImpl(serviceHub, smm, objectMapper, cordaMapper);
	}

	@Test
	public void test() {
		PingFlow.PingFlowInitiator flowLogic = new PingFlow.PingFlowInitiator(new PingInput());

		FlowHandle handle = Mockito.mock(FlowHandle.class);
		StateMachineRunId runId = new StateMachineRunId(UUID.randomUUID());
		Mockito.when(serviceHub.startFlow(Mockito.any())).thenAnswer((Answer<FlowHandle>) invocation -> {
			FlowStateMachine<?> stateMachine = Mockito.mock(FlowStateMachine.class);
			Mockito.when(stateMachine.getId()).thenReturn(runId);
			flowLogic.setStateMachine(stateMachine);
			impl.addFlow(flowLogic); // should come from better observable setup...
			return handle;
		});
		Mockito.when(handle.getId()).thenReturn(runId);

		PingInput input = new PingInput();
		input.setOtherParty("other");
		input.setMessage("echo");
		JsonNode inputJson = objectMapper.valueToTree(input);
		String flowClassName = PingFlow.PingFlowInitiator.class.getName();

		// start flow
		FlowExecutionDTO flow = new FlowExecutionDTO();
		flow.setParameters(inputJson);
		flow.setFlowClass(flowClassName);
		flow = impl.create(flow);
		assertThat(flow.getLastModified()).isNotNull();
		assertThat(flow.getId()).isEqualTo(handle.getId().getUuid());

		// find flow
		QuerySpec querySpec = new QuerySpec(FlowExecutionDTO.class);
		assertThat(impl.findOne(runId.getUuid(), null)).isSameAs(flow);
		assertThat(impl.findAll(querySpec)).hasSize(1);

		// stop flow
		Try result = new Try.Success("Hello");
		impl.removeFlow(flowLogic, result);
		assertThat(impl.findAll(querySpec)).hasSize(1);
		assertThat(flow.getCurrentStep()).isEqualTo(FlowExecutionDTO.ENDED_STEP);

		// garbage collect will not collect
		impl.lastGc = Instant.now().minus(Duration.ofHours(1));
		impl.checkGc();
		assertThat(impl.findAll(querySpec)).hasSize(1);

		// garbage collect will not collect again
		impl.lastGc = Instant.now().minus(Duration.ofHours(1));
		impl.checkGc();
		assertThat(impl.findAll(querySpec)).hasSize(1);

		// entry expired, but garbage collection will not run
		flow.setLastModified(Instant.now().minus(Duration.ofHours(1)));
		impl.checkGc();
		assertThat(impl.findAll(querySpec)).hasSize(1);

		// garbage collection not run for a long time and will finally collect
		impl.lastGc = Instant.now().minus(Duration.ofHours(1));
		impl.checkGc();
		assertThat(impl.findAll(querySpec)).isEmpty();
	}
}
