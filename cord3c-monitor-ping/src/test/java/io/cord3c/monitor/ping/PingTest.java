package io.cord3c.monitor.ping;

import lombok.SneakyThrows;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.internal.CordaUtilsKt;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import net.corda.testing.node.internal.CustomCordapp;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.*;

@TestInstance(Lifecycle.PER_CLASS)
public class PingTest implements WithAssertions {

	private MockNetwork network;

	private StartedMockNode node1;

	private StartedMockNode node2;

	private CordaX500Name name1;

	private CordaX500Name name2;

	private static Collection<TestCordapp> cordapps() {
		return Collections.singletonList(
				new CustomCordapp(new HashSet<>(Arrays.asList("io.cord3c")), "mock-cordapp", 1, CordaUtilsKt.PLATFORM_VERSION, Collections.emptySet(), Collections.emptyList(), null, new HashMap<>()));
	}

	@BeforeAll
	public void setup() {
		name1 = new CordaX500Name("STAR Labs", "Central City", "US");
		name2 = new CordaX500Name("Wayne Enterprises", "Gotham City", "US");
		final MockNetworkParameters defaultParameters = new MockNetworkParameters().withCordappsForAllNodes(cordapps());
		network = new MockNetwork(defaultParameters);
		network.startNodes();
		node1 = network.createNode(name1);
		node2 = network.createNode(name2);
	}

	@SneakyThrows
	@Test
	public void verifyPingPong() {
		PingInput input = new PingInput();
		input.setOtherParty(name2.toString());
		input.setMessage("echo");
		CordaFuture<PingMessage> future = node1.startFlow(new PingFlow.PingFlowInitiator(input));
		network.runNetwork();
		PingMessage response = future.get();
		assertThat(response.getMessage()).isEqualTo("ECHO");
	}
}
