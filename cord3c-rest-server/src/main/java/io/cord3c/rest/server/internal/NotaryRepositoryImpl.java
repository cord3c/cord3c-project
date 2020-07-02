package io.cord3c.rest.server.internal;

import io.cord3c.rest.api.map.NotaryDTO;
import io.cord3c.rest.api.map.NotaryRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;

import java.util.List;
import java.util.stream.Collectors;

public class NotaryRepositoryImpl extends ReadOnlyResourceRepositoryBase<NotaryDTO, String> implements NotaryRepository {

	private final ServiceHub serviceHub;

	private final CordaMapper cordaMapper;


	public NotaryRepositoryImpl(ServiceHub serviceHub, CordaMapper cordaMapper) {
		super(NotaryDTO.class);
		this.serviceHub = serviceHub;
		this.cordaMapper = cordaMapper;
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
		dto.setId(cordaMapper.getId(party));
		dto.setIdentity(cordaMapper.mapParty(party));
		return dto;
	}
}
