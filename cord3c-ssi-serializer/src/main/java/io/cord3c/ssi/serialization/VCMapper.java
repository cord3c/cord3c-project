package io.cord3c.ssi.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.api.vc.W3CHelper;
import io.cord3c.ssi.serialization.credential.TypedCredential;
import io.cord3c.ssi.serialization.internal.information.ClaimInformation;
import io.cord3c.ssi.serialization.internal.information.VerifiableCredentialInformation;
import io.cord3c.ssi.serialization.internal.information.VerifiableCredentialRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.bytebuddy.implementation.bytecode.TypeCreation;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class VCMapper {

	private final VerifiableCredentialRegistry registry;

	private final ObjectMapper claimMapper;

	public VerifiableCredential toCredential(Object state) {
		VerifiableCredentialInformation information = registry.get(state.getClass());

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
