package io.cord3c.rest.client.map;

import io.cord3c.rest.client.ServerAddress;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@JsonApiResource(type = "nodeInfo")
@Data
@FieldNameConstants
public class NodeDTO {

	@JsonApiId
	private String id;

	private List<ServerAddress> addresses;

	@JsonApiRelation
	private List<PartyDTO> legalIdentitiesAndCerts;

	private int platformVersion;

	private long serial;
}
