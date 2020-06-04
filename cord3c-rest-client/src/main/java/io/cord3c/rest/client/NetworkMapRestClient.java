package io.cord3c.rest.client;

import io.cord3c.rest.client.map.NetworkParametersRepository;
import io.cord3c.rest.client.map.NodeRepository;
import io.cord3c.rest.client.map.NotaryRepository;
import io.cord3c.rest.client.map.PartyRepository;
import io.crnk.client.CrnkClient;
import lombok.Getter;

public class NetworkMapRestClient {

	@Getter
	private final CrnkClient client;

	public NetworkMapRestClient(String url) {
		this(new CrnkClient(url));
	}

	public NetworkMapRestClient(CrnkClient client) {
		this.client = client;
		client.findModules();
	}

	public NotaryRepository getNotaries() {
		return client.getRepositoryForInterface(NotaryRepository.class);
	}

	public NetworkParametersRepository getNetworkParameters() {
		return client.getRepositoryForInterface(NetworkParametersRepository.class);
	}

	public PartyRepository getParties() {
		return client.getRepositoryForInterface(PartyRepository.class);
	}

	public NodeRepository getNodes() {
		return client.getRepositoryForInterface(NodeRepository.class);
	}
}
