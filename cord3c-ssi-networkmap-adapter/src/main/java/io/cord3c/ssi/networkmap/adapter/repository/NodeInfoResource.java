package io.cord3c.ssi.networkmap.adapter.repository;

import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import net.corda.core.crypto.SecureHash;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.utilities.NetworkHostAndPort;

@Data
@JsonApiResource(type = "nodeInfo")
public class NodeInfoResource {

	@JsonApiId
	private SecureHash id;

	private List<NetworkHostAndPort> addresses;

	private List<PartyAndCertificate> legalIdentitiesAndCerts;

	private int platformVersion;

	private long serial;

}
