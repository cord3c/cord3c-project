package io.cord3c.ssi.networkmap.adapter.repository;

import io.cord3c.rest.client.map.NetworkParametersDTO;
import io.cord3c.rest.client.map.NetworkParametersRepository;
import io.cord3c.rest.server.internal.CordaMapper;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.crypto.SecureHash;
import net.corda.core.internal.SignedDataWithCert;
import net.corda.core.node.NetworkParameters;
import net.corda.node.services.network.NetworkMapClient;
import net.corda.node.services.network.NetworkMapResponse;
import net.corda.nodeapi.internal.network.NetworkMap;
import org.mapstruct.Context;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NetworkParametersRepositoryImpl extends ReadOnlyResourceRepositoryBase<NetworkParametersDTO, String>
		implements NetworkParametersRepository {


	private final CordaMapper cordaMapper;

	private final NetworkMapClient networkMapClient;

	public NetworkParametersRepositoryImpl(NetworkMapClient networkMapClient, CordaMapper cordaMapper) {
		super(NetworkParametersDTO.class);
		this.networkMapClient = networkMapClient;
		this.cordaMapper = cordaMapper;
	}

	@Override
	public ResourceList<NetworkParametersDTO> findAll(QuerySpec querySpec) {
		NetworkMapResponse networkMap = networkMapClient.getNetworkMap(null);
		NetworkMap payload = networkMap.getPayload();
		SecureHash hash = payload.getNetworkParameterHash();

		SignedDataWithCert<NetworkParameters> signed = networkMapClient.getNetworkParameters(hash);

		NetworkParameters parameters = VCNetworkMapUtils.deserialize(NetworkParameters.class, signed.getRaw().copyBytes());
		NetworkParametersDTO resource = map(parameters, hash);
		return querySpec.apply(Arrays.asList(resource));
	}

	// for some reason MapStruct cannot handle this class, leading in a StackOverFlow
	public NetworkParametersDTO map(NetworkParameters networkParameters, @Context SecureHash id) {
		NetworkParametersDTO dto = new NetworkParametersDTO();

		dto.setMinimumPlatformVersion(networkParameters.getMinimumPlatformVersion());
		dto.setMaxMessageSize(networkParameters.getMaxMessageSize());
		dto.setMaxTransactionSize(networkParameters.getMaxTransactionSize());
		dto.setModifiedTime(networkParameters.getModifiedTime());
		dto.setEpoch(networkParameters.getEpoch());
		dto.setEventHorizon(networkParameters.getEventHorizon());
		dto.setEventHorizon(networkParameters.getEventHorizon());
		dto.setNotaries(networkParameters.getNotaries().stream().map(it -> cordaMapper.map(it)).collect(Collectors.toList()));
		dto.setId(id.toString());
		return dto;
	}
}
