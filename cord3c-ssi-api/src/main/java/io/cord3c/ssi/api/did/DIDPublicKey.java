package io.cord3c.ssi.api.did;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.corda.core.crypto.Base58;

import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DIDPublicKey {

	public static final String EC_KEY_ALG = "EC";

	private String id;

	private String controller;

	private String type;

	private String publicKeyBase58;

	@SneakyThrows
	public java.security.PublicKey decode() {
		byte[] bytes = Base58.decode(publicKeyBase58);
		KeyFactory keyFactory = KeyFactory.getInstance(EC_KEY_ALG);
		return keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
	}
}
