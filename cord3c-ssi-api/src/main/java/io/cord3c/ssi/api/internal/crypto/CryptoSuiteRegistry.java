package io.cord3c.ssi.api.internal.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nimbusds.jose.JWSAlgorithm;
import io.cord3c.ssi.api.did.DIDPublicKey;
import lombok.SneakyThrows;
import net.i2p.crypto.eddsa.EdDSAPublicKey;

/**
 * see https://w3c-ccg.github.io/ld-cryptosuite-registry/.
 */
public class CryptoSuiteRegistry {

	public static final String SECP256K1_VERIFICATION_KEY = "EcdsaSecp256k1VerificationKey2019";

	public static final String SECP256K1_SIGNATURE = "EcdsaSecp256k1Signature2019";

	// FIXME not standardized?
	public static final String SECP256R1_VERIFICATION_KEY = "EcdsaSecp256r1VerificationKey";

	public static final String SECP256R1_SIGNATURE = "EcdsaSecp256r1Signature";


	public static final String JWS_VERIFICATION_KEY_2020 = "JwsVerificationKey2020";

	public static final String JSON_WEB_SIGNATURE_2020 = "JsonWebSignature2020";


	private static final String EC_JAVA_KEY_ALGORITHM = "EC";

	private static final String SECP256K1_CURVE = "secp256k1";

	private static final String SECP256R1_CURVE = "secp256r1";

	private Map<String, CryptoScheme> keyMapping = new HashMap<>();

	private Map<JWSAlgorithm, String> algToProofTypeMapping = new HashMap<>();

	private Map<String, JWSAlgorithm> proofTypeToAlgMapping = new HashMap<>();

	private List<CryptoScheme> schemes = new ArrayList<>();

	public CryptoSuiteRegistry() {
		registerProofType(EdDSAPublicKey.class, null, JWS_VERIFICATION_KEY_2020, JSON_WEB_SIGNATURE_2020, JWSAlgorithm.EdDSA,
				null, "EdDSA");
		registerProofType(ECPublicKey.class, SECP256K1_CURVE, SECP256K1_VERIFICATION_KEY, SECP256K1_SIGNATURE,
				JWSAlgorithm.ES256,
				256, "EC");
		registerProofType(ECPublicKey.class, SECP256R1_CURVE, SECP256R1_VERIFICATION_KEY, SECP256R1_SIGNATURE,
				JWSAlgorithm.ES256K, 256, "EC");
	}

	private void registerProofType(Class keyImplementationType, String curve, String keyType, String proofType, JWSAlgorithm alg,
			Integer keySize, String javaAlg) {

		CryptoScheme scheme = new CryptoScheme(keyImplementationType, curve, keyType, proofType, alg, keySize, javaAlg);
		schemes.add(scheme);
		keyMapping.put(keyType, scheme);
		algToProofTypeMapping.put(alg, proofType);
		proofTypeToAlgMapping.put(proofType, alg);
	}

	public String toKeyType(PublicKey publicKey) {
		Optional<String> keyType = schemes.stream()
				.filter(it -> it.getKeyImplementationType().isAssignableFrom(publicKey.getClass()))
				.filter(it -> it.getCurve() == null || publicKey.toString().toLowerCase()
						.contains(it.getCurve())) // unfortunately no clean API...
				.map(it -> it.getKeyType()).findFirst();

		if (!keyType.isPresent()) {
			throw new IllegalStateException(String.format("unknown public key: %s, known keys: %s", publicKey, schemes));
		}
		return keyType.get();
	}

	public CryptoScheme findScheme(DIDPublicKey publicKey) {
		String keyType = publicKey.getType();
		CryptoScheme scheme = keyMapping.get(keyType);
		if (scheme == null) {
			throw new IllegalStateException(String.format("key of type %s not registered", keyType));
		}
		return scheme;
	}

	public String toProofType(JWSAlgorithm alg) {
		String proofType = algToProofTypeMapping.get(alg);
		if (proofType == null) {
			throw new IllegalStateException(String.format("alg %s not registered", alg));
		}
		return proofType;
	}

	public JWSAlgorithm toJwsAlg(String proofType) {
		JWSAlgorithm alg = proofTypeToAlgMapping.get(proofType);
		if (alg == null) {
			throw new IllegalStateException(String.format("proofType %s not registered", proofType));
		}
		return alg;
	}

	@SneakyThrows
	public KeyPair generateKeyPair() {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(EC_JAVA_KEY_ALGORITHM);
		keyPairGenerator.initialize(new ECGenParameterSpec(SECP256K1_CURVE));
		return keyPairGenerator.generateKeyPair();
	}
}


