package io.cord3c.rest.client;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

@JsonApiResource(type = "notary")
@Data
public class NotaryDTO {

	@JsonApiId
	private String id;

	@JsonApiRelation
	private PartyDTO party;
}
