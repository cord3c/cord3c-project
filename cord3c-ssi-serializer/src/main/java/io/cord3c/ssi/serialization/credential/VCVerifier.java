package io.cord3c.ssi.serialization.credential;

import com.google.common.base.Verify;
import io.cord3c.ssi.api.vc.DIDDocument;
import io.cord3c.ssi.api.vc.KeyFactoryHelper;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import net.corda.core.crypto.Base58;

import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;

public class VCVerifier {

	public static void verify(VerifiableCredential verifiableCredential) throws Exception {
		DefaultUniversalResolver resolver = new DefaultUniversalResolver(true);
		verify(resolver, verifiableCredential);
	}

	public static void verifyForTestPR(VerifiableCredential verifiableCredential) throws Exception {
		DefaultUniversalResolver resolver = new DefaultUniversalResolver(false);
		verify(resolver, verifiableCredential);
	}

	private static void verify(UniversalResolver resolver, VerifiableCredential verifiableCredential) {
		DIDDocument didDocument = resolver.resolve(verifiableCredential.getIssuer());

		Verify.verify(didDocument.getPublicKeys().size() == 1, "Assuming there is only 1 PublicKey");

		byte[] publicKeyBytes = Base58.decode(didDocument.getPublicKeys().get(0).getPublicKeyBase58());
		PublicKey publicKey = KeyFactoryHelper.constructPublicKeyFromEncodedBytes(publicKeyBytes);

		verify(verifiableCredential, publicKey);
	}

	public static void verify(VerifiableCredential verifiableCredential, PublicKey publicKey) {
		verifiableCredential.verify((ECPublicKey) publicKey);
	}

}
