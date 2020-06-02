package io.cord3c.monitor.metrics;

import io.cord3c.server.http.HttpService;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.internal.CordaUtilsKt;
import net.corda.core.node.ServiceHub;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import net.corda.testing.node.internal.CustomCordapp;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.io.IOException;
import java.util.*;

@TestInstance(Lifecycle.PER_CLASS)
public class MetricsTest implements WithAssertions {

	private MockNetwork network;

	private StartedMockNode node;

	private CordaX500Name name;

	private static Collection<TestCordapp> cordapps() {
		return Collections.singletonList(
				new CustomCordapp(new HashSet<>(Arrays.asList("io.cord3c")), "mock-cordapp", 1, CordaUtilsKt.PLATFORM_VERSION, Collections.emptySet(), Collections.emptyList(), null, new HashMap<>()));
	}

	@BeforeAll
	public void setup() {
		name = new CordaX500Name("STAR Labs", "Central City", "US");
		final MockNetworkParameters defaultParameters = new MockNetworkParameters().withCordappsForAllNodes(cordapps());
		network = new MockNetwork(defaultParameters);
		network.startNodes();
		node = network.createNode(name);
	}

	@Test
	public void verifyMetrics() throws IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpResponse response = client.execute(new HttpGet(getUrl()));
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
		String metrics = EntityUtils.toString(response.getEntity());
		assertThat(metrics).contains("jvm_memory_max_bytes");
		assertThat(metrics).contains("hibernate_entities_inserts_total");
		assertThat(metrics).contains("cordapp_total");
		assertThat(metrics).contains("process_uptime_seconds");
	}

	private String getUrl() {
		ServiceHub serviceHub = node.getServices();
		HttpService httpService = serviceHub.cordaService(HttpService.class);
		return "http://127.0.0.1:" + httpService.getPort() + "/prometheus";
	}
}
