package io.cord3c.ssi.serialization.internal.mapper;

import com.google.common.base.Verify;
import io.cord3c.ssi.api.vc.Proof;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.api.vc.W3CHelper;

import java.time.Instant;

public class VerfiableCredentialSigner {


	public static VerifiableCredential addJwsProof(VerifiableCredential verifiableCredential, Instant timestamp, String jwsToken) {
		Verify.verify(!verifiableCredential.isSigned(), "Verifiable Credential is already signed");

		Proof proof = new Proof(W3CHelper.JsonWebSignature2020, timestamp, W3CHelper.PROOF_PURPOSE_ASSERTION_METHOD, verifiableCredential.getIssuer(), jwsToken);

		verifiableCredential.setProof(proof);
		return verifiableCredential;
	}
}
