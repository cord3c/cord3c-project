package io.cord3c.ssi.api.internal.crypto;

import com.google.common.base.Verify;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import io.cord3c.ssi.api.vc.Proof;
import io.cord3c.ssi.api.vc.SignatureVerificationException;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.api.internal.W3CHelper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.minidev.json.JSONObject;

import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.time.OffsetDateTime;
import java.util.Set;

@RequiredArgsConstructor
public class JWSCrypto {

	private final CryptoSuiteRegistry registry;

	@SneakyThrows(JOSEException.class)
	public VerifiableCredential sign(VerifiableCredential credential, ECPrivateKey privateKey) {
		return sign(credential, new ECDSASigner(privateKey));
	}

	@SneakyThrows(JOSEException.class)
	public VerifiableCredential sign(VerifiableCredential credential, JWSSigner signer) {
		Verify.verify(!credential.isSigned(), "The seems to be a proof already");
		credential.validate();

		Set<JWSAlgorithm> jwsAlgorithms = signer.supportedJWSAlgorithms();
		Verify.verify(jwsAlgorithms.size() == 1);
		JWSAlgorithm alg = jwsAlgorithms.iterator().next();

		JWSObject jwsObject = new JWSObject(new JWSHeader(alg), getPayload(credential));
		jwsObject.sign(signer);

		String proofType = registry.toProofType(alg);

		String token = jwsObject.serialize();
		Proof proof = new Proof(proofType, OffsetDateTime.now().toInstant(), W3CHelper.PROOF_PURPOSE_ASSERTION_METHOD, credential.getIssuer(), token);
		VerifiableCredential clone = credential.clone();
		clone.setProof(proof);
		return clone;
	}


	public Payload getPayload(VerifiableCredential credential) {
		VerifiableCredential vcWithoutProof = credential.clone();
		vcWithoutProof.setProof(null);
		return new Payload(vcWithoutProof.toJsonString());
	}

	@SneakyThrows
	public void verify(VerifiableCredential credential, PublicKey publicKey) {
		Verify.verify(credential.isSigned(), "The VerifiableCredential doesn't have a proof");
		credential.validate();

		Proof proof = credential.getProof();
		JWSObject jwsObject = JWSObject.parse(proof.getJws());


		JWSVerifier verifier;
		if (publicKey instanceof EdDSAPublicKey) {
			// support still a bit problematic as fairly new
			verifier = new Ed25519JavaVerifier((EdDSAPublicKey) publicKey);
		} else {
			JWSAlgorithm alg = registry.toJwsAlg(proof.getType());

			DefaultJWSVerifierFactory verifierFactory = new DefaultJWSVerifierFactory();
			JWSHeader header = new JWSHeader.Builder(alg).build();
			verifier = verifierFactory.createJWSVerifier(header, publicKey);
		}

		//JWSVerifier verifier = new ECDSAVerifier((ECPublicKey) javaPublicKey);
		if (jwsObject.verify(verifier)) {
			JSONObject jsonObject = getPayload(credential).toJSONObject();
			if (!jsonObject.equals(jwsObject.getPayload().toJSONObject())) {
				throw new SignatureVerificationException("Payloads don't match");
			}
		} else {
			throw new SignatureVerificationException("Signature verification failed");
		}
	}
}
