package io.cord3c.ssi.api.vc;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Verify;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * For the moment, we mostly use {@link VerifiableCredential}.
 * <p>
 * However, this class was created to pass more tests in the vc-test-suite.
 * <p>
 * One could change how we sign {@link VerifiableCredential} and instead sign {@link VerifiablePresentation}. Or have the option to do either.
 */
@Data
@NoArgsConstructor
public class VerifiablePresentation {

	@JsonProperty(value = "@context")
	private List<String> contexts;

	@JsonInclude(Include.NON_NULL)
	private String id;

	@JsonProperty(value = "type")
	private List<String> types;

	@JsonProperty(value = "verifiableCredential")
	private List<VerifiableCredential> verifiableCredentials;

	@JsonProperty(value = "proof")
	private PresentationProof presentationProof;

	// allu/03-02-2020: don't use the default PrettyPrinter because of CR-LF issues
	private static ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS).findAndRegisterModules();

	public VerifiablePresentation(List<String> contexts, String id, List<String> types, List<VerifiableCredential> verifiableCredentials, PresentationProof presentationProof) {
		this.contexts = contexts;
		this.id = id;
		this.types = types;
		this.verifiableCredentials = verifiableCredentials;
		this.presentationProof = presentationProof;
		checkVerifiablePresentation(this);
	}

	@JsonIgnore
	public boolean isSigned() {
		return presentationProof != null && presentationProof.getJws() != null;
	}

	@SneakyThrows
	public static VerifiablePresentation fromJson(String jsonString) {
		VerifiablePresentation parsedVP = mapper.readValue(jsonString, VerifiablePresentation.class);
		checkVerifiablePresentation(parsedVP);
		return parsedVP;
	}

	@SneakyThrows
	public String toJsonString() {
		return mapper.writeValueAsString(this);
	}

	public VerifiablePresentation sign(ECPrivateKey privateKey) {
		// TODO allu/28-04-2020: since we're not needing this now, I leave unimplemented
		throw new UnsupportedOperationException();
	}

	public void verify(ECPublicKey publicKey) {
		// TODO allu/28-04-2020: since we're not needing this now, I leave unimplemented
		throw new UnsupportedOperationException();
	}

	private static void checkVerifiablePresentation(VerifiablePresentation verifiablePresentation) {
		// @context
		Verify.verify(!verifiablePresentation.getContexts().isEmpty(), "'@context' property MUST be one or more URIs");
		Verify.verify(verifiablePresentation.getContexts().get(0).equals(W3CHelper.DEFAULT_VC_CONTEXT_1), "'@context' property's first value MUST be " + W3CHelper.DEFAULT_VC_CONTEXT_1);

		// type
		Verify.verify(!verifiablePresentation.getTypes().isEmpty(), "'type' property MUST be one or more URIs");
		Verify.verify(verifiablePresentation.getTypes().contains(W3CHelper.DEFAULT_VERIFIABLE_PRESENTATION), "'type' property for Presentation MUST be " + W3CHelper.DEFAULT_VERIFIABLE_CREDENTIAL + " plus specific type");

		// verifiableCredential
		Verify.verify(!verifiablePresentation.getVerifiableCredentials().isEmpty(), "'verifiableCredential' property MUST be one or more VerifiableCredentials");

		// proof
		Verify.verifyNotNull(verifiablePresentation.getPresentationProof(), "'proof' property MUST be present");
		Verify.verifyNotNull(verifiablePresentation.getPresentationProof().getType(), "'proof' property MUST include 'type' property");
	}

}
