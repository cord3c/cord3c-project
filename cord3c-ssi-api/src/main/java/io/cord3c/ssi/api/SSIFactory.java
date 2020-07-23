package io.cord3c.ssi.api;

import io.cord3c.ssi.api.resolver.DefaultUniversalResolver;
import io.cord3c.ssi.api.vc.VCCrypto;
import lombok.Getter;

@Getter
public class SSIFactory {

	private DefaultUniversalResolver resolver;

	private VCCrypto crypto;

	public SSIFactory() {
		resolver = new DefaultUniversalResolver();
		crypto = new VCCrypto(resolver);
	}
}
