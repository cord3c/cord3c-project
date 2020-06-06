package io.cord3c.ssi.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cord3c.ssi.api.resolver.DefaultUniversalResolver;
import io.cord3c.ssi.api.vc.VCCrypto;
import lombok.Getter;

@Getter
public class SSIFactory {

	private DefaultUniversalResolver resolver;

	private ObjectMapper claimMapper = new ObjectMapper();

	private VCCrypto crypto;

	public SSIFactory() {
		resolver = new DefaultUniversalResolver();
		crypto = new VCCrypto(resolver);
	}
}
