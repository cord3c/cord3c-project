package io.cord3c.ssi.networkmap.adapter.repository;

import com.google.common.base.Verify;
import io.cord3c.rest.client.map.NetworkParametersDTO;
import io.cord3c.rest.client.map.NetworkParametersRepository;
import io.cord3c.rest.client.map.NotaryDTO;
import io.cord3c.rest.client.map.NotaryRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

public class NotaryMapRepositoryImpl extends ReadOnlyResourceRepositoryBase<NotaryDTO, String> implements NotaryRepository {

	private final NetworkParametersRepository networkParametersRepository;

	public NotaryMapRepositoryImpl(NetworkParametersRepository networkParametersRepository) {
		super(NotaryDTO.class);
		this.networkParametersRepository = networkParametersRepository;
	}

	@Override
	public ResourceList<NotaryDTO> findAll(QuerySpec querySpec) {
		ResourceList<NetworkParametersDTO> list = networkParametersRepository.findAll(new QuerySpec(NetworkParametersDTO.class));
		Verify.verify(list.size() == 1);
		NetworkParametersDTO networkParameters = list.get(0);

		return querySpec.apply(networkParameters.getNotaries());
	}
}
