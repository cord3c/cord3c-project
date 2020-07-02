package io.cord3c.ssi.corda.state;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.corda.internal.information.ClaimInformation;
import io.cord3c.ssi.corda.internal.information.VerifiableCredentialInformation;
import io.cord3c.ssi.corda.internal.information.VerifiableCredentialRegistry;
import io.cord3c.ssi.corda.internal.party.PartyRegistry;
import io.cord3c.ssi.corda.state.credential.TypedCredential;
import lombok.Getter;
import lombok.SneakyThrows;

public class VCMapper {

	@Getter
	private final VerifiableCredentialRegistry registry;

	@Getter
	private ObjectMapper claimMapper = new ObjectMapper();

	public VCMapper(PartyRegistry partyRegistry) {
		this.registry = new VerifiableCredentialRegistry(partyRegistry);
	}

	public VerifiableCredential toCredential(Object state) {
		VerifiableCredentialInformation information = registry.get(state.getClass());

		claimMapper.findAndRegisterModules();

		if (information.getJsonAccessor() != null) {
			String json = information.getJsonAccessor().getValue(state);
			if (json != null) {
				return VerifiableCredential.fromJson(json);
			}
		}

		VerifiableCredential credential = new VerifiableCredential();
		addTypes(credential, information, state);
		credential.setId(information.getIdAccessor().getValue(state));
		credential.getContexts().addAll(information.getContexts());
		credential.setIssuanceDate(information.getTimestampAccessor().getValue(state));
		credential.setIssuer(information.getIssuerAccessor().getValue(state));
		credential.setClaims(toClaims(information, state));
		return credential;
	}

	@SneakyThrows
	public <T> T fromCredential(VerifiableCredential credential) {
		VerifiableCredentialInformation information = registry.get(credential.getTypes());

		Object state = information.getImplementationType().newInstance();

		information.getTimestampAccessor().setValue(state, credential.getIssuanceDate());
		information.getIdAccessor().setValue(state, credential.getId());
		information.getIssuerAccessor().setValue(state, credential.getIssuer());
		if (state instanceof TypedCredential) {
			((TypedCredential) state).setTypes(credential.getTypes());
		}

		for (Map.Entry<String, JsonNode> claim : credential.getClaims().entrySet()) {
			ClaimInformation claimInformation = information.getClaims().get(claim.getKey());

			ObjectReader valueReader = claimMapper.readerFor(claimInformation.getAccessor().getImplementationClass());
			Object value = valueReader.readValue(claim.getValue());
			claimInformation.getAccessor().setValue(state, value);
		}

		if (information.getJsonAccessor() != null) {
			information.getJsonAccessor().setValue(state, credential.toJsonString());
		}

		return (T) state;
	}

	private void addTypes(VerifiableCredential credential, VerifiableCredentialInformation information, Object state) {
		credential.getTypes().addAll(information.getTypes());
		if (state instanceof TypedCredential) {
			credential.getTypes().addAll(((TypedCredential) state).getTypes());
		}
	}

	private Map<String, JsonNode> toClaims(VerifiableCredentialInformation information, Object state) {
		Map<String, JsonNode> map = new LinkedHashMap<>();

		for (ClaimInformation claimInformation : information.getClaims().values()) {
			Object value = claimInformation.getAccessor().getValue(state);
			if (value != null) {
				JsonNode valueNode = claimMapper.valueToTree(value);
				if (valueNode != null) {
					map.put(claimInformation.getJsonName(), valueNode);
				}
			}
		}
		return map;
	}
}
