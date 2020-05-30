package io.cord3c.ssi.networkmap.adapter.repository;

import java.util.Arrays;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.crypto.SecureHash;
import net.corda.core.internal.SignedDataWithCert;
import net.corda.core.node.NetworkParameters;
import net.corda.node.services.network.NetworkMapClient;
import net.corda.node.services.network.NetworkMapResponse;
import net.corda.nodeapi.internal.network.NetworkMap;
import org.mapstruct.factory.Mappers;

public class NetworkParametersRepository extends ReadOnlyResourceRepositoryBase<NetworkParametersResource, SecureHash> {

	private static final CordaMapper MAPPER = Mappers.getMapper(CordaMapper.class);

	private final NetworkMapClient networkMapClient;

	public NetworkParametersRepository(NetworkMapClient networkMapClient) {
		super(NetworkParametersResource.class);
		this.networkMapClient = networkMapClient;
	}

	@Override
	public ResourceList<NetworkParametersResource> findAll(QuerySpec querySpec) {
		NetworkMapResponse networkMap = networkMapClient.getNetworkMap(null);
		NetworkMap payload = networkMap.getPayload();
		SecureHash hash = payload.getNetworkParameterHash();

		SignedDataWithCert<NetworkParameters> signed = networkMapClient.getNetworkParameters(hash);

		NetworkParameters parameters = VCNetworkMapUtils.deserialize(NetworkParameters.class, signed.getRaw().copyBytes());
		NetworkParametersResource resource = MAPPER.toResource(parameters, new VCNetworkMapUtils.SecureHashHolder(hash));
		return querySpec.apply(Arrays.asList(resource));
	}

}
