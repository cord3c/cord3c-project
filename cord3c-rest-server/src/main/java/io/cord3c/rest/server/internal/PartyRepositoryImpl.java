package io.cord3c.rest.server.internal;

import com.google.common.base.Verify;
import io.cord3c.rest.api.map.PartyDTO;
import io.cord3c.rest.api.map.PartyRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.node.AppServiceHub;

import java.util.List;
import java.util.stream.Collectors;

public class PartyRepositoryImpl extends ReadOnlyResourceRepositoryBase<PartyDTO, String>
		implements PartyRepository {

	private final AppServiceHub serviceHub;

	private final CordaMapper cordaMapper;


	public PartyRepositoryImpl(AppServiceHub serviceHub, CordaMapper cordaMapper) {
		super(PartyDTO.class);
		this.serviceHub = serviceHub;
		this.cordaMapper = cordaMapper;
		Verify.verifyNotNull(cordaMapper);
	}

	@Override
	public ResourceList<PartyDTO> findAll(QuerySpec querySpec) {
		List<PartyDTO> nodes = serviceHub.getNetworkMapCache()
				.getAllNodes()
				.stream()
				.flatMap(it -> it.getLegalIdentitiesAndCerts().stream())
				.map(it -> cordaMapper.mapParty(it)).collect(Collectors.toList());

		return querySpec.apply(nodes);
	}
}
