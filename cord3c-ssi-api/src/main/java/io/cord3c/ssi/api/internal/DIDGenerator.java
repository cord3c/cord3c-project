package io.cord3c.ssi.api.internal;

import io.cord3c.ssi.api.did.Authentication;
import io.cord3c.ssi.api.did.PublicKey;
import io.cord3c.ssi.api.vc.W3CHelper;
import lombok.SneakyThrows;
import net.corda.core.crypto.Base58;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class DIDGenerator {

	public static final String DID_WEB_PREFIX = "did:web";

	public static final String ALG_SHA256 = "SHA-256";

	public static String toWellKnownDid(String domain) {
		return DID_WEB_PREFIX + ":" + domain;
	}

	public static String generateRandomDid(String domain) {
		return toWellKnownDid(domain) + ":" + UUID.randomUUID().toString();
	}


	public static Authentication toAuthentication(PublicKey publicKey) {
		return new Authentication(W3CHelper.JwsVerificationKey2020, Arrays.asList(publicKey.getId()));

	}

	public static PublicKey toPublicKey(java.security.PublicKey owningKey, String did) {
		String publicKeyBase58 = Base58.encode(owningKey.getEncoded());
		return new PublicKey(did + "#keys-1", did, W3CHelper.JwsVerificationKey2020, publicKeyBase58);
	}

	@SneakyThrows(NoSuchAlgorithmException.class)
	private static byte[] toSHA256(byte[] bytes) {
		MessageDigest messageDigest = MessageDigest.getInstance(ALG_SHA256);
		return messageDigest.digest(bytes);
	}
}
