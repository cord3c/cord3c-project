package io.cord3c.ssi.api.internal.crypto;

import com.google.common.base.Verify;
import com.nimbusds.jose.JWSAlgorithm;
import io.cord3c.ssi.api.did.DIDPublicKey;
import lombok.SneakyThrows;
import net.i2p.crypto.eddsa.EdDSAPublicKey;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * see https://w3c-ccg.github.io/ld-cryptosuite-registry/.
 */
public class CryptoSuiteRegistry {

	private static final String SECP256K1_VERIFICATION_KEY = "EcdsaSecp256k1VerificationKey2019";

	private static final String SECP256K1_SIGNATURE = "EcdsaSecp256k1Signature2019";

	private static final String JWS_VERIFICATION_KEY_2020 = "JwsVerificationKey2020";

	private static final String JSON_WEB_SIGNATURE_2020 = "JsonWebSignature2020";

	private static final String EC_JAVA_KEY_ALGORITHM = "EC";

	private static final String SECP256K1_CURVE = "secp256k1";

	private Map<Class, String> classToKeyTypeMapping = new HashMap<>();

	private Map<String, String> keyToProofTypeMapping = new HashMap<>();

	private Map<JWSAlgorithm, String> algToProofTypeMapping = new HashMap<>();

	private Map<String, JWSAlgorithm> proofTypeToAlgMapping = new HashMap<>();

	public CryptoSuiteRegistry() {
		registerProofType(EdDSAPublicKey.class, JWS_VERIFICATION_KEY_2020, JSON_WEB_SIGNATURE_2020, JWSAlgorithm.EdDSA);
		registerProofType(ECPublicKey.class, SECP256K1_VERIFICATION_KEY, SECP256K1_SIGNATURE, JWSAlgorithm.ES256K);
	}

	private void registerProofType(Class keyImplementationType, String keyType, String proofType, JWSAlgorithm alg) {
		classToKeyTypeMapping.put(keyImplementationType, keyType);
		keyToProofTypeMapping.put(keyType, proofType);
		algToProofTypeMapping.put(alg, proofType);
		proofTypeToAlgMapping.put(proofType, alg);
	}

	public String toKeyType(PublicKey publicKey) {
		Optional<String> keyType = classToKeyTypeMapping.entrySet().stream()
				.filter(it -> it.getKey().isAssignableFrom(publicKey.getClass()))
				.map(it -> it.getValue()).findFirst();

		Verify.verify(keyType.isPresent(), "key of type %s not registered", publicKey.getClass());
		return keyType.get();
	}

	public String toProofType(DIDPublicKey publicKey) {
		String keyType = publicKey.getType();
		String proofType = keyToProofTypeMapping.get(keyType);
		Verify.verify(proofType != null, "key of type %s not registered", keyType);
		return proofType;
	}

	public String toProofType(JWSAlgorithm alg) {
		String proofType = algToProofTypeMapping.get(alg);
		Verify.verify(proofType != null, "alg %s not registered", alg);
		return proofType;
	}

	public JWSAlgorithm toJwsAlg(String proofType) {
		JWSAlgorithm alg = proofTypeToAlgMapping.get(proofType);
		Verify.verify(proofType != null, "proofType %s not registered", proofType);
		return alg;
	}

	@SneakyThrows
	public KeyPair generateKeyPair() {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(EC_JAVA_KEY_ALGORITHM);
		keyPairGenerator.initialize(new ECGenParameterSpec(SECP256K1_CURVE));
		return keyPairGenerator.generateKeyPair();
	}
}


