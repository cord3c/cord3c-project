package io.cord3c.common.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.api.internal.W3CHelper;
import lombok.experimental.UtilityClass;
import net.corda.core.internal.CordaUtilsKt;
import net.corda.testing.node.TestCordapp;
import net.corda.testing.node.internal.CustomCordapp;

import java.time.Instant;
import java.util.*;

@UtilityClass
public class VCTestUtils {

	public static VerifiableCredential generateMockCredentials() {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, JsonNode> claims = new HashMap<>();
		claims.put("hello", objectMapper.valueToTree("world"));
		claims.put(W3CHelper.CLAIM_SUBJECT_ID, objectMapper.valueToTree("did:web:mock-subject"));
		VerifiableCredential credential = new VerifiableCredential();
		credential.setContexts(Arrays.asList(W3CHelper.VC_CONTEXT_V1));
		credential.setTypes(Arrays.asList(W3CHelper.DEFAULT_VERIFIABLE_CREDENTIAL, "test"));
		credential.setId("http://mock.io/credentials/foo");
		credential.setIssuanceDate(Instant.ofEpochMilli(0));
		credential.setIssuer("did:doe");
		credential.setClaims(claims);
		return credential;
	}


	public static Collection<TestCordapp> cordapps() {
		return Collections.singletonList(
				new CustomCordapp(new HashSet<>(Arrays.asList("io.cord3c.ssi")), "mock-cordapp", 1, CordaUtilsKt.PLATFORM_VERSION, Collections.emptySet(), Collections.emptyList(), null, new HashMap<>()));
	}

}
