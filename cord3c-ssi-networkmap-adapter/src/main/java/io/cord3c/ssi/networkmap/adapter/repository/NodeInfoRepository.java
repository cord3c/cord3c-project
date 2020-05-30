package io.cord3c.ssi.networkmap.adapter.repository;

import java.util.ArrayList;
import java.util.List;

import io.cord3c.ssi.networkmap.adapter.repository.VCNetworkMapUtils.SecureHashHolder;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.crypto.SecureHash;
import net.corda.core.node.NodeInfo;
import net.corda.node.services.network.NetworkMapClient;
import net.corda.node.services.network.NetworkMapResponse;
import net.corda.nodeapi.internal.network.NetworkMap;
import org.mapstruct.factory.Mappers;

public class NodeInfoRepository extends ReadOnlyResourceRepositoryBase<NodeInfoResource, SecureHash> {

	private static final CordaMapper MAPPER = Mappers.getMapper(CordaMapper.class);

	private final NetworkMapClient networkMapClient;

	public NodeInfoRepository(NetworkMapClient networkMapClient) {
		super(NodeInfoResource.class);
		this.networkMapClient = networkMapClient;
	}

	@Override
	public ResourceList<NodeInfoResource> findAll(QuerySpec querySpec) {
		NetworkMapResponse networkMap = networkMapClient.getNetworkMap(null);
		NetworkMap payload = networkMap.getPayload();
		List<SecureHash> nodeInfoHashes = payload.getNodeInfoHashes();

		List<NodeInfoResource> resources = new ArrayList<>();
		for (SecureHash nodeInfoHash : nodeInfoHashes) {
			NodeInfo nodeInfo = networkMapClient.getNodeInfo(nodeInfoHash);
			resources.add(MAPPER.toResource(nodeInfo, new SecureHashHolder(nodeInfoHash)));
		}
		return querySpec.apply(resources);
	}
}
