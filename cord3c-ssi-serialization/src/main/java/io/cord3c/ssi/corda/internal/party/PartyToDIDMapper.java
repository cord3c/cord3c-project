package io.cord3c.ssi.corda.internal.party;

import com.google.common.base.Verify;
import io.cord3c.ssi.api.internal.PropertyUtils;
import io.cord3c.ssi.api.internal.VCUtils;
import io.cord3c.ssi.corda.internal.information.VCProperties;
import lombok.Getter;
import lombok.Setter;
import net.corda.core.crypto.CryptoUtils;

import java.security.PublicKey;

public class PartyToDIDMapper {

	private static final String PARTY_DID_PREFIX = "did:web:";

	private static final String PARTIES_PATH = ":parties:";

	@Getter
	@Setter
	private String networkMapUrl = PropertyUtils.getProperty(VCProperties.NETWORK_MAP_URL, null);

	public String toDid(PublicKey publicKey) {
		return toDid(CryptoUtils.toStringShort(publicKey));
	}

	public String toDid(String shortName) {
		return PARTY_DID_PREFIX + VCUtils.toHost(networkMapUrl) + PARTIES_PATH + shortName;
	}

	public String getShortName(String did) {
		Verify.verify(did.startsWith(PARTY_DID_PREFIX));

		int sep = did.indexOf(PARTIES_PATH);
		Verify.verify(sep != -1);

		String host = did.substring(PARTY_DID_PREFIX.length(), sep);
		String shortName = did.substring(sep + PARTIES_PATH.length());

		Verify.verify(host.equals(VCUtils.toHost(networkMapUrl)), "unknown network map: %s, expected %s", host, networkMapUrl);
		return shortName;
	}

}
