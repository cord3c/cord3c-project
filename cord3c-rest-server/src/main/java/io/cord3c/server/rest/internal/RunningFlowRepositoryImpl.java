package io.cord3c.server.rest.internal;

import io.cord3c.server.rest.RunningFlowDTO;
import io.cord3c.server.rest.RunningFlowRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.node.AppServiceHub;
import org.mapstruct.factory.Mappers;

public class RunningFlowRepositoryImpl extends ReadOnlyResourceRepositoryBase<RunningFlowDTO, String>
		implements RunningFlowRepository {

	private final AppServiceHub serviceHub;

	private static final CordaMapper MAPPER = Mappers.getMapper(CordaMapper.class);

	public RunningFlowRepositoryImpl(AppServiceHub serviceHub) {
		super(RunningFlowDTO.class);
		this.serviceHub = serviceHub;
	}

	@Override
	public ResourceList<RunningFlowDTO> findAll(QuerySpec querySpec) {
		return null;
	}
}
