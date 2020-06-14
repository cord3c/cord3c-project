package io.cord3c.ssi.networkmap.resolver.repository;

import io.cord3c.rest.client.map.NodeDTO;
import io.cord3c.rest.client.map.NodeRepository;
import io.cord3c.rest.client.map.PartyDTO;
import io.cord3c.rest.server.internal.CordaMapper;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.crypto.SecureHash;
import net.corda.core.node.NodeInfo;
import net.corda.node.services.network.NetworkMapClient;
import net.corda.node.services.network.NetworkMapResponse;
import net.corda.nodeapi.internal.network.NetworkMap;

import java.util.ArrayList;
import java.util.List;

public class NodeMapRepositoryImpl extends ReadOnlyResourceRepositoryBase<NodeDTO, String> implements NodeRepository {

	private final CordaMapper cordaMapper;

	private final NetworkMapClient networkMapClient;

	public NodeMapRepositoryImpl(NetworkMapClient networkMapClient, CordaMapper cordaMapper) {
		super(NodeDTO.class);
		this.networkMapClient = networkMapClient;
		this.cordaMapper = cordaMapper;
	}

	@Override
	public ResourceList<NodeDTO> findAll(QuerySpec querySpec) {
		NetworkMapResponse networkMap = networkMapClient.getNetworkMap(null);
		NetworkMap payload = networkMap.getPayload();
		List<SecureHash> nodeInfoHashes = payload.getNodeInfoHashes();

		List<NodeDTO> resources = new ArrayList<>();
		for (SecureHash nodeInfoHash : nodeInfoHashes) {
			NodeInfo nodeInfo = networkMapClient.getNodeInfo(nodeInfoHash);
			resources.add(cordaMapper.map(nodeInfo));
		}
		return querySpec.apply(resources);
	}

	public PartyDTO findParty(String did) {
		QuerySpec querySpec = new QuerySpec(NodeInfo.class);
		ResourceList<NodeDTO> nodeInfos = findAll(querySpec);
		for (NodeDTO node : nodeInfos) {
			PartyDTO party = node.getLegalIdentitiesAndCerts().get(0);
			if (did.equals(party.getDid())) {
				return party;
			}
		}
		return null;
	}
}
