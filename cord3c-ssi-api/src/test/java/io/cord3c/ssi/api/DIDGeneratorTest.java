package io.cord3c.ssi.api;

import io.cord3c.ssi.api.internal.DIDGenerator;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

public class DIDGeneratorTest implements WithAssertions {

	private static final String TEST_DOMAIN = "test";

	@Test
	public void generateRandomDidTest() {
		String did = DIDGenerator.generateRandomDid(TEST_DOMAIN);

		assertThat(did.startsWith(DIDGenerator.DID_WEB_PREFIX)).isTrue();
	}
}
