package io.cord3c.rest.client;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

@Data
@JsonApiResource(type = "party")
public class PartyDTO {

	@JsonApiId
	private String id;

	private X500Name name;

	// val certificate: X509Certificate

	// val owningKey: PublicKey get() = party.owningKey
}
