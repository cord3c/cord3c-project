package io.cord3c.ssi.api;

import io.cord3c.ssi.api.vc.KeyFactoryHelper;
import io.cord3c.ssi.api.vc.SignatureVerificationException;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@TestInstance(Lifecycle.PER_CLASS)
public class VerifiableCredentialTest implements WithAssertions {

	private ECPublicKey publicKey;

	private ECPrivateKey privateKey;

	@BeforeAll
	public void setup() {
		KeyPair keyPair = KeyFactoryHelper.generateKeyPair();
		publicKey = (ECPublicKey) keyPair.getPublic();
		privateKey = (ECPrivateKey) keyPair.getPrivate();
	}

	@Test
	public void encodeAndVerifyVerifiableCredential() {
		VerifiableCredential verifiableCredential = generateSignedNetworkMembershipRoleVerifiableCredential();

		assertThatCode(() -> verifiableCredential.verify(publicKey)).doesNotThrowAnyException();
	}

	@Test
	public void encodeAndVerifyVerifiableCredentialWithWrongPublicKey() {
		VerifiableCredential verifiableCredential = generateSignedNetworkMembershipRoleVerifiableCredential();

		ECPublicKey someOtherPublicKey = (ECPublicKey) KeyFactoryHelper.generateKeyPair().getPublic();

		assertThatThrownBy(() -> verifiableCredential.verify(someOtherPublicKey)).isExactlyInstanceOf(SignatureVerificationException.class).hasMessageContaining("Signature verification failed");
	}

	@Test
	public void encodeTamperAndVerifyVerifiableCredentialFails() {
		VerifiableCredential verifiableCredential = generateSignedNetworkMembershipRoleVerifiableCredential();

		verifiableCredential.getClaims().put("hello", "new world");

		assertThatThrownBy(() -> verifiableCredential.verify(publicKey)).isExactlyInstanceOf(SignatureVerificationException.class).hasMessageContaining("Payloads don't match");
	}

	private VerifiableCredential generateSignedNetworkMembershipRoleVerifiableCredential() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("hello", "world");
		VerifiableCredential credential = new VerifiableCredential();
		credential.setTypes(Arrays.asList("test"));
		credential.setId("mock-id");
		credential.setClaims(claims);
		return credential.sign(privateKey);
	}

}
