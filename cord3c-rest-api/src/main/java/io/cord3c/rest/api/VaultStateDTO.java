package io.cord3c.rest.api;

import io.cord3c.rest.api.map.PartyDTO;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.links.DefaultSelfLinksInformation;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import net.corda.core.node.services.Vault;
import net.corda.core.schemas.PersistentStateRef;

import java.time.Instant;

@JsonApiResource(type = "vaultState")
@Data
@FieldNameConstants
public class VaultStateDTO {

	/**
	 * Holds self link on rest-layer. Important when sending along entities across services to maintain the original origin.
	 */
	@JsonApiLinksInformation
	private DefaultSelfLinksInformation restLinks = new DefaultSelfLinksInformation();

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
