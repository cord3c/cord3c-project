package io.cord3c.ssi.serialization.internal.party;

import com.google.common.base.Verify;
import net.corda.core.crypto.CryptoUtils;
import net.corda.core.identity.Party;
import net.corda.core.node.services.IdentityService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class CordaPartyRegistry implements PartyRegistry {

	private static final String PARTY_DID_PREFIX = "did:web:";

	private static final String PARTIES_PATH = ":parties:";

	private final Supplier<List<Party>> partySupplier;

	private final String networkMapHost;

	private final ConcurrentHashMap<String, Party> cache = new ConcurrentHashMap<>();

	public CordaPartyRegistry(String networkMapHost, Supplier<List<Party>> partySupplier) {
		this.networkMapHost = networkMapHost;
		this.partySupplier = partySupplier;
	}

	public CordaPartyRegistry(String networkMapHost, IdentityService identityService) {
		this.networkMapHost = networkMapHost;
		this.partySupplier = () -> {
			List<Party> parties = new ArrayList();
			identityService.getAllIdentities().forEach(it -> parties.add(it.getParty()));
			return parties;
		};
	}

	@Override
	public String toDid(Party party) {
		return PARTY_DID_PREFIX + networkMapHost + PARTIES_PATH + CryptoUtils.toStringShort(party.getOwningKey());
	}

	@Override
	public Party toParty(String did) {
		if (cache.containsKey(did)) {
			return cache.get(did);
		}

		Verify.verify(did.startsWith(PARTY_DID_PREFIX));

		int sep = did.indexOf(PARTIES_PATH);
		Verify.verify(sep != -1);

		String host = did.substring(PARTY_DID_PREFIX.length(), sep);
		String shortName = did.substring(sep + PARTIES_PATH.length());

		Verify.verify(host.equals(networkMapHost), "unknown network map: %s, expected %s", host, networkMapHost);

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
