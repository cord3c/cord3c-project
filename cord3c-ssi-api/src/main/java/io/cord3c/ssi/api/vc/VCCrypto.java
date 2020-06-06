package io.cord3c.ssi.api.vc;

import com.google.common.base.Verify;
import com.nimbusds.jose.JWSSigner;
import io.cord3c.ssi.api.did.Authentication;
import io.cord3c.ssi.api.did.DIDDocument;
import io.cord3c.ssi.api.did.DIDPublicKey;
import io.cord3c.ssi.api.internal.crypto.CryptoSuiteRegistry;
import io.cord3c.ssi.api.internal.crypto.JWSCrypto;
import io.cord3c.ssi.api.resolver.UniversalResolver;
import lombok.RequiredArgsConstructor;
import net.corda.core.crypto.Base58;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.util.Arrays;

@RequiredArgsConstructor
public class VCCrypto {

	private final CryptoSuiteRegistry registry = new CryptoSuiteRegistry();

	private final JWSCrypto crypto = new JWSCrypto(registry);

	private final UniversalResolver resolver;

	public KeyPair generateKeyPair() {
		return registry.generateKeyPair();
	}

	public void verify(VerifiableCredential verifiableCredential) {
		DIDDocument didDocument = resolver.resolve(verifiableCredential.getIssuer());
		Verify.verify(didDocument.getPublicKeys().size() == 1, "Assuming there is only 1 PublicKey");
		PublicKey publicKey = didDocument.getPublicKeys().get(0).decode();
		verify(verifiableCredential, publicKey);
	}

	public void verify(VerifiableCredential verifiableCredential, PublicKey publicKey) {
		crypto.verify(verifiableCredential, publicKey);
	}

	public Authentication toAuthentication(DIDPublicKey publicKey) {
		String proofType = registry.toProofType(publicKey);
		return new Authentication(proofType, Arrays.asList(publicKey.getId()));
	}

	public DIDPublicKey toDidPublicKey(java.security.PublicKey publicKey, String did) {
		return toDidPublicKey(publicKey, did, "keys-1");
	}

	public DIDPublicKey toDidPublicKey(java.security.PublicKey publicKey, String did, String name) {
		String publicKeyBase58 = Base58.encode(publicKey.getEncoded());
		String keyType = registry.toKeyType(publicKey);
		return new DIDPublicKey(did + "#" + name, did, keyType, publicKeyBase58);
	}

	public VerifiableCredential sign(VerifiableCredential verifiableCredential, PrivateKey privateKey) {
		return crypto.sign(verifiableCredential, (ECPrivateKey) privateKey);
	}

	public VerifiableCredential sign(VerifiableCredential verifiableCredential, JWSSigner signer) {
		return crypto.sign(verifiableCredential, signer);
	}

}
