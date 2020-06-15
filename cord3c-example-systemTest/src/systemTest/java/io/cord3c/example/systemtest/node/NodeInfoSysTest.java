package io.cord3c.example.systemtest.node;

import io.cord3c.example.systemtest.SecretExtension;
import io.cord3c.example.systemtest.SystemTestConfiguration;
import io.cord3c.rest.client.NodeRestClient;
import io.cord3c.rest.client.map.NodeDTO;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ExtendWith(SecretExtension.class)
@SpringBootTest(classes = SystemTestConfiguration.class)
public class NodeInfoSysTest implements WithAssertions {

	@Autowired
	private NodeRestClient client;

	@Test
	public void verifyGet() {
		ResourceList<NodeDTO> nodes = client.getNodes().findAll(new QuerySpec(NodeDTO.class));
		assertThat(nodes).hasSize(1);

		NodeDTO node = nodes.get(0);
		assertThat(node.getId()).isEqualTo("starlabs_centralcity_us");
		assertThat(node.getAddresses()).hasSize(1);
	}
}
