package io.cord3c.ssi.corda;

import io.cord3c.common.test.VCTestUtils;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.corda.serialization.setup.TestParty;
import io.cord3c.ssi.corda.vault.VCVault;
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

import java.util.Arrays;

@TestInstance(Lifecycle.PER_CLASS)
public class VCServiceTest implements WithAssertions {

	private TestParty association = TestParty.associationNotary("association");

	private MockNetwork network;

	private VCVault vault;


	@BeforeAll
	public void setup() {
		final MockNetworkParameters defaultParameters = new
				MockNetworkParameters().withNotarySpecs(Arrays.asList(new MockNetworkNotarySpec(association.toX500Name()))).withCordappsForAllNodes(VCTestUtils.cordapps());
		network = new MockNetwork(defaultParameters);
		network.startNodes();
		association.addToNetwork(network);

		StartedMockNode node = association.getNode();
		ServiceHub services = node.getServices();
		VCService vcService = services.cordaService(VCService.class);
		vault = vcService.getVault();
	}

	@AfterAll
	public void tearDown() {
		network.stopNodes();
	}

	@Test
	public void vaultWriteAndRead() {
		VerifiableCredential credential = VCTestUtils.generateMockCredentials();
		vault.record(credential);
		assertThat(vault.get(credential.getId())).isEqualToComparingFieldByField(credential);
	}

	@Test
	public void vaultDuplicateWrite() {
		VerifiableCredential credential = VCTestUtils.generateMockCredentials();
		vault.record(credential);
		vault.record(credential);
		assertThat(vault.get(credential.getId())).isEqualToComparingFieldByField(credential);
	}
}
