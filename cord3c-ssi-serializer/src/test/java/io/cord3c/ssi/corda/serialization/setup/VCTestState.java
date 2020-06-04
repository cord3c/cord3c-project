package io.cord3c.ssi.corda.serialization.setup;

import java.time.Instant;
import java.util.List;

import com.google.common.collect.ImmutableList;
import io.cord3c.ssi.corda.state.annotations.Claim;
import io.cord3c.ssi.corda.state.annotations.Issuer;
import io.cord3c.ssi.corda.state.annotations.Subject;
import io.cord3c.ssi.corda.state.annotations.VerifiableCredentialType;
import io.cord3c.ssi.corda.state.credential.EventState;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

@Data
@FieldNameConstants
@ToString(callSuper = true)
@BelongsToContract(VCTestContract.class)
@VerifiableCredentialType(type = "test")
public class VCTestState implements EventState {

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
