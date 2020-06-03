package io.cord3c.ssi.serialization.credential;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

public interface EventState extends ContractState {

	Instant getTimestamp();

	void setTimestamp(Instant timestamp);

	Party getIssuerNode();

	void setIssuerNode(Party issuerNode);

	List<AbstractParty> getParticipants();

}
