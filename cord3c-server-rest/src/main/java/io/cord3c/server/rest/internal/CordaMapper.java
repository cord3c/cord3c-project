package io.cord3c.server.rest.internal;

import io.cord3c.server.rest.NodeDTO;
import io.cord3c.server.rest.PartyDTO;
import io.cord3c.server.rest.VaultStateDTO;
import net.corda.core.identity.Party;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.node.NodeInfo;
import net.corda.node.services.vault.VaultSchemaV1;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class CordaMapper {

	@Mapping(target = "id", expression = "java(getId(nodeInfo))")
	public abstract NodeDTO map(NodeInfo nodeInfo);

	public abstract VaultStateDTO map(VaultSchemaV1.VaultStates state);

	@Mapping(target = "id", expression = "java(getId(party))")
	public abstract PartyDTO mapParty(PartyAndCertificate party);

	@Mapping(target = "id", expression = "java(getId(party))")
	public abstract PartyDTO mapParty(Party party);

	protected String getId(NodeInfo nodeInfo) {
		return nodeInfo.getLegalIdentities().get(0).getName().getX500Principal().getName();
	}

	protected String getId(PartyAndCertificate party) {
		return getId(party.getParty());
	}

	protected String getId(Party party) {
		return party.getOwningKey().toString();
	}
}