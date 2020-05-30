package io.cord3c.ssi.serialization.credential;

import java.time.OffsetDateTime;
import java.util.List;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

public interface EventState extends ContractState {

	OffsetDateTime getTimestamp();

	void setTimestamp(OffsetDateTime offsetDateTime);

	Party getIssuerNode();

	void setIssuerNode(Party issuerNode);

	List<AbstractParty> getParticipants();

}
