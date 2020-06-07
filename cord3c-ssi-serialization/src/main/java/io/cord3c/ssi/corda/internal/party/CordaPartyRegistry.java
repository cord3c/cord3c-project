package io.cord3c.ssi.corda.internal.party;

import net.corda.core.crypto.CryptoUtils;
import net.corda.core.identity.Party;
import net.corda.core.node.services.IdentityService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class CordaPartyRegistry implements PartyRegistry {

	private final Supplier<List<Party>> partySupplier;

	private final ConcurrentHashMap<String, Party> cache = new ConcurrentHashMap<>();

	private PartyToDIDMapper mapper;

	public CordaPartyRegistry(Supplier<List<Party>> partySupplier) {
		this.partySupplier = partySupplier;
		this.mapper = new PartyToDIDMapper();
	}

	public CordaPartyRegistry(IdentityService identityService) {
		this(() -> {
			List<Party> parties = new ArrayList();
			identityService.getAllIdentities().forEach(it -> parties.add(it.getParty()));
			return parties;
		});
	}

	public void setNetworkMapUrl(String networkMapUrl) {
		mapper.setNetworkMapUrl(networkMapUrl);
	}

	public String getNetworkMapUrl() {
		return mapper.getNetworkMapUrl();
	}

	@Override
	public String toDid(Party party) {
		return mapper.toDid(party.getOwningKey());
	}


	@Override
	public Party toParty(String did) {
		if (cache.containsKey(did)) {
			return cache.get(did);
		}

		String shortName = mapper.getShortName(did);

		for (Party otherIdentity : partySupplier.get()) {
			String otherShortName = CryptoUtils.toStringShort(otherIdentity.getOwningKey());
			cache.put(otherShortName, otherIdentity);
			if (shortName.equals(otherShortName)) {
				return otherIdentity;
			}
		}

		throw new IllegalStateException("party not found: " + did);
	}
}
