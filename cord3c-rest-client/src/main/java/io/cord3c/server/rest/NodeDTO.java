package io.cord3c.server.rest;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

import java.util.List;

@JsonApiResource(type = "node")
@Data
public class NodeDTO {

	@JsonApiId
	private String id;

	private List<ServerAddress> addresses;

	private List<PartyDTO> legalIdentitiesAndCerts;

	private int platformVersion;

	private long serial;
}
