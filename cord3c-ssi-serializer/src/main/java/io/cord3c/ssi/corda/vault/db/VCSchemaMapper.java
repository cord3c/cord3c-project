package io.cord3c.ssi.corda.vault.db;

import com.fasterxml.jackson.databind.JsonNode;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Mapper
public abstract class VCSchemaMapper {


	@Mapping(target = "claims", expression = "java(toClaimEntity(entity, credential))")
	@Mapping(target = "jsonValue", expression = "java(credential.toJsonString())")
	public abstract void toEntity(@MappingTarget CredentialEntity entity, VerifiableCredential credential);

	/*@InheritInverseConfiguration
	@Mapping(target = "claims", expression = "java(fromClaimEntity(entity))")
	@Mapping(target = "fromJson", ignore = true)
	public abstract VerifiableCredential fromEntity(CredentialEntity entity);
	 */

	public VerifiableCredential fromEntity(CredentialEntity entity) {
		if (entity == null) {
			return null;
		}
		return VerifiableCredential.fromJson(entity.getJsonValue());
	}

	protected Map<String, ClaimEntity> toClaimEntity(CredentialEntity credentialEntity, VerifiableCredential credential) {
		Map<String, ClaimEntity> map = credentialEntity.getClaims();
		if (map == null) {
			map = new HashMap<>();
		}

		Map<String, ClaimEntity> existing = new HashMap<>(map);
		map.clear();

		for (Map.Entry<String, JsonNode> entry : credential.getClaims().entrySet()) {
			ClaimEntity entity = existing.get(entry.getKey());
			if (entity == null) {
				ClaimId id = new ClaimId();
				id.setClaimId(credential.getId());
				id.setName(entry.getKey());

				entity = new ClaimEntity();
				entity.setId(id);
			}
			entity.setValue(entry.getValue());
			map.put(entry.getKey(), entity);
		}
		return map;
	}

	protected Map<String, JsonNode> fromClaimEntity(CredentialEntity entity) {
		Map<String, JsonNode> claims = new LinkedHashMap<>();
		for (ClaimEntity claimEntity : entity.getClaims().values()) {
			claims.put(claimEntity.getId().getName(), claimEntity.getValue());
		}
		return claims;
	}
}
