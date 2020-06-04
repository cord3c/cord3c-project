package io.cord3c.rest.server.internal;

import io.cord3c.rest.client.map.NodeDTO;
import io.cord3c.rest.client.map.NodeRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.node.AppServiceHub;

import java.util.List;
import java.util.stream.Collectors;

public class NodeRepositoryImpl extends ReadOnlyResourceRepositoryBase<NodeDTO, String>
		implements NodeRepository {

	private final AppServiceHub serviceHub;

	private final CordaMapper cordaMapper;


	public NodeRepositoryImpl(AppServiceHub serviceHub, CordaMapper cordaMapper) {
		super(NodeDTO.class);
		this.serviceHub = serviceHub;
		this.cordaMapper = cordaMapper;
	}

	@Override
	public ResourceList<NodeDTO> findAll(QuerySpec querySpec) {
		List<NodeDTO> nodes = serviceHub.getNetworkMapCache()
				.getAllNodes().stream().map(it -> cordaMapper.map(it)).collect(Collectors.toList());

		return querySpec.apply(nodes);
	}


}
