package io.cord3c.rest.server.internal;

import io.cord3c.rest.client.NodeDTO;
import io.cord3c.rest.client.PartyDTO;
import io.cord3c.rest.client.RunningFlowDTO;
import io.cord3c.rest.client.VaultStateDTO;
import net.corda.core.crypto.CryptoUtils;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.messaging.StateMachineTransactionMapping;
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
		CordaX500Name name = nodeInfo.getLegalIdentities().get(0).getName();
		StringBuilder builder = new StringBuilder();
		append(builder, name.getCommonName());
		append(builder, name.getOrganisation());
		append(builder, name.getOrganisationUnit());
		append(builder, name.getState());
		append(builder, name.getLocality());
		append(builder, name.getCountry());
		return builder.toString();
	}

	private void append(StringBuilder builder, String value) {
		if (value != null) {
			if (builder.length() > 0) {
				builder.append("_");
			}
			builder.append(value.replace(" ", "").toLowerCase());
		}
	}

	protected String getId(PartyAndCertificate party) {
		return getId(party.getParty());
	}

	protected String getId(Party party) {
		return CryptoUtils.toStringShort(party.getOwningKey());
	}

	public RunningFlowDTO map(StateMachineTransactionMapping mapping) {
		RunningFlowDTO dto = new RunningFlowDTO();
		dto.setId(mapping.getStateMachineRunId().getUuid());
		dto.setTransactionId(mapping.getTransactionId());
		return dto;
	}
}