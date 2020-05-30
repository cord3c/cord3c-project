package io.cord3c.ssi.api.internal;

import lombok.SneakyThrows;
import net.corda.core.identity.CordaX500Name;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.UUID;

public class DIDGenerator {

	public static final String DID_WEB_PREFIX = "did:web";

	public static final String HASH_ALGORITHM = "SHA-256";

	public static String getPrefix(String domain) {
		return DID_WEB_PREFIX + ":" + domain;
	}

	public static String generateRandomDid(String domain) {
		return getPrefix(domain) + ":" + UUID.randomUUID().toString();
	}

	public static String computeOrganizationDid(String domain) {
		return getPrefix(domain);
	}

	public static String getDidFromCordaX500Name(CordaX500Name cordaX500Name, String domain) {
		return getDidFromString(cordaX500Name.toString(), domain);
	}

	public static String getDidFromString(String s, String domain) {
		return getPrefix(domain) + ":" + getHexHash(s);
	}

	public static String getDidFromPublicKey(String networkMapHost, PublicKey publicKey) {
		String publicKeyHash = new String(getHash(publicKey.getEncoded()));
		String publicKeyHashEncoded = Base64.getEncoder().encodeToString(publicKeyHash.getBytes());
		return DID_WEB_PREFIX + ":" + networkMapHost + ":" + publicKeyHashEncoded;
	}

	@SneakyThrows(NoSuchAlgorithmException.class)
	private static byte[] getHash(byte[] bytes) {
		MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
		return messageDigest.digest(bytes);
	}

	private static String getHexHash(String s) {
		byte[] hashedName = getHash(s.getBytes(StandardCharsets.UTF_8));
		BigInteger number = new BigInteger(1, hashedName);

		StringBuilder hexString = new StringBuilder(number.toString(16));
		while (hexString.length() < 64) {
			hexString.append("0");
		}

		return hexString.toString();
	}

	public static boolean compareNodeDIDs(String nodeDID, CordaX500Name nodeName) {
		String nodeDIDHash = nodeDID.substring(nodeDID.lastIndexOf(':') + 1);
		String nodeNameHash = getHexHash(nodeName.toString());
		return nodeDIDHash.equals(nodeNameHash);
	}

}
