package io.cord3c.ssi.networkmap.adapter.repository;

import io.cord3c.rest.client.map.NodeDTO;
import io.cord3c.rest.client.map.NodeRepository;
import io.cord3c.rest.client.map.PartyDTO;
import io.cord3c.rest.client.map.PartyRepository;
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
