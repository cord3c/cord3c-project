package io.cord3c.rest.api.map;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@JsonApiResource(type = "myInfo")
@FieldNameConstants
public class MyInfoDTO {

	@JsonApiId
	private String id;

	@JsonApiRelation
	private NodeDTO node;

}
