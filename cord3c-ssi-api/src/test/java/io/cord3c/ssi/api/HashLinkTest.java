package io.cord3c.ssi.api;

import io.cord3c.ssi.api.internal.hashlink.HashLink;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

public class HashLinkTest implements WithAssertions {

	@Test
	public void test() {
		// verifies example of https://tools.ietf.org/html/draft-sporny-hashlink-04
		assertThat(HashLink.create("Hello World!")).isEqualTo("zQmWvQxTqbG2Z9HPJgG57jjwR154cKhbtJenbyYTWkjgF3e");
	}
}
