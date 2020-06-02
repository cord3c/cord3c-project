package io.cord3c.rest.client;

import io.crnk.client.CrnkClient;
import lombok.Getter;

public class RestClient {

	@Getter
	private final CrnkClient client;

	public RestClient(String url) {
		this(new CrnkClient(url));
	}

	public RestClient(CrnkClient client) {
		this.client = client;
	}

	public NotaryRepository getNotaries() {
		return client.getRepositoryForInterface(NotaryRepository.class);
	}

	public VaultStateRepository getVault() {
		return client.getRepositoryForInterface(VaultStateRepository.class);
	}

	public RunningFlowRepository getFlows() {
		return client.getRepositoryForInterface(RunningFlowRepository.class);
	}

	public PartyRepository getParties() {
		return client.getRepositoryForInterface(PartyRepository.class);
	}

	public NodeRepository getNodes() {
		return client.getRepositoryForInterface(NodeRepository.class);
	}
}
