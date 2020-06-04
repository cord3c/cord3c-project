package io.cord3c.ssi.networkmap.adapter.manager.membership;

import io.cord3c.rest.client.NetworkMapRestClient;
import io.cord3c.rest.client.map.NetworkParametersDTO;
import io.cord3c.rest.client.map.NodeDTO;
import io.cord3c.rest.client.map.NotaryDTO;
import io.cord3c.rest.client.map.PartyDTO;
import io.cord3c.ssi.api.did.Authentication;
import io.cord3c.ssi.api.did.DIDDocument;
import io.cord3c.ssi.api.resolver.DefaultUniversalResolver;
import io.cord3c.ssi.api.vc.W3CHelper;
import io.cord3c.ssi.networkmap.adapter.config.VCNetworkMapConfiguration;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = VCNetworkMapConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = "server.port=8085")
public class CordaVCNetworkMapTest implements WithAssertions {

	@LocalServerPort
	protected int port;

	private DefaultUniversalResolver universalResolver;

	private NetworkMapRestClient client;

	@BeforeAll
	public void setup() {
		universalResolver = new DefaultUniversalResolver();
		client = new NetworkMapRestClient("http://localhost:" + port + "/api/");
	}

	@Test
	public void verifyNodeRepository() {
		QuerySpec querySpec = new QuerySpec(NodeDTO.class);
		querySpec.includeRelation(PathSpec.of(NodeDTO.Fields.legalIdentitiesAndCerts));
		ResourceList<NodeDTO> nodes = client.getNodes().findAll(querySpec);
		assertThat(nodes).isNotEmpty();
		NodeDTO node = nodes.get(0);
		assertThat(node.getAddresses()).hasSize(1);
		assertThat(node.getLegalIdentitiesAndCerts()).hasSize(1);
	}

	@Test
	public void verifyNotaryRepository() {
		QuerySpec querySpec = new QuerySpec(NotaryDTO.class);
		querySpec.includeRelation(PathSpec.of(NotaryDTO.Fields.identity));
		ResourceList<NotaryDTO> list = client.getNotaries().findAll(querySpec);
		assertThat(list).isNotEmpty();
		NotaryDTO notary = list.get(0);
		assertThat(notary.getIdentity()).isNotNull();
		assertThat(notary.isValidating());
	}


	@Test
	public void verifyPartyRepository() {
		QuerySpec querySpec = new QuerySpec(PartyDTO.class);
		ResourceList<PartyDTO> list = client.getParties().findAll(querySpec);
		assertThat(list).isNotEmpty();
		PartyDTO party = list.get(0);
		assertThat(party.getDid()).isNotNull();
		assertThat(party.getName()).isNotNull();
		assertThat(party.getDid()).isNotNull();
	}

	@Test
	public void verifyNetworkParametersRepository() {
		QuerySpec querySpec = new QuerySpec(NetworkParametersDTO.class);
		querySpec.includeRelation(PathSpec.of(NetworkParametersDTO.Fields.notaries));
		ResourceList<NetworkParametersDTO> list = client.getNetworkParameters().findAll(querySpec);
		assertThat(list).isNotEmpty();
		NetworkParametersDTO networkParameters = list.get(0);
		assertThat(networkParameters.getNotaries()).isNotEmpty();
		assertThat(networkParameters.getId()).isNotNull();
		assertThat(networkParameters.getModifiedTime()).isNotNull();
		assertThat(networkParameters.getMaxMessageSize()).isNotNull();
	}

	@Test
	public void verifyRootDid() {
		String did = "did:web:localhost:" + port;
		DIDDocument rootDoc = universalResolver.resolve(did);
		assertThat(rootDoc.getId()).isEqualTo(did);

		assertThat(rootDoc.getPublicKeys()).hasSize(1);
		assertThat(rootDoc.getAuthentications()).hasSize(1);

		Authentication authentication = rootDoc.getAuthentications().get(0);
		assertThat(authentication.getType()).isEqualTo(W3CHelper.JwsVerificationKey2020);
		assertThat(authentication.getPublicKey()).isEqualTo(Arrays.asList("did:web:localhost:8085#keys-1"));
	}

	@Test
	public void verifyPartyDid() {
		PartyDTO party = client.getParties().findAll(new QuerySpec(PartyDTO.class)).get(0);

		String did = party.getDid();
		DIDDocument partyDoc = universalResolver.resolve(did);
		assertThat(partyDoc).isNotNull();
	}
}
