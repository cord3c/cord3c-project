package io.cord3c.rest.server.internal;

import io.cord3c.rest.client.NotaryDTO;
import io.cord3c.rest.client.NotaryRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

public class NotaryRepositoryImpl extends ReadOnlyResourceRepositoryBase<NotaryDTO, String> implements NotaryRepository {

	private final ServiceHub serviceHub;

	private static final CordaMapper MAPPER = Mappers.getMapper(CordaMapper.class);

	public NotaryRepositoryImpl(ServiceHub serviceHub) {
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
		dto.setId(MAPPER.getId(party));
		dto.setParty(MAPPER.mapParty(party));
		return dto;
	}
}
