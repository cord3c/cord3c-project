package io.cord3c.ssi.api.internal;

import lombok.SneakyThrows;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class DIDGenerator {

	public static final String DID_WEB_PREFIX = "did:web";

	public static final String ALG_SHA256 = "SHA-256";

	public static String toWellKnownDid(String url) {
		return DID_WEB_PREFIX + ":" + VCUtils.toHost(url);
	}

	public static String generateRandomDid(String url) {
		return toWellKnownDid(url) + ":" + UUID.randomUUID().toString();
	}


	@SneakyThrows(NoSuchAlgorithmException.class)
	private static byte[] toSHA256(byte[] bytes) {
		MessageDigest messageDigest = MessageDigest.getInstance(ALG_SHA256);
		return messageDigest.digest(bytes);
	}
}
