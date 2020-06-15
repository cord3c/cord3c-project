package io.cord3c.example.systemtest;

import io.cord3c.rest.client.NodeRestClient;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.testkit.RandomWalkLinkChecker;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ExtendWith(SecretExtension.class)
@SpringBootTest(classes = SystemTestConfiguration.class)
public class LinkWalkerSysTest implements WithAssertions {

	@Autowired
	private NodeRestClient client;

	@Autowired
	private SystemTestProperties properties;

	@Test
	public void test() {
		HttpAdapter httpAdapter = client.getCrnk().getHttpAdapter();
		RandomWalkLinkChecker linkChecker = new RandomWalkLinkChecker(httpAdapter) {

			@Override
			protected boolean accept(String url, HttpAdapterResponse response) {
				return super.accept(url, response);
			}
		};
		linkChecker.setSeed(System.currentTimeMillis()); // we keep it random by default
		linkChecker.setWalkLength(300);
		linkChecker.addStartUrl(properties.getNodeUrl());
		linkChecker.addStartUrl(properties.getResolverUrl());
		linkChecker.performCheck();
	}
}
