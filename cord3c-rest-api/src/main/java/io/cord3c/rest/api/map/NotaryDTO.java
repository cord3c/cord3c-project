package io.cord3c.rest.api.map;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@JsonApiResource(type = "notary")
@Data
@FieldNameConstants
public class NotaryDTO {

	@JsonApiId
	private String id;

	@JsonApiRelation
	private PartyDTO identity;

	private boolean validating;
}
