package io.cord3c.rest.client.map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.cord3c.rest.client.X500Name;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;

import java.security.PublicKey;

@Data
@JsonApiResource(type = "party")
public class PartyDTO {

	@JsonApiId
	private String id;

	private String did;

	private X500Name name;

	@JsonIgnore // no needed on rest layer yet, needs custom serializer
	private PublicKey owningKey;

	// val certificate: X509Certificate
}
