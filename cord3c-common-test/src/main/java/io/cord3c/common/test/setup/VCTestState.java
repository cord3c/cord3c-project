package io.cord3c.common.test.setup;

import java.time.Instant;
import java.util.List;

import com.google.common.collect.ImmutableList;
import io.cord3c.ssi.annotations.Claim;
import io.cord3c.ssi.annotations.Id;
import io.cord3c.ssi.annotations.IssuanceTimestamp;
import io.cord3c.ssi.annotations.Issuer;
import io.cord3c.ssi.annotations.Json;
import io.cord3c.ssi.annotations.Subject;
import io.cord3c.ssi.annotations.VerifiableCredentialType;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

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

	@Id
	private String id;

	@Claim
	int value;

	@Json
	private String json;

	@Override
	public List<AbstractParty> getParticipants() {
		return ImmutableList.of(issuerNode);
	}
}
