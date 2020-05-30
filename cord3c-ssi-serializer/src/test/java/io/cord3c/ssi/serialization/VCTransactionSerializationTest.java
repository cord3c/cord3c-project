package io.cord3c.ssi.serialization;

import java.io.NotSerializableException;
import java.security.PublicKey;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.cord3c.ssi.serialization.setup.TestParty;
import io.cord3c.ssi.serialization.setup.VCTestCommands;
import io.cord3c.ssi.serialization.setup.VCTestState;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.AttachmentResolutionException;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionResolutionException;
import net.corda.core.identity.AbstractParty;
import net.corda.core.internal.CordaUtilsKt;
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
import net.corda.testing.node.TestCordapp;
import net.corda.testing.node.internal.CustomCordapp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class VCTransactionSerializationTest {

	private TestParty association = TestParty.associationNotary("association");

	private MockNetwork network;

	private SerializationContext context = null;

	private static Collection<TestCordapp> cordapps() {
		return Collections.singletonList(
				new CustomCordapp(new HashSet<>(Arrays.asList("io.cord3c.ssi")), "mock-cordapp", 1, CordaUtilsKt.PLATFORM_VERSION, Collections.emptySet(), Collections.emptyList(), null, new HashMap<>()));
	}

	@BeforeAll
	public void setup() {
		List<TestParty> allNodes = Arrays.asList(association);
		final MockNetworkParameters defaultParameters = new MockNetworkParameters().withNotarySpecs(Arrays.asList(new MockNetworkNotarySpec(association.toX500Name()))).withCordappsForAllNodes(cordapps());
		network = new MockNetwork(defaultParameters);
		network.startNodes();
		association.addToNetwork(network);
	}

	@org.junit.jupiter.api.Test
	public void test() throws TransactionResolutionException, AttachmentResolutionException, MissingContractAttachments, NotSerializableException {
		StartedMockNode node = association.getNode();
		ServiceHub services = node.getServices();

		VCTestState state = new VCTestState();
		state.setTimestamp(OffsetDateTime.now());
		state.setValue(42);
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

		VCSerializationScheme scheme = new VCSerializationScheme();
		SerializedBytes serialize = scheme.serialize(wireTransaction, context);
		System.out.println(serialize);

	}
}
