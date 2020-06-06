package io.cord3c.ssi.corda;

import com.google.auto.service.AutoService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jca.JCAContext;
import com.nimbusds.jose.util.Base64URL;
import io.cord3c.ssi.api.resolver.DefaultUniversalResolver;
import io.cord3c.ssi.api.resolver.UniversalResolver;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.api.vc.VCCrypto;
import io.cord3c.ssi.corda.vault.VCVault;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.node.services.KeyManagementService;
import net.corda.core.serialization.SingletonSerializeAsToken;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;

@CordaService
@Slf4j
@AutoService(SingletonSerializeAsToken.class)
public class VCService extends SingletonSerializeAsToken {

	private final AppServiceHub serviceHub;

	@Getter
	private final VCVault vault;

	@Getter
	private final UniversalResolver univeralResolver;

	@Getter
	private final VCCrypto crypto;

	public VCService(AppServiceHub serviceHub) {
		this.serviceHub = serviceHub;
		this.vault = new VCVault(serviceHub);
		this.univeralResolver = new DefaultUniversalResolver();
		this.crypto = new VCCrypto(univeralResolver);
	}

	public VerifiableCredential sign(VerifiableCredential verifiableCredential) {
		PublicKey publicKey = getIdentityKey().getPublicKey();
		return crypto.sign(verifiableCredential, new CordaJwsSigner(publicKey));
	}

	public X509Certificate getIdentityKey() {
		return serviceHub.getMyInfo().getLegalIdentitiesAndCerts().get(0).getCertificate();
	}

	/**
	 * Forwards the actual JWS signing to the key management service of Corda.
	 */
	@RequiredArgsConstructor
	public class CordaJwsSigner implements JWSSigner {

		private final JCAContext jcaContext = new JCAContext();

		private final PublicKey publicKey;

		@Override
		public Base64URL sign(JWSHeader header, byte[] signingInput) throws JOSEException {
			final JWSAlgorithm alg = header.getAlgorithm();
			if (!JWSAlgorithm.EdDSA.equals(alg)) {
				throw new JOSEException("Ed25519Signer requires alg=EdDSA in JWSHeader");
			}

			KeyManagementService keyManagementService = serviceHub.getKeyManagementService();
			byte[] jwsSignature = keyManagementService.sign(signingInput, publicKey).copyBytes();
			return Base64URL.encode(jwsSignature);
		}

		@Override
		public Set<JWSAlgorithm> supportedJWSAlgorithms() {
			return Collections.singleton(JWSAlgorithm.EdDSA);
		}

		@Override
		public JCAContext getJCAContext() {
			return jcaContext;
		}
	}

	@SneakyThrows
	public void verify(VerifiableCredential verifiableCredential) {
		crypto.verify(verifiableCredential);
	}

	@SneakyThrows
	public void verify(VerifiableCredential verifiableCredential, PublicKey publicKey) {
		crypto.verify(verifiableCredential, publicKey);
	}

}
