package io.cord3c.example.systemtest;

import io.cord3c.rest.client.NetworkMapRestClient;
import io.cord3c.rest.client.NodeRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SystemTestProperties.class)
public class SystemTestConfiguration {

	@Autowired
	private SystemTestProperties properties;

	@Bean
	public NodeRestClient nodeRestClient() {
		return new NodeRestClient(properties.getNodeUrl());
	}

	@Bean
	public NetworkMapRestClient networkMapClient() {
		return new NetworkMapRestClient(properties.getResolverUrl());
	}
}
