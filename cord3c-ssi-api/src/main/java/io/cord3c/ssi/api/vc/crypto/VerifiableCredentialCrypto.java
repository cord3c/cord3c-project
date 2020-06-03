package io.cord3c.ssi.api.vc.crypto;

import com.google.common.base.Verify;
import io.cord3c.ssi.api.resolver.UniversalResolver;
import io.cord3c.ssi.api.did.DIDDocument;
import io.cord3c.ssi.api.vc.KeyFactoryHelper;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.api.vc.VerifiableCredentialES256KCrypto;
import lombok.RequiredArgsConstructor;
import net.corda.core.crypto.Base58;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

@RequiredArgsConstructor
public class VerifiableCredentialCrypto {

	private final VerifiableCredentialES256KCrypto crypto = new VerifiableCredentialES256KCrypto();

	private final UniversalResolver resolver;

	private void verify(VerifiableCredential verifiableCredential) {
		DIDDocument didDocument = resolver.resolve(verifiableCredential.getIssuer());

		Verify.verify(didDocument.getPublicKeys().size() == 1, "Assuming there is only 1 PublicKey");

		byte[] publicKeyBytes = Base58.decode(didDocument.getPublicKeys().get(0).getPublicKeyBase58());
		PublicKey publicKey = KeyFactoryHelper.constructPublicKeyFromEncodedBytes(publicKeyBytes);

		verify(verifiableCredential, publicKey);
	}

	public void verify(VerifiableCredential verifiableCredential, PublicKey publicKey) {
		crypto.verify(verifiableCredential, (ECPublicKey) publicKey);
	}

	public VerifiableCredential sign(VerifiableCredential verifiableCredential, PrivateKey privateKey) {
		return crypto.sign(verifiableCredential, (ECPrivateKey) privateKey);
	}

}
