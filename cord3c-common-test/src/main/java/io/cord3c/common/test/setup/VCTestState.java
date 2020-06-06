package io.cord3c.common.test.setup;

import com.google.common.collect.ImmutableList;
import io.cord3c.ssi.annotations.*;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.time.Instant;
import java.util.List;

@Data
@FieldNameConstants
@ToString(callSuper = true)
@BelongsToContract(VCTestContract.class)
@VerifiableCredentialType(type = "test")
public class VCTestState implements ContractState {

	@IssuanceTimestamp
	private Instant timestamp;

	@Issuer
	private Party issuerNode;

	@Subject
	private Party subjectNode;

	@Claim
	int value;

	@Override
	public List<AbstractParty> getParticipants() {
		return ImmutableList.of(issuerNode);
	}
}
