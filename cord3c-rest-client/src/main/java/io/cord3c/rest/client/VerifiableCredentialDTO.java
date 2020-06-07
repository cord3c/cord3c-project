package io.cord3c.rest.client;

import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@JsonApiResource(type = "verifiableCredential")
@FieldNameConstants
@NoArgsConstructor
public class VerifiableCredentialDTO {

	@JsonApiId
	private String id;

	private VerifiableCredential credential;

	public VerifiableCredentialDTO(VerifiableCredential credential) {
		setCredential(credential);
		setId(credential.toHashId());
	}
}
