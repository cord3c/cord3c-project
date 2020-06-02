package io.cord3c.rest.server.internal;

import io.cord3c.rest.client.NodeDTO;
import io.cord3c.rest.client.NodeRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.node.AppServiceHub;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

public class NodeRepositoryImpl extends ReadOnlyResourceRepositoryBase<NodeDTO, String>
		implements NodeRepository {

	private final AppServiceHub serviceHub;

	private static final CordaMapper MAPPER = Mappers.getMapper(CordaMapper.class);

	public NodeRepositoryImpl(AppServiceHub serviceHub) {
		super(NodeDTO.class);
		this.serviceHub = serviceHub;
	}

	@Override
	public ResourceList<NodeDTO> findAll(QuerySpec querySpec) {
		List<NodeDTO> nodes = serviceHub.getNetworkMapCache()
				.getAllNodes().stream().map(it -> MAPPER.map(it)).collect(Collectors.toList());

		return querySpec.apply(nodes);
	}


}
