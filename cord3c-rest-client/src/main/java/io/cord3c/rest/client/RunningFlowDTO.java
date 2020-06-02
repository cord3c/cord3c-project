package io.cord3c.rest.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import net.corda.core.crypto.SecureHash;

import java.util.UUID;

@JsonApiResource(type = "flow")
@Data
public class RunningFlowDTO {

	@JsonApiId
	private UUID id;

	private String flowClass;

	private JsonNode parameters;

	private SecureHash transactionId;

}
