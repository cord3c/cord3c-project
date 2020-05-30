package io.cord3c.ssi.networkmap.adapter.repository;

import net.corda.core.node.NetworkParameters;
import net.corda.core.node.NodeInfo;
import org.mapstruct.Mapper;

@Mapper
public abstract class CordaMapper {

	public NetworkParametersResource toResource(NetworkParameters networkParamters, VCNetworkMapUtils.SecureHashHolder hashHolder) {
		return null; // FIXMe
	}

	public NodeInfoResource toResource(NodeInfo nodeInfo, VCNetworkMapUtils.SecureHashHolder hashHolder) {
		return null;
	}

}