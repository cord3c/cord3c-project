package io.cord3c.server.rest.internal;

import io.cord3c.server.rest.NotaryDTO;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;

import java.util.List;
import java.util.stream.Collectors;

public class NotaryRepository extends ReadOnlyResourceRepositoryBase<NotaryDTO, String> {

	private final ServiceHub serviceHub;

	public NotaryRepository(ServiceHub serviceHub) {
		super(NotaryDTO.class);
		this.serviceHub = serviceHub;
	}

	@Override
	public ResourceList<NotaryDTO> findAll(QuerySpec querySpec) {
		List<NotaryDTO> list = serviceHub.getNetworkMapCache().getNotaryIdentities().stream()
				.map(it -> toDto(it))
				.collect(Collectors.toList());

		return querySpec.apply(list);
	}

	private NotaryDTO toDto(Party party) {
		NotaryDTO dto = new NotaryDTO();
		dto.setId(party.getName().getX500Principal().getName());
		return dto;
	}
}
