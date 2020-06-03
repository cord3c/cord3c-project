package io.cord3c.ssi.api.vc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.base.Verify;
import com.nimbusds.jose.Payload;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.serialization.CordaSerializable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.*;

@Data
@NoArgsConstructor
@CordaSerializable
@Slf4j
public class VerifiableCredential {

	// allu/03-02-2020: don't use the default PrettyPrinter because of CR-LF issues
	// note that it is safe  to have a private mapper, no reconfiguration necessary (no new data types), mostly...
	private static ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS).findAndRegisterModules();

	@JsonProperty(value = "@context")
	private List<String> contexts;

	private String id;

	@JsonProperty(value = "type")
	private List<String> types;

	private String issuer;

	@JsonSerialize(using = ToStringSerializer.class)
	private Instant issuanceDate;

	@JsonInclude(Include.NON_NULL)
	@JsonSerialize(using = ToStringSerializer.class)
	private Instant expirationDate;

	@JsonProperty(value = "credentialSubject")
	private Map<String, JsonNode> claims;

	@JsonInclude(Include.NON_NULL)
	@Setter
	private Proof proof;


	@JsonIgnore
	public boolean isSigned() {
		return proof != null && proof.getJws() != null;
	}

	public void validate() {
		// @context
		Verify.verify(!contexts.isEmpty(), "'@context' property MUST be one or more URIs");
		Verify.verify(contexts.get(0).equals(W3CHelper.VC_CONTEXT_V1), "'@context' property's first value MUST be " + W3CHelper.VC_CONTEXT_V1);

		// id
		// MUST be a single URI (-> error will get thrown during deserialization)

		// type
		Verify.verify(!types.isEmpty(), "'type' property MUST be one or more URIs");
		Verify.verify(types.contains(W3CHelper.DEFAULT_VERIFIABLE_CREDENTIAL), "'type' property for Credential MUST be " + W3CHelper.DEFAULT_VERIFIABLE_CREDENTIAL + " plus specific type");

		// credentialSubject
		Verify.verify(getClaims().containsKey(W3CHelper.CLAIM_SUBJECT_ID), "'claims' property MUST contain '" + W3CHelper.CLAIM_SUBJECT_ID + "' property");

		// issuer
		Verify.verifyNotNull(issuer, "'issuer' property MUST be present");
		if (!issuer.contains("did:")) {
			try {
				new URL(issuer).toURI();
			} catch (MalformedURLException | URISyntaxException e) {
				if (e instanceof MalformedURLException && e.getMessage().contains("no protocol: " + issuer)) {
					try {
						UUID.fromString(issuer);
					} catch (IllegalArgumentException e2) {
						throw new IllegalStateException("'issuer' property MUST be a valid URI", e);
					}
					// FIXME allu/06-05-2020
					log.info("'issuer' property is {}, this isn't a valid format, but this only happens on systemTestALL_IN_ONE for some reason", issuer);
				} else {
					throw new IllegalStateException("'issuer' property MUST be a valid URI", e);
				}
			}
		}

		// issuanceDate
		Verify.verifyNotNull(issuanceDate, "'issuanceDate' property MUST be present");

		// proof
		if (proof != null) {
			Verify.verifyNotNull(proof.getType(), "'proof' property MUST include 'type' property");
		}
	}

	@SneakyThrows
	public void writeJsonToFile(File file) {
		mapper.writeValue(file, this);
	}

	@SneakyThrows
	public String toJsonString() {
		return mapper.writeValueAsString(this);
	}

	@SneakyThrows
	public String toPrettyJsonString() {
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
	}

	@SneakyThrows
	public static VerifiableCredential fromJson(File jsonFile) {
		VerifiableCredential parsedVC = mapper.readValue(jsonFile, VerifiableCredential.class);
		parsedVC.validate();
		return parsedVC;
	}

	@SneakyThrows
	public static VerifiableCredential fromJson(String jsonString) {
		VerifiableCredential parsedVC = mapper.readValue(jsonString, VerifiableCredential.class);
		parsedVC.validate();
		return parsedVC;
	}

	@Override
	public VerifiableCredential clone() {
		VerifiableCredential credential = new VerifiableCredential();
		credential.setProof(proof);
		credential.setIssuanceDate(issuanceDate);
		credential.setId(id);
		credential.setClaims(new HashMap<>(claims));
		credential.setIssuer(issuer);
		credential.setTypes(new ArrayList<>(types));
		credential.setContexts(new ArrayList<>(contexts));
		credential.expirationDate = expirationDate;
		return credential;
	}
}
