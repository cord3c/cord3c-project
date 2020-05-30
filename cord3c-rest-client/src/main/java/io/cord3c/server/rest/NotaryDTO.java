package io.cord3c.server.rest;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

@JsonApiResource(type = "notary")
@Data
public class NotaryDTO {

	@JsonApiId
	private String id;

}
