package io.cord3c.ssi.serialization.setup;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import com.google.common.collect.ImmutableList;
import io.cord3c.ssi.serialization.annotations.Claim;
import io.cord3c.ssi.serialization.annotations.Issuer;
import io.cord3c.ssi.serialization.annotations.Subject;
import io.cord3c.ssi.serialization.annotations.VerifiableCredentialType;
import io.cord3c.ssi.serialization.credential.EventState;
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
