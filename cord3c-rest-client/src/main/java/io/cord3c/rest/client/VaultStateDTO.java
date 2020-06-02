package io.cord3c.rest.client;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import net.corda.core.node.services.Vault;
import net.corda.core.schemas.PersistentStateRef;

import java.time.Instant;

@JsonApiResource(type = "vault")
@Data
public class VaultStateDTO {

	@JsonApiId
	private PersistentStateRef stateRef;

	private PartyDTO notary;

	private String contractStateClassName;

	private Vault.StateStatus stateStatus;

	private Instant recordedTime;

	private Instant consumedTime;

	private String lockId;

	private Vault.RelevancyStatus relevancyStatus;

	private Instant lockUpdateTime;

}
