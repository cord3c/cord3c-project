package io.cord3c.ssi.serialization.internal.party;

import io.cord3c.ssi.serialization.internal.information.ValueAccessor;
import lombok.RequiredArgsConstructor;
import net.corda.core.identity.Party;

@RequiredArgsConstructor
public class PartyAdapterAccessor implements ValueAccessor<String> {

	private final ValueAccessor<Party> partyAccessor;

	private final PartyRegistry registry;

	@Override
	public String getValue(Object state) {
		Party party = partyAccessor.getValue(state);
		return party != null ? registry.toDid(party) : null;
	}

	@Override
	public void setValue(Object state, String did) {
		Party party = did != null ? registry.toParty(did) : null;
		partyAccessor.setValue(state, party);
	}

	@Override
	public Class<? extends String> getImplementationClass() {
		return String.class;
	}
}
