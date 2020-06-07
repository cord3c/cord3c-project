package io.cord3c.rest.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cord3c.rest.client.map.NodeRepository;
import io.cord3c.rest.client.map.NotaryRepository;
import io.cord3c.rest.client.map.PartyRepository;
import io.crnk.client.CrnkClient;
import lombok.Getter;

public class NodeRestClient {

	@Getter
	private final CrnkClient client;

	public NodeRestClient(String url) {
		this(new CrnkClient(url));
	}

	public NodeRestClient(CrnkClient client) {
		this.client = client;
		client.findModules();
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

	public VCRepository getCredentials() {
		return client.getRepositoryForInterface(VCRepository.class);
	}

	public RunningFlowDTO invokeFlow(Class flowClass, Object input) {
		RunningFlowDTO flow = new RunningFlowDTO();
		flow.setFlowClass(flowClass.getName());
		flow.setParameters(toJson(input));
		return getFlows().create(flow);
	}

	private JsonNode toJson(Object value) {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.valueToTree(value);
	}

}
