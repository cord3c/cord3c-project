package io.cord3c.ssi.api.internal.crypto;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.impl.CriticalHeaderParamsDeferral;
import com.nimbusds.jose.crypto.impl.EdDSAProvider;
import com.nimbusds.jose.util.Base64URL;
import lombok.SneakyThrows;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;

import java.security.MessageDigest;
import java.security.Signature;
import java.util.Set;

/**
 * Based on https://github.com/str4d/ed25519-java. A bit complicated as no widepsread support yet for Ed22519.
 * Corda nd jose make use of different implementations.
 */
public class Ed25519JavaVerifier extends EdDSAProvider implements JWSVerifier, CriticalHeaderParamsAware {


	private final EdDSAPublicKey publicKey;

	private final CriticalHeaderParamsDeferral critPolicy = new CriticalHeaderParamsDeferral();


	public Ed25519JavaVerifier(final EdDSAPublicKey publicKey)
			throws JOSEException {

		super();

		/* if (!Curve.Ed25519.equals(publicKey.getCurve())) {
			throw new JOSEException("Ed25519Verifier only supports OctetKeyPairs with crv=Ed25519");
		}

		if (publicKey.isPrivate()) {
			throw new JOSEException("Ed25519Verifier requires a public key, use OctetKeyPair.toPublicJWK()");
		}(*/

		this.publicKey = publicKey;
		//	tinkVerifier = new Ed25519Verify(publicKey.getDecodedX());
		//critPolicy.setDeferredCriticalHeaderParams(defCritHeaders);
	}


	/**
	 * Returns the public key.
	 *
	 * @return An OctetKeyPair without the private part
	 */
	public EdDSAPublicKey getPublicKey() {

		return publicKey;
	}


	@Override
	public Set<String> getProcessedCriticalHeaderParams() {

		return critPolicy.getProcessedCriticalHeaderParams();
	}


	@Override
	public Set<String> getDeferredCriticalHeaderParams() {

		return critPolicy.getProcessedCriticalHeaderParams();
	}


	@Override
	@SneakyThrows
	public boolean verify(final JWSHeader header,
						  final byte[] signedContent,
						  final Base64URL signature)
			throws JOSEException {

		// Check alg field in header
		final JWSAlgorithm alg = header.getAlgorithm();
		if (!JWSAlgorithm.EdDSA.equals(alg)) {
			throw new JOSEException("Ed25519Verifier requires alg=EdDSA in JWSHeader");
		}

		// Check for unrecognized "crit" properties
		//if (!critPolicy.headerPasses(header)) {
		//		return false;
		//	}

		final byte[] jwsSignature = signature.decode();

		Signature sgr = new EdDSAEngine(MessageDigest.getInstance("SHA-512"));
		sgr.initVerify(publicKey);
		sgr.update(signedContent);
		return sgr.verify(jwsSignature);


	}
}
