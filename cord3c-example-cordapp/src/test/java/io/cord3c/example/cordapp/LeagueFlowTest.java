package io.cord3c.example.cordapp;

import io.cord3c.ssi.api.internal.DIDGenerator;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.corda.internal.information.VCProperties;
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
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.*;
import java.util.concurrent.TimeUnit;

@TestInstance(Lifecycle.PER_CLASS)
public class LeagueFlowTest implements WithAssertions {

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
		System.setProperty(VCProperties.SERVER_URL, "localhost");
		System.setProperty(VCProperties.NETWORK_MAP_URL, "http://localhost");
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
	public void issueMembership() {
		String did = DIDGenerator.generateRandomDid("http://private");

		IssueLeagueMembership.IssueMembershipInput input = new IssueLeagueMembership.IssueMembershipInput();
		input.setDid(did);
		input.setName("Flash");

		CordaFuture<VerifiableCredential> future = node1.startFlow(new IssueLeagueMembership.IssueMembershipInititor(input));
		network.runNetwork();
		VerifiableCredential credential = future.get(4, TimeUnit.SECONDS);
		assertThat(credential.getId()).isEqualTo("localhost/leagueMembership/Flash");
	}
}
