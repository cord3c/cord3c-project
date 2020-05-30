package io.cord3c.ssi.api;

import java.util.Random;

import io.cord3c.ssi.api.internal.DIDGenerator;
import net.corda.core.identity.CordaX500Name;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

public class DIDGeneratorTest implements WithAssertions {

	private static final String TEST_DOMAIN = "test";

	@Test
	public void generateRandomDidTest() {
		String did = DIDGenerator.generateRandomDid(TEST_DOMAIN);

		assertThat(did.startsWith(DIDGenerator.DID_WEB_PREFIX)).isTrue();
	}

	@Test
	public void cordaX500NameTest() {
		CordaX500Name name = new CordaX500Name("imdb", "Zurich", "CH");
		String did = DIDGenerator.getDidFromCordaX500Name(name, TEST_DOMAIN);

		assertThat(did.startsWith(DIDGenerator.DID_WEB_PREFIX)).isTrue();
	}

	@Test
	public void cordaX500NameSimilarTest() {
		CordaX500Name name = new CordaX500Name("imdb", "Zurich", "CH");
		String did = DIDGenerator.getDidFromCordaX500Name(name, TEST_DOMAIN);

		CordaX500Name name2 = new CordaX500Name("imdb2", "Zurich", "CH");
		String did2 = DIDGenerator.getDidFromCordaX500Name(name2, TEST_DOMAIN);

		assertThat(did).isNotEqualTo(did2);
	}

	@Test
	public void cordaX500NameReproducibleTest() {
		CordaX500Name name = new CordaX500Name("imdb", "Zurich", "CH");
		String did = DIDGenerator.getDidFromCordaX500Name(name, TEST_DOMAIN);

		String didTest;
		for (int i = 0; i < 100; ++i) {
			didTest = DIDGenerator.getDidFromCordaX500Name(name, TEST_DOMAIN);
			assertThat(didTest).isEqualTo(did);
		}
	}

	@Test
	public void getDidFromStringLengthTest() {
		for (int i = 0; i < 1000000; ++i) {
			int leftLimit = 0;
			int rightLimit = 255;
			int targetStringLength = i % 50;
			Random random = new Random();
			StringBuilder buffer = new StringBuilder(targetStringLength);
			for (int j = 0; j < targetStringLength; j++) {
				int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
				buffer.append((char) randomLimitedInt);
			}
			String generatedString = buffer.toString();

			String did = DIDGenerator.getDidFromString(generatedString, TEST_DOMAIN);
			assertThat(did.length()).isEqualTo(DIDGenerator.DID_WEB_PREFIX.length() + TEST_DOMAIN.length() + 2 + 64);
		}
	}

}
