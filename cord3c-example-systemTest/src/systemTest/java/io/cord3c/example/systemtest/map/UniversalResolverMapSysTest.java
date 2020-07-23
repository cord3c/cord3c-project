package io.cord3c.example.systemtest.map;

import io.cord3c.example.systemtest.SecretExtension;
import io.cord3c.example.systemtest.SystemTestConfiguration;
import io.cord3c.example.systemtest.SystemTestProperties;
import io.cord3c.rest.client.NetworkMapRestClient;
import io.cord3c.rest.api.map.PartyDTO;
import io.cord3c.ssi.api.did.Authentication;
import io.cord3c.ssi.api.did.DIDDocument;
import io.cord3c.ssi.api.did.DIDPublicKey;
import io.cord3c.ssi.api.internal.VCUtils;
import io.cord3c.ssi.api.internal.crypto.CryptoSuiteRegistry;
import io.cord3c.ssi.api.resolver.DefaultUniversalResolver;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ExtendWith(SecretExtension.class)
@SpringBootTest(classes = SystemTestConfiguration.class)
public class UniversalResolverMapSysTest implements WithAssertions {

	@Autowired
	private NetworkMapRestClient client;

	@Autowired
	private SystemTestProperties properties;

	private DefaultUniversalResolver resolver = new DefaultUniversalResolver();

	@Test
	public void resolveParties() {
		ResourceList<PartyDTO> parties = client.getParties().findAll(new QuerySpec(PartyDTO.class));
		assertThat(parties).hasSize(1);

		PartyDTO party = parties.get(0);
		String did = party.getDid();
		DIDDocument document = resolver.resolve(did);
		assertThat(document.getId()).isEqualTo(did);
		assertThat(document.getPublicKeys()).hasSize(1);

		DIDPublicKey publicKey = document.getPublicKeys().get(0);
		assertThat(publicKey.getId()).isEqualTo(did + "#keys-1");
		assertThat(publicKey.getType()).isEqualTo(CryptoSuiteRegistry.JWS_VERIFICATION_KEY_2020);
		assertThat(publicKey.getPublicKeyBase58()).isNotNull();
	}

	@Test
	public void resolverRoot() {
		String did = getRootDid();
		DIDDocument document = resolver.resolve(did);

		assertThat(document.getId()).isEqualTo(did);
		assertThat(document.getPublicKeys()).hasSize(1);
		assertThat(document.getContext()).isEqualTo("https://www.w3.org/ns/did/v1");

		DIDPublicKey publicKey = document.getPublicKeys().get(0);

		assertThat(publicKey.getId()).isEqualTo(did + "#keys-1");
		assertThat(publicKey.getType()).isEqualTo(CryptoSuiteRegistry.SECP256R1_VERIFICATION_KEY);
		assertThat(publicKey.getPublicKeyBase58()).isNotNull();

		List<Authentication> authentications = document.getAuthentications();
		assertThat(authentications).hasSize(1);
		Authentication authentication = authentications.get(0);
		assertThat(authentication.getPublicKey()).isEqualTo(Arrays.asList(publicKey.getId()));
	}

	private String getRootDid() {
		return "did:web:" + VCUtils.toHost(properties.getResolverUrl());
	}
}
