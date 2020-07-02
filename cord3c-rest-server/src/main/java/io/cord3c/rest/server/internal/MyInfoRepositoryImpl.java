package io.cord3c.rest.server.internal;

import java.util.Arrays;

import com.google.common.base.Verify;
import io.cord3c.rest.api.map.MyInfoDTO;
import io.cord3c.rest.api.map.MyInfoRepository;
import io.cord3c.rest.api.map.NodeDTO;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.NodeInfo;

public class MyInfoRepositoryImpl extends ReadOnlyResourceRepositoryBase<MyInfoDTO, String>
		implements MyInfoRepository {

	private final AppServiceHub serviceHub;

	private final CordaMapper cordaMapper;

	public MyInfoRepositoryImpl(AppServiceHub serviceHub, CordaMapper cordaMapper) {
		super(MyInfoDTO.class);
		this.serviceHub = serviceHub;
		this.cordaMapper = cordaMapper;
		Verify.verifyNotNull(cordaMapper);
	}

	@Override
	public ResourceList<MyInfoDTO> findAll(QuerySpec querySpec) {
		NodeInfo nodeInfo = serviceHub.getMyInfo();

		NodeDTO nodeDto = cordaMapper.map(nodeInfo);

		MyInfoDTO dto = new MyInfoDTO();
		dto.setId("me");
		dto.setNode(nodeDto);
		return querySpec.apply(Arrays.asList(dto));
	}
}
