package io.cord3c.ssi.corda.serialization.setup;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.StartedMockNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class TestParty {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestParty.class);

	@NonNull
	public final String simpleName;

	@NonNull
	public final TestParty association;

	@NonNull
	public final TestParty primaryNotary;

	public StartedMockNode node;

	public Party party;

	@Setter
	private MockNetwork network;

	private TestParty(String simpleName, TestParty association, TestParty primaryNotary) {
		this.simpleName = simpleName;
		this.association = association == null ? this : association;
		this.primaryNotary = primaryNotary == null ? this : primaryNotary;
	}

	public static TestParty associationNotary(String name) {
		return new TestParty(name, null, null);
	}

	public static TestParty memberNotary(String name, TestParty association) {
		return new TestParty(name, association, null);
	}

	public static TestParty associationPeerOnly(String name, TestParty associationNotary) {
		return new TestParty(name, null, associationNotary);
	}

	public static TestParty memberPeerOnly(String name, TestParty association, TestParty primaryNotary) {
		return new TestParty(name, association, primaryNotary);
	}

	public boolean isNotary() {
		return primaryNotary == this;
	}

	public CordaX500Name toX500Name() {
		String city = isNotary() ? "notaryCity" : "peerCity";
		return new CordaX500Name(simpleName, city, "CH");
	}

	public void addToNetwork(MockNetwork network) {
		if (node != null) {
			// already started
			return;
		}

		//long seed = ByteBuffer.wrap(SecureHash.sha256(toX500Name().toString().getBytes()).getBytes()).getLong(); BigInteger.valueOf(seed)
		if (isNotary()) {
			// validating notaries were already started
			node = network.getNotaryNodes().stream().filter(x -> x.getInfo().getLegalIdentities().get(0).getName().equals(toX500Name())).findAny().orElseThrow(() -> new RuntimeException("missing notary node"));
		} else {
			// lets start a new node
			node = network.createPartyNode(toX500Name());
		}
		party = node.getInfo().getLegalIdentities().get(0);
		setupListener();
	}

	private void setupListener() {
		ServiceHub services = node.getServices();
		Logger logger = LoggerFactory.getLogger(LOGGER.getName() + "." + simpleName);
		services.getVaultService().getUpdates().subscribe(update -> logger.debug("vaultUpdate flowId={}, produced={}, consumed={}", update.getFlowId(), update.getProduced(), update.getConsumed()));
		services.getValidatedTransactions().getUpdates().subscribe(signedTransaction -> logger.debug("validated {}, signatures={}", signedTransaction, signedTransaction.getSigs()));
	}

	@SuppressWarnings("squid:S2142")
	public <T> T startFlow(FlowLogic<T> flowLogic) {
		CordaFuture<T> future = node.startFlow(flowLogic);
		network.runNetwork();
		try {
			return future.get(10, TimeUnit.SECONDS);
		} catch (ExecutionException | TimeoutException | InterruptedException e) {
			throw new IllegalStateException("failed to run flow " + flowLogic + " on node " + party, e);
		}
	}
}
