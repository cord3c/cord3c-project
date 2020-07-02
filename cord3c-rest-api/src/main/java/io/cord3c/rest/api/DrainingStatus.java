package io.cord3c.rest.api;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonApiResource(type = "draining")
@AllArgsConstructor
@NoArgsConstructor
public class DrainingStatus {

	public enum Status {DRAINING, DRAINED, OFF}

	private Status status;

	@JsonApiId
	private String id;

	private boolean active;

}
