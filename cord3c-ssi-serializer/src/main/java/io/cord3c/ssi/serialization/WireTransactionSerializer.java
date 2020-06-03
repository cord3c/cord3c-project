package io.cord3c.ssi.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Verify;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.serialization.credential.*;
import net.corda.core.contracts.*;
import net.corda.core.crypto.SecureHash;
import net.corda.core.identity.Party;
import net.corda.core.transactions.WireTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WireTransactionSerializer extends StdSerializer<WireTransaction> {

	private static final String PRIVACY_SALT_CLAIM = "privacySalt";

	private static final String TIME_WINDOW_CLAIM = "timeWindow";

	private static final String NETWORK_CLAIM = "network";

	private static final String INPUTS_CLAIM = "inputs";

	private static final String REFERENCES_CLAIM = "referrences";

	private String networkMapHost;

	private String serverUrl;

	private VCMapper mapper;

	protected WireTransactionSerializer(String networkMapHost, String serverUrl, VCMapper mapper) {
		super(WireTransaction.class);
		this.networkMapHost = networkMapHost;
		this.serverUrl = serverUrl;
		this.mapper = mapper;
	}

	@Override
	public void serialize(WireTransaction tx, JsonGenerator gen, SerializerProvider provider) throws IOException {
		List<ContractState> outputStates = tx.getOutputStates();

		List<VerifiableCredential> credentials = new ArrayList<>();
		if (outputStates.size() == 1 && outputStates.get(0) instanceof TransactionCredential) {
			// we serialize transaction as single credentials
			TransactionCredential txCredential = (TransactionCredential) outputStates.get(0);
			toTransactionCredential(txCredential, tx);
			credentials.add(mapper.toCredential(txCredential));
		} else {
			// we serialize transaction as a set of credentials
			GenericTransactionCredential genericCredential = new GenericTransactionCredential();
			//setId(credential, tx);
			addNotary(genericCredential, tx);
			addCommands(genericCredential, tx);
			toTransactionCredential(genericCredential, tx);
			credentials.add(mapper.toCredential(genericCredential));
			outputStates.stream().map(it -> mapper.toCredential(it)).forEach(credentials::add);
		}

		Verify.verify(!credentials.isEmpty(), "not yet supported");
		if (credentials.size() == 1) {
			gen.writeObject(credentials.get(0));
		} else {
			gen.writeObject(credentials);
		}
	}

	private void addCommands(GenericTransactionCredential credential, WireTransaction tx) {
		List<Command<?>> commands = tx.getCommands();
		commands.forEach(it -> credential.getTypes().add(toType(it)));
	}

	private void toTransactionCredential(TransactionCredential credential, WireTransaction tx) {
		PrivacySalt privacySalt = tx.getPrivacySalt();
		SecureHash networkParametersHash = tx.getNetworkParametersHash();

		TimeWindow timeWindow = tx.getTimeWindow();
		List<StateRef> references = tx.getReferences();
		List<StateRef> inputs = tx.getInputs();

		if (!references.isEmpty()) {
			((InputReferenceCredential) credential).setReferences(references);
		}
		if (!inputs.isEmpty()) {
			((InputReferenceCredential) credential).setInputs(inputs);
		}
		if (timeWindow != null) {
			((TimeWindowCredential) credential).setTimeWindow(timeWindow);
		}
		if (privacySalt != null) {
			credential.setPrivacySalt(privacySalt.copyBytes());
		}
		if (networkParametersHash != null) {
			credential.setNetwork(toDid(networkParametersHash));
		}
	}

	/*
	private void setId(VerifiableCredential credential, WireTransaction tx) {
		// FIXME fix credential id
		String type = toType(tx.getCommands().get(0));
		String id = tx.getId().toString();
		credential.setId(serverUrl + "/credentials/" + type + "/" + id);
	}*/

	private void addNotary(GenericTransactionCredential credential, WireTransaction tx) {
		if (tx.getNotary() != null) {
			// claims are issued by notary, if a notary is specified
			credential.setIssuerNode(tx.getNotary());
		} else if (credential.getIssuerNode() == null) {
			throw new IllegalStateException("neither a notary nor a issuer specified");
		}
	}

	private String toType(Command<?> command) {
		return command.getValue().getClass().getSimpleName();
	}

	private String toDid(SecureHash networkParametersHash) {
		return "did:corda:" + networkMapHost + ":params:" + networkParametersHash.toString();
	}

	private String toDid(Party notary) {
		return "did:corda:" + networkMapHost + ":" + notary.getOwningKey().toString();
	}
}
