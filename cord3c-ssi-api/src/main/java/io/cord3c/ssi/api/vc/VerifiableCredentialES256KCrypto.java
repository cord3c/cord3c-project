package io.cord3c.ssi.api.vc;

import com.google.common.base.Verify;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import lombok.SneakyThrows;
import net.minidev.json.JSONObject;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.time.OffsetDateTime;

public class VerifiableCredentialES256KCrypto {

	@SneakyThrows(JOSEException.class)
	public VerifiableCredential sign(VerifiableCredential credential, ECPrivateKey privateKey) {
		Verify.verify(!credential.isSigned(), "The seems to be a proof already");
		credential.validate();

		JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.ES256K), getPayload(credential));
		jwsObject.sign(new ECDSASigner(privateKey));

		String token = jwsObject.serialize();


		Proof proof = new Proof(W3CHelper.SECP256K1_SIGNATURE, OffsetDateTime.now().toInstant(), W3CHelper.PROOF_PURPOSE_ASSERTION_METHOD, credential.getIssuer(), token);

		VerifiableCredential clone = credential.clone();
		clone.setProof(proof);
		return clone;
	}


	public Payload getPayload(VerifiableCredential credential) {
		VerifiableCredential vcWithoutProof = credential.clone();
		vcWithoutProof.setProof(null);
		return new Payload(vcWithoutProof.toJsonString());
	}

	@SneakyThrows({ParseException.class, JOSEException.class})
	public void verify(VerifiableCredential credential, ECPublicKey publicKey) {
		Verify.verify(credential.isSigned(), "The VerifiableCredential doesn't have a proof");
		credential.validate();

		Proof proof = credential.getProof();
		JWSObject jwsObject = JWSObject.parse(proof.getJws());

		if (jwsObject.verify(new ECDSAVerifier(publicKey))) {
			JSONObject jsonObject = getPayload(credential).toJSONObject();
			if (!jsonObject.equals(jwsObject.getPayload().toJSONObject())) {
				throw new SignatureVerificationException("Payloads don't match");
			}
		} else {
			throw new SignatureVerificationException("Signature verification failed");
		}
	}
}
