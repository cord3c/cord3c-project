package io.cord3c.ssi.corda.internal.party;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import net.corda.core.identity.Party;
import net.corda.core.node.services.IdentityService;

@RequiredArgsConstructor
public class CordaPartySupplier implements Supplier<List<Party>> {

	private final IdentityService identityService;

	@Override
	public List<Party> get() {
		List<Party> parties = new ArrayList();
		identityService.getAllIdentities().forEach(it -> parties.add(it.getParty()));
		return parties;
	}
}
