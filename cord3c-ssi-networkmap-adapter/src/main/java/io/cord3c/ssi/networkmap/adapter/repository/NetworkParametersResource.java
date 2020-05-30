package io.cord3c.ssi.networkmap.adapter.repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import net.corda.core.crypto.SecureHash;
import net.corda.core.node.NodeInfo;

@Data
@JsonApiResource(type = "networkParameter")
public class NetworkParametersResource {

	@JsonApiId
	private SecureHash id;

	private Integer minimumPlatformVersion;

	private List<NodeInfo> notaries;

	private Integer maxMessageSize;

	private Integer maxTransactionSize;

	private Instant modifiedTime;

	private Integer epoch;

	private Duration eventHorizon;

}
