package io.cord3c.ssi.corda.serialization;

import com.google.common.collect.ImmutableList;
import io.cord3c.common.test.VCTestUtils;
import io.cord3c.ssi.corda.internal.party.CordaPartyRegistry;
import io.cord3c.ssi.corda.internal.party.PartyRegistry;
import io.cord3c.common.test.setup.TestParty;
import io.cord3c.common.test.setup.VCTestCommands;
import io.cord3c.common.test.setup.VCTestState;
import net.corda.core.contracts.AttachmentResolutionException;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionResolutionException;
import net.corda.core.identity.AbstractParty;
import net.corda.core.node.ServiceHub;
import net.corda.core.serialization.SerializationContext;
import net.corda.core.serialization.SerializedBytes;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.core.transactions.MissingContractAttachments;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.transactions.WireTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkNotarySpec;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.io.NotSerializableException;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@TestInstance(Lifecycle.PER_CLASS)
public class VCTransactionSerializationTest {

	private TestParty association = TestParty.associationNotary("association");

	private MockNetwork network;

	private SerializationContext context = null;

	@BeforeAll
	public void setup() {
		List<TestParty> allNodes = Arrays.asList(association);
		final MockNetworkParameters defaultParameters = new MockNetworkParameters().withNotarySpecs(Arrays.asList(
				new MockNetworkNotarySpec(association.toX500Name()))).withCordappsForAllNodes(VCTestUtils.cordapps());
		network = new MockNetwork(defaultParameters);
		network.startNodes();
		association.addToNetwork(network);
	}

	@AfterAll
	public void tearDown() {
		network.stopNodes();
	}

	@Test
	@Disabled // FIXME
	public void test() throws TransactionResolutionException, AttachmentResolutionException, MissingContractAttachments, NotSerializableException {
		StartedMockNode node = association.getNode();
		ServiceHub services = node.getServices();

		VCTestState state = new VCTestState();
		state.setTimestamp(Instant.now());
		state.setValue(42);
		state.setId("foo");
		state.setIssuerNode(association.getParty());
		state.setSubjectNode(association.getParty());

		ImmutableList.Builder<AbstractParty> participantListBuilder = new ImmutableList.Builder<>();
		participantListBuilder.add(association.getParty());

		List<PublicKey> participatingKeys = Arrays.asList(association.getParty().getOwningKey());
		Command issueCommand = new Command(new VCTestCommands.Test(), participatingKeys);

		TransactionBuilder txBuilder = new TransactionBuilder(association.getParty());
		txBuilder.addOutputState(state);
		txBuilder.addCommand(issueCommand);

		LedgerTransaction ledgerTransaction = txBuilder.toLedgerTransaction(services);

		WireTransaction wireTransaction = txBuilder.toWireTransaction(services);

		System.out.println(wireTransaction);

		CordaPartyRegistry partyRegistry = new CordaPartyRegistry(node.getServices().getIdentityService());
		partyRegistry.setNetworkMapUrl("mock-networkmap.org");
		String baseUrl = "http://localhost";
		VCSerializationScheme scheme = new VCSerializationScheme(partyRegistry, baseUrl);
		SerializedBytes serialize = scheme.serialize(wireTransaction, context);
		System.out.println(serialize);
		System.out.println("Hallo");

	}
}
