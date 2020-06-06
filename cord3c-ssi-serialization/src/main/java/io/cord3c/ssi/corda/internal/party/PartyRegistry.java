package io.cord3c.ssi.corda.internal.party;

import net.corda.core.identity.Party;

public interface PartyRegistry {

	String toDid(Party party);

	Party toParty(String did);

}
