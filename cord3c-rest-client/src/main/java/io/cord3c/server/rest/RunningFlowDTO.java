package io.cord3c.server.rest;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

import java.util.List;

@JsonApiResource(type = "flow")
@Data
public class RunningFlowDTO {

	@JsonApiId
	private String id;

}
