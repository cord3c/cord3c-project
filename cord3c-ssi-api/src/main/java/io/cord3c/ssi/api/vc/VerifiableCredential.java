package io.cord3c.ssi.api.vc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.base.Verify;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.serialization.CordaSerializable;
import net.minidev.json.JSONObject;

@Data
@NoArgsConstructor
@CordaSerializable
@Slf4j
public class VerifiableCredential {

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
	private Map<String, Object> claims;

	@JsonInclude(Include.NON_NULL)
	@Setter
	private Proof proof;

	// allu/03-02-2020: don't use the default PrettyPrinter because of CR-LF issues
	private static ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS).findAndRegisterModules();

	public VerifiableCredential(List<String> contexts, String id, List<String> types, String issuer, Instant issuanceDate, Map<String, Object> claims, Proof proof) {
		this.contexts = contexts;
		this.id = id;
		this.types = types;
		this.issuer = issuer;
		this.issuanceDate = issuanceDate;
		this.claims = claims;
		this.proof = proof;
		checkVerifiableCredential(this);
	}

	@JsonIgnore
	public boolean isSigned() {
		return proof != null && proof.getJws() != null;
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
		checkVerifiableCredential(parsedVC);
		return parsedVC;
	}

	@SneakyThrows
	public static VerifiableCredential fromJson(String jsonString) {
		VerifiableCredential parsedVC = mapper.readValue(jsonString, VerifiableCredential.class);
		checkVerifiableCredential(parsedVC);
		return parsedVC;
	}

	private static void checkVerifiableCredential(VerifiableCredential verifiableCredential) {
		// @context
		Verify.verify(!verifiableCredential.getContexts().isEmpty(), "'@context' property MUST be one or more URIs");
		Verify.verify(verifiableCredential.getContexts().get(0).equals(W3CHelper.DEFAULT_VC_CONTEXT_1), "'@context' property's first value MUST be " + W3CHelper.DEFAULT_VC_CONTEXT_1);

		// id
		// MUST be a single URI (-> error will get thrown during deserialization)

		// type
		Verify.verify(!verifiableCredential.getTypes().isEmpty(), "'type' property MUST be one or more URIs");
		Verify.verify(verifiableCredential.getTypes().contains(W3CHelper.DEFAULT_VERIFIABLE_CREDENTIAL), "'type' property for Credential MUST be " + W3CHelper.DEFAULT_VERIFIABLE_CREDENTIAL + " plus specific type");

		// credentialSubject
		Verify.verify(verifiableCredential.getClaims().containsKey(W3CHelper.CLAIM_SUBJECT_ID), "'credentialSubject' property MUST contain 'id' property");

		// issuer
		Verify.verifyNotNull(verifiableCredential.getIssuer(), "'issuer' property MUST be present");
		String issuer = verifiableCredential.getIssuer();
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
					log.info("'issuer' property is {}, this isn't a valid format, but this only happens on systemTestALL_IN_ONE for some reason", verifiableCredential.getIssuer());
				} else {
					throw new IllegalStateException("'issuer' property MUST be a valid URI", e);
				}
			}
		}

		// issuanceDate
		Verify.verifyNotNull(verifiableCredential.getIssuanceDate(), "'issuanceDate' property MUST be present");

		// proof
		if (verifiableCredential.getProof() != null) {
			Verify.verifyNotNull(verifiableCredential.getProof().getType(), "'proof' property MUST include 'type' property");
		}
	}

	@SneakyThrows(JOSEException.class)
	public VerifiableCredential sign(ECPrivateKey privateKey) {
		Verify.verify(!isSigned(), "The seems to be a proof already");
		checkVerifiableCredential(this);

		JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.ES256K), getPayload());
		jwsObject.sign(new ECDSASigner(privateKey));

		String token = jwsObject.serialize();

		proof = new Proof(proof.getType(), OffsetDateTime.now().toInstant(), W3CHelper.PROOF_PURPOSE_ASSERTION_METHOD, issuer, token);

		return this;
	}

	@SneakyThrows({ParseException.class, JOSEException.class})
	public void verify(ECPublicKey publicKey) {
		Verify.verify(isSigned(), "The VerifiableCredential doesn't have a proof");
		checkVerifiableCredential(this);

		JWSObject jwsObject = JWSObject.parse(proof.getJws());

		if (jwsObject.verify(new ECDSAVerifier(publicKey))) {
			JSONObject jsonObject = getPayload().toJSONObject();
			if (!jsonObject.equals(jwsObject.getPayload().toJSONObject())) {
				throw new SignatureVerificationException("Payloads don't match");
			}
		} else {
			throw new SignatureVerificationException("Signature verification failed");
		}
	}

	private Payload getPayload() {
		// allu/30-04-2020: to generate the payload, we remove the verifiable credential's 'proof' object
		VerifiableCredential vcWithoutProof = new VerifiableCredential(contexts, id, types, issuer, issuanceDate, claims, null);
		return new Payload(vcWithoutProof.toJsonString());
	}
}
