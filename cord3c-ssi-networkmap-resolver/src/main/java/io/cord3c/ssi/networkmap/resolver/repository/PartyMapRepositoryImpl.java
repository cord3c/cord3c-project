package io.cord3c.ssi.networkmap.resolver.repository;

import io.cord3c.rest.api.map.NodeDTO;
import io.cord3c.rest.api.map.NodeRepository;
import io.cord3c.rest.api.map.PartyDTO;
import io.cord3c.rest.api.map.PartyRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

import java.util.List;
import java.util.stream.Collectors;

public class PartyMapRepositoryImpl extends ReadOnlyResourceRepositoryBase<PartyDTO, String> implements PartyRepository {

	private final NodeRepository nodeRepository;

	public PartyMapRepositoryImpl(NodeRepository nodeRepository) {
		super(PartyDTO.class);
		this.nodeRepository = nodeRepository;
	}

	@Override
	public ResourceList<PartyDTO> findAll(QuerySpec querySpec) {
		ResourceList<NodeDTO> nodes = nodeRepository.findAll(new QuerySpec(NodeDTO.class));
		List<PartyDTO> parties = nodes.stream().flatMap(it -> it.getLegalIdentitiesAndCerts().stream()).collect(Collectors.toList());
		return querySpec.apply(parties);
	}
}
