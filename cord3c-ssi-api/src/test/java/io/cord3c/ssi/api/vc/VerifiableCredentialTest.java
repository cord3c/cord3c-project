package io.cord3c.ssi.api.vc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class VerifiableCredentialTest implements WithAssertions {

	@Test
	public void verifyHashLink() {
		VerifiableCredential credential = generateMockCredentials();
		String link = credential.toHashLink();

		// we have a hash here to ensure serialization does not change, otherwise
		// when you have to update this, make sure to maintain backward compatibility
		assertThat(link).isEqualTo("http://mock.io/credentials/foo?hl=zQmQ9xCX64zLibHoJGafzR9vzmGySrgKZmomYQVCoNQxYhC");
	}

	private VerifiableCredential generateMockCredentials() {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, JsonNode> claims = new HashMap<>();
		claims.put("hello", objectMapper.valueToTree("world"));
		VerifiableCredential credential = new VerifiableCredential();
		credential.setContexts(Arrays.asList(W3CHelper.VC_CONTEXT_V1));
		credential.setTypes(Arrays.asList(W3CHelper.DEFAULT_VERIFIABLE_CREDENTIAL, "test"));
		credential.setId("http://mock.io/credentials/foo");
		credential.setIssuanceDate(Instant.ofEpochMilli(0));
		credential.setIssuer("did:doe");
		credential.setClaims(claims);
		return credential;
	}
}
