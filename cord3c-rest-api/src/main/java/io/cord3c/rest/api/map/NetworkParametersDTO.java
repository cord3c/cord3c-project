package io.cord3c.rest.api.map;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Data
@JsonApiResource(type = "networkParameter")
@FieldNameConstants
public class NetworkParametersDTO {

	@JsonApiId
	private String id;

	private Integer minimumPlatformVersion;

	@JsonApiRelation
	private List<NotaryDTO> notaries;

	private Integer maxMessageSize;

	private Integer maxTransactionSize;

	private Instant modifiedTime;

	private Integer epoch;

	private Duration eventHorizon;

}
