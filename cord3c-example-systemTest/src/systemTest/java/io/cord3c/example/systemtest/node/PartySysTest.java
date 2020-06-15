package io.cord3c.example.systemtest.node;

import io.cord3c.example.systemtest.SecretExtension;
import io.cord3c.example.systemtest.SystemTestConfiguration;
import io.cord3c.rest.client.NodeRestClient;
import io.cord3c.rest.client.map.PartyDTO;
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
public class PartySysTest implements WithAssertions {

	@Autowired
	private NodeRestClient client;

	@Test
	public void verifyGet() {
		ResourceList<PartyDTO> parties = client.getParties().findAll(new QuerySpec(PartyDTO.class));
		assertThat(parties).hasSize(1);

		PartyDTO party = parties.get(0);
		assertThat(party.getId()).isNotNull();
		assertThat(party.getName().getOrganisation()).isEqualTo("STAR Labs");
	}
}
