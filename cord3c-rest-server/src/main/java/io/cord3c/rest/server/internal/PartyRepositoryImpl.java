package io.cord3c.rest.server.internal;

import io.cord3c.rest.client.PartyDTO;
import io.cord3c.rest.client.PartyRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.node.AppServiceHub;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

public class PartyRepositoryImpl extends ReadOnlyResourceRepositoryBase<PartyDTO, String>
		implements PartyRepository {

	private final AppServiceHub serviceHub;

	private static final CordaMapper MAPPER = Mappers.getMapper(CordaMapper.class);

	public PartyRepositoryImpl(AppServiceHub serviceHub) {
		super(PartyDTO.class);
		this.serviceHub = serviceHub;
	}

	@Override
	public ResourceList<PartyDTO> findAll(QuerySpec querySpec) {
		List<PartyDTO> nodes = serviceHub.getNetworkMapCache()
				.getAllNodes()
				.stream()
				.flatMap(it -> it.getLegalIdentitiesAndCerts().stream())
				.map(it -> MAPPER.mapParty(it)).collect(Collectors.toList());

		return querySpec.apply(nodes);
	}
}
