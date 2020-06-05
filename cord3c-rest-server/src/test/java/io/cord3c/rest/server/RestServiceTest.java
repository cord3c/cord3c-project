package io.cord3c.rest.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cord3c.common.test.VCTestUtils;
import io.cord3c.monitor.ping.PingFlow;
import io.cord3c.monitor.ping.PingInput;
import io.cord3c.rest.client.*;
import io.cord3c.rest.client.map.NodeDTO;
import io.cord3c.rest.client.map.NotaryDTO;
import io.cord3c.rest.client.map.PartyDTO;
import io.cord3c.rest.server.internal.RestServletFactory;
import io.cord3c.server.http.HttpService;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
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
public class RestServiceTest implements WithAssertions {

	private MockNetwork network;

	private StartedMockNode node;

	private NodeRestClient client;

	private CordaX500Name name;

	private static Collection<TestCordapp> cordapps() {
		return Collections.singletonList(
				new CustomCordapp(new HashSet<>(Arrays.asList("io.cord3c")), "mock-cordapp", 1, CordaUtilsKt.PLATFORM_VERSION, Collections.emptySet(), Collections.emptyList(), null, new HashMap<>()));
	}

	@BeforeAll
	public void setup() {
		RestServletFactory.setNetworkMapHost("http://localhost:8080");

		name = new CordaX500Name("STAR Labs", "Central City", "US");
		final MockNetworkParameters defaultParameters = new MockNetworkParameters().withCordappsForAllNodes(cordapps());
		network = new MockNetwork(defaultParameters);
		network.startNodes();
		node = network.createNode(name);

		client = new NodeRestClient(getUrl());
	}

	@Test
	public void verifyApiListing() throws IOException {
		String url = getUrl();
		HttpClient client = HttpClientBuilder.create().build();
		HttpResponse response = client.execute(new HttpGet(url));
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.reader().readTree(EntityUtils.toString(response.getEntity()));

		JsonNode links = jsonNode.get("links");
		assertThat(links.has("flow")).isTrue();
		assertThat(links.has("node")).isTrue();
		assertThat(links.has("notary")).isTrue();
		assertThat(links.has("party")).isTrue();
		assertThat(links.has("vault")).isTrue();
	}


	@Test
	public void verifyVCs() {
		VerifiableCredential credential = VCTestUtils.generateMockCredentials();

		VCRepository repository = client.getCredentials();
		repository.create(new VerifiableCredentialDTO(credential));

		verifyFindAllVCs(credential);
		verifyFindById(credential);
		verifyFindByCredentialAttribute(credential, true);
		verifyFindByCredentialAttribute(credential, false);
		verifyFindByClaim(credential, true);
		verifyFindByClaim(credential, false);
	}

	private void verifyFindByClaim(VerifiableCredential credential, boolean match) {
		VerifiableCredentialDTO dto = new VerifiableCredentialDTO(credential);

		PathSpec issuerPath = PathSpec.of(VerifiableCredentialDTO.Fields.credential, VerifiableCredential.Fields.claims, "hello");
		QuerySpec querySpec = new QuerySpec(VerifiableCredentialDTO.class);
		querySpec.addFilter(issuerPath.filter(FilterOperator.EQ, match ? "world" : "does not exist"));

		VCRepository repository = client.getCredentials();
		List<VerifiableCredentialDTO> list = repository.findAll(querySpec);
		if (match) {
			assertThat(list).hasSize(1);
			VerifiableCredential resultCredential = list.get(0).getCredential();
			assertThat(resultCredential.getId()).isEqualTo(dto.getCredential().getId());
		} else {
			assertThat(list).isEmpty();
		}
	}

	private void verifyFindById(VerifiableCredential credential) {
		VerifiableCredentialDTO dto = new VerifiableCredentialDTO(credential);

		VCRepository repository = client.getCredentials();
		VerifiableCredential searchedCredential = repository.findOne(dto.getId(), new QuerySpec(VerifiableCredentialDTO.class)).getCredential();
		assertThat(searchedCredential).isEqualToComparingFieldByField(credential);
	}

