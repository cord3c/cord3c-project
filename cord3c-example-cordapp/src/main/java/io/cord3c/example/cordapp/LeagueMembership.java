package io.cord3c.example.cordapp;

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
@BelongsToContract(LeageContract.class)
@VerifiableCredentialType(type = "leagueMembership")
public class LeagueMembership implements ContractState {

	@IssuanceTimestamp
	private Instant timestamp;

	@Issuer
	private Party issuer;

	@Subject
	private String subject;

	@Id
	private String name;

	@Override
	public List<AbstractParty> getParticipants() {
		return ImmutableList.of(issuer);
	}
}
