package io.cord3c.ssi.corda;

import io.cord3c.common.test.VCTestUtils;
import io.cord3c.common.test.setup.TestParty;
import io.cord3c.ssi.api.did.DIDPublicKey;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.vault.VCVault;
import lombok.SneakyThrows;
import net.corda.core.node.ServiceHub;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkNotarySpec;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.security.PublicKey;
import java.util.Arrays;

@TestInstance(Lifecycle.PER_CLASS)
public class VCServiceTest implements WithAssertions {

	private TestParty association = TestParty.associationNotary("association");

	private MockNetwork network;

	private VCVault vault;

	private VCService service;


	@BeforeAll
	public void setup() {
		final MockNetworkParameters defaultParameters = new
				MockNetworkParameters().withNotarySpecs(Arrays.asList(new MockNetworkNotarySpec(association.toX500Name()))).withCordappsForAllNodes(VCTestUtils.cordapps());
		network = new MockNetwork(defaultParameters);
		network.startNodes();
		association.addToNetwork(network);

		StartedMockNode node = association.getNode();
		ServiceHub services = node.getServices();
		service = services.cordaService(VCService.class);
		vault = service.getVault();
	}

	@AfterAll
	public void tearDown() {
		network.stopNodes();
	}

	@Test
	public void vaultWriteAndRead() {
		VerifiableCredential credential = VCTestUtils.generateMockCredentials();
		vault.record(credential);
		assertThat(vault.get(credential.toHashId())).isEqualToComparingFieldByField(credential);
	}

	@Test
	public void vaultDuplicateWrite() {
		VerifiableCredential credential = VCTestUtils.generateMockCredentials();
		vault.record(credential);
		vault.record(credential);
		assertThat(vault.get(credential.toHashId())).isEqualToComparingFieldByField(credential);
	}

	@Test
	@SneakyThrows
	public void verifySigning() {
		VerifiableCredential credential = VCTestUtils.generateMockCredentials();

		VerifiableCredential signed = service.sign(credential);
		assertThat(signed.getProof().getType()).isEqualTo("JsonWebSignature2020");
		assertThat(signed.getProof()).isNotNull();

		PublicKey publicKey = service.getIdentityKey().getPublicKey();
		service.verify(signed, publicKey);
	}

	@Test
	@SneakyThrows
	public void verifyCordaDidPublicKey() {
		DIDPublicKey publicKey = service.getCrypto().toDidKey(service.getIdentityKey().getPublicKey(), "did:hello:world");
		assertThat(publicKey.getType()).isEqualTo("JwsVerificationKey2020");
		assertThat(publicKey.getId()).isEqualTo("did:hello:world#keys-1");
		assertThat(publicKey.getController()).isEqualTo("did:hello:world");
	}
}