	private void verifyFindByCredentialAttribute(VerifiableCredential credential, boolean match) {
		VerifiableCredentialDTO dto = new VerifiableCredentialDTO(credential);

		PathSpec issuerPath = PathSpec.of(VerifiableCredentialDTO.Fields.credential, VerifiableCredential.Fields.issuer);
		QuerySpec querySpec = new QuerySpec(VerifiableCredentialDTO.class);
		querySpec.addFilter(issuerPath.filter(FilterOperator.EQ, match ? dto.getCredential().getIssuer() : "does not exist"));

		VCRepository repository = client.getCredentials();
		List<VerifiableCredentialDTO> list = repository.findAll(querySpec);
		if (match) {
			assertThat(list).hasSize(1);
			VerifiableCredential resultCredential = list.get(0).getCredential();
			assertThat(resultCredential.getId()).isEqualTo(dto.getCredential().getId());
		} else {
			assertThat(list).isEmpty();
		}
	}

	private void verifyFindAllVCs(VerifiableCredential credential) {
		VCRepository repository = client.getCredentials();
		ResourceList<VerifiableCredentialDTO> list = repository.findAll(new QuerySpec(VerifiableCredentialDTO.class));
		assertThat(list).hasSize(1);
		VerifiableCredential searchedCredential = list.get(0).getCredential();
		assertThat(searchedCredential).isEqualToComparingFieldByField(credential);
	}

	@Test
	public void verifyNodes() {
		QuerySpec querySpec = new QuerySpec(NodeDTO.class);
		querySpec.includeRelation(PathSpec.of(NodeDTO.Fields.legalIdentitiesAndCerts));
		ResourceList<NodeDTO> nodes = client.getNodes().findAll(querySpec);
		assertThat(nodes).hasSize(2);
		NodeDTO node = nodes.get(1);
		assertThat(node.getAddresses()).hasSize(1);
		assertThat(node.getLegalIdentitiesAndCerts()).hasSize(1);
		ServerAddress serverAddress = node.getAddresses().get(0);
		assertThat(serverAddress.getHost()).isEqualTo("mock.node");
		assertThat(serverAddress.getPort()).isEqualTo(1000);
		PartyDTO party = node.getLegalIdentitiesAndCerts().get(0);
		assertThat(party.getName().getCountry()).isEqualTo("US");
		assertThat(party.getName().getLocality()).isEqualTo("Central City");
		assertThat(party.getName().getOrganisation()).isEqualTo("STAR Labs");
	}

	@Test
	public void verifyNotaries() {
		QuerySpec querySpec = new QuerySpec(NotaryDTO.class);
		ResourceList<NotaryDTO> notaries = client.getNotaries().findAll(querySpec);
		assertThat(notaries).hasSize(1);
		NotaryDTO notary = notaries.get(0);
		assertThat(notary.getId()).isNotNull();
	}

	@Test
	public void verifyParties() {
		QuerySpec querySpec = new QuerySpec(PartyDTO.class);
		ResourceList<PartyDTO> parties = client.getParties().findAll(querySpec);
		assertThat(parties).hasSize(2);
		PartyDTO party = parties.get(1);
		assertThat(party.getName().getCountry()).isEqualTo("US");
		assertThat(party.getName().getLocality()).isEqualTo("Central City");
		assertThat(party.getName().getOrganisation()).isEqualTo("STAR Labs");
	}

	@Test
	public void verifyVault() {
		QuerySpec querySpec = new QuerySpec(VaultStateDTO.class);
		ResourceList<VaultStateDTO> states = client.getVault().findAll(querySpec);
		assertThat(states).hasSize(0); // FIXME add test data
	}


	@Test
	public void verifyRunningFlows() {
		QuerySpec querySpec = new QuerySpec(RunningFlowDTO.class);
		ResourceList<RunningFlowDTO> states = client.getFlows().findAll(querySpec);
		assertThat(states).hasSize(0); // FIXME add test flows data
	}

	@Test
	public void verifyTriggerFlow() {
		PingInput input = new PingInput();
		input.setOtherParty(name.toString());
		input.setMessage("echo");
		RunningFlowDTO flow = new RunningFlowDTO();
		flow.setFlowClass(PingFlow.PingFlowInitiator.class.getName());
		flow.setParameters(toJson(input));
		flow = client.getFlows().create(flow);
		assertThat(flow.getId()).isNotNull();
	}

	private JsonNode toJson(Object value) {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.valueToTree(value);
	}


	private String getUrl() {
		ServiceHub serviceHub = node.getServices();
		HttpService httpService = serviceHub.cordaService(HttpService.class);
		return "http://127.0.0.1:" + httpService.getPort() + "/api";
	}
}
