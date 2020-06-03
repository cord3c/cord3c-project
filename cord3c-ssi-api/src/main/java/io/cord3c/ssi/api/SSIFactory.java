package io.cord3c.ssi.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cord3c.ssi.api.resolver.DefaultUniversalResolver;
import io.cord3c.ssi.api.vc.crypto.VerifiableCredentialCrypto;
import lombok.Getter;

@Getter
public class SSIFactory {

	private DefaultUniversalResolver resolver;

	private ObjectMapper claimMapper = new ObjectMapper();

	private VerifiableCredentialCrypto verifier;

	public SSIFactory() {
		resolver = new DefaultUniversalResolver();
		verifier = new VerifiableCredentialCrypto(resolver);
	}
}
