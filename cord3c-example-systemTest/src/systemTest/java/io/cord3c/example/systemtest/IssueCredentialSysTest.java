package io.cord3c.example.systemtest;

import io.cord3c.example.cordapp.IssueLeagueMembership;
import io.cord3c.rest.client.NodeRestClient;
import io.cord3c.rest.client.RunningFlowDTO;
import io.cord3c.ssi.api.internal.DIDGenerator;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ExtendWith(SecretExtension.class)
@SpringBootTest(classes = SystemTestConfiguration.class)
public class IssueCredentialSysTest implements WithAssertions {

	@Autowired
	private NodeRestClient client;

	@Test
	public void issue() {
		String did = DIDGenerator.generateRandomDid("private");

		IssueLeagueMembership.IssueMembershipInput input = new IssueLeagueMembership.IssueMembershipInput();
		input.setDid(did);
		input.setName("Flash");

		RunningFlowDTO flow = client.invokeFlow(IssueLeagueMembership.IssueMembershipInititor.class, input);
		assertThat(flow).isNotNull();
	}
}
