package io.cord3c.rest.server.internal;

import io.cord3c.rest.api.FlowExecutionDTO;
import io.cord3c.rest.api.VaultStateDTO;
import io.cord3c.rest.api.map.PartyDTO;
import lombok.Setter;
import net.corda.core.identity.Party;
import net.corda.core.messaging.StateMachineTransactionMapping;
import net.corda.node.services.vault.VaultSchemaV1;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class VaultMapper {

	@Setter
	private CordaMapper cordaMapper;

	public PartyDTO map(Party party) {
		return cordaMapper.mapParty(party);
	}

	@Mapping(target = "restLinks", ignore = true)
	public abstract VaultStateDTO map(VaultSchemaV1.VaultStates state);

	public FlowExecutionDTO map(StateMachineTransactionMapping mapping) {
		FlowExecutionDTO dto = new FlowExecutionDTO();
		dto.setId(mapping.getStateMachineRunId().getUuid());
		dto.setTransactionId(mapping.getTransactionId());
		return dto;
	}
}