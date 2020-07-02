package io.cord3c.ssi.api.internal.crypto;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import com.nimbusds.jose.JWSAlgorithm;
import io.cord3c.ssi.api.did.DIDPublicKey;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.corda.core.crypto.Base58;

@Data
@RequiredArgsConstructor
public class CryptoScheme {

	private final Class keyImplementationType;

	private final String curve;

	private final String keyType;

	private final String proofType;

	private final JWSAlgorithm jwsAlgorithm;

	private final Integer keySize;

	private final String javaAlg;

	@SneakyThrows
	public PublicKey decode(DIDPublicKey publicKey) {
		byte[] bytes = Base58.decode(publicKey.getPublicKeyBase58());
		KeyFactory keyFactory = KeyFactory.getInstance(javaAlg);
		return keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
	}
}