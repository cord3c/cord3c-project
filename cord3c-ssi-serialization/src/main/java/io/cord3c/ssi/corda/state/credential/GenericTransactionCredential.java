package io.cord3c.ssi.corda.state.credential;

import com.google.common.collect.ImmutableList;
import io.cord3c.ssi.annotations.*;
import lombok.Data;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateRef;
import net.corda.core.contracts.TimeWindow;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Can take any number of input and output states and issue a credential for a transaction with those states.
 */
@Data
@VerifiableCredentialType
public class GenericTransactionCredential implements ContractState, InputReferenceCredential, TimeWindowCredential, TransactionCredential, TypedCredential {

	@IssuanceTimestamp
	private Instant timestamp;

	@Issuer
	private Party issuerNode;

	private List<String> types = new ArrayList<>();

	@Subject
	private Party subjectNode;

	@Claim
	TimeWindow timeWindow;

	@Claim
	List<StateRef> references;

	@Claim
	List<StateRef> inputs;

	@Claim
	List<StateRef> outputs;

	@Claim
	private byte[] privacySalt;

	@Claim
	private String network;

	@Override
	public List<AbstractParty> getParticipants() {
		return ImmutableList.of(issuerNode);
	}
}
