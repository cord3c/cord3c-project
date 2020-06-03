package io.cord3c.ssi.api;

import io.cord3c.ssi.api.resolver.DefaultUniversalResolver;
import io.cord3c.ssi.api.resolver.WebDriver;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

public class DefaultUniversalResolverTest implements WithAssertions {


	@Test
	public void verifyWebMethodSupport() {
		DefaultUniversalResolver resolver = new DefaultUniversalResolver();
		assertThat(resolver.getSupportedMethods()).contains(WebDriver.DID_METHOD);
	}

}
