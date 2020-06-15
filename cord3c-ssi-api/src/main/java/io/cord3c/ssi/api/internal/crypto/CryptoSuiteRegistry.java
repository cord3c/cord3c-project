package io.cord3c.ssi.api.internal.crypto;

import com.google.common.base.Verify;
import com.nimbusds.jose.JWSAlgorithm;
import io.cord3c.ssi.api.did.DIDPublicKey;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.X509;
import sun.security.x509.X509Key;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EllipticCurve;
import java.util.*;

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

	private Map<String, String> toKeyTypeMapping = new HashMap<>();

	private Map<String, String> keyToProofTypeMapping = new HashMap<>();

	private Map<JWSAlgorithm, String> algToProofTypeMapping = new HashMap<>();

	private Map<String, JWSAlgorithm> proofTypeToAlgMapping = new HashMap<>();

	private List<CryptoRegistration> registrations = new ArrayList<>();

	@Data
	@RequiredArgsConstructor
	class CryptoRegistration {

		private final Class keyImplementationType;

		private final String curve;

		private final String keyType;

		private final String proofType;

		private final JWSAlgorithm jwsAlgorithm;

		private final Integer keySize;

	}

	public CryptoSuiteRegistry() {
		registerProofType(EdDSAPublicKey.class, null, JWS_VERIFICATION_KEY_2020, JSON_WEB_SIGNATURE_2020, JWSAlgorithm.EdDSA, null);
		registerProofType(ECPublicKey.class, SECP256K1_CURVE, SECP256K1_VERIFICATION_KEY, SECP256K1_SIGNATURE, JWSAlgorithm.ES256, 256);
		registerProofType(ECPublicKey.class, SECP256R1_CURVE, SECP256R1_VERIFICATION_KEY, SECP256R1_SIGNATURE, JWSAlgorithm.ES256K, 256);
	}

	private void registerProofType(Class keyImplementationType, String curve, String keyType, String proofType, JWSAlgorithm alg, Integer keySize) {
		registrations.add(new CryptoRegistration(keyImplementationType, curve, keyType, proofType, alg, keySize));
		keyToProofTypeMapping.put(keyType, proofType);
		algToProofTypeMapping.put(alg, proofType);
		proofTypeToAlgMapping.put(proofType, alg);
	}

	public String toKeyType(PublicKey publicKey) {
		Optional<String> keyType = registrations.stream()
				.filter(it -> it.getKeyImplementationType().isAssignableFrom(publicKey.getClass()))
				.filter(it -> it.curve == null || publicKey.toString().toLowerCase().contains(it.curve)) // unfortunately no clean API...
				.map(it -> it.getKeyType()).findFirst();

		Verify.verify(keyType.isPresent(), "unknown public key: %s, known keys: {}", publicKey, registrations);
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


