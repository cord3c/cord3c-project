package io.cord3c.example.systemtest.node;

import io.cord3c.example.cordapp.IssueLeagueMembership;
import io.cord3c.example.systemtest.SecretExtension;
import io.cord3c.example.systemtest.SystemTestConfiguration;
import io.cord3c.rest.client.NodeRestClient;
import io.cord3c.rest.client.FlowExecutionDTO;
import io.cord3c.rest.client.VerifiableCredentialDTO;
import io.cord3c.ssi.api.internal.DIDGenerator;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.crnk.core.queryspec.QuerySpec;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@ExtendWith(SecretExtension.class)
@SpringBootTest(classes = SystemTestConfiguration.class)
public class IssueCredentialSysTest implements WithAssertions {

	@Autowired
	private NodeRestClient client;

	@Test
	public void issue() {
		String did = DIDGenerator.generateRandomDid("http://private");

		// trigger flow
		IssueLeagueMembership.IssueMembershipInput input = new IssueLeagueMembership.IssueMembershipInput();
		input.setDid(did);
		input.setName("Flash");
		FlowExecutionDTO<VerifiableCredential> flow = client.invokeFlow(IssueLeagueMembership.IssueMembershipInititor.class, input);
		assertThat(flow).isNotNull();

		// wait for flow
		flow = client.waitForFlow(flow, Duration.ofSeconds(15));
		assertThat(flow.getCurrentStep()).isEqualTo(FlowExecutionDTO.ENDED_STEP);

		// verify
		VerifiableCredential credential = flow.toTypedResult();
		assertThat(credential.getId()).isNotNull();
		assertThat(credential.getProof()).isNotNull();
		assertThat(credential.getClaims().get("id").textValue()).isEqualTo(did);
		assertThat(credential.getIssuanceDate()).isNotNull();
		assertThat(credential.getIssuer()).contains(":parties:"); // corda network map did
		assertThat(credential.getTypes()).isEqualTo(Arrays.asList("VerifiableCredential", "leagueMembership"));
		assertThat(credential.getContexts()).isEqualTo(Arrays.asList("https://www.w3.org/2018/credentials/v1"));

		VerifiableCredential storedCredential = client.getCredentials().findOne(credential.toHashId(), new QuerySpec(VerifiableCredentialDTO.class)).getCredential();
		assertThat(storedCredential).isNotNull();
		assertThat(storedCredential.getId()).isEqualTo(credential.getId());
	}
}
