package io.cord3c.ssi.networkmap.resolver;

import io.cord3c.ssi.networkmap.resolver.config.VCNetworkMapConfiguration;
import org.springframework.boot.SpringApplication;

public class NetworkMapResolverMain {

	public static void main(String[] args) {
		NetworkMapResolverMain app = new NetworkMapResolverMain();
		app.run();
	}

	public void run() {
		String[] args = new String[0];
		SpringApplication app = new SpringApplication(VCNetworkMapConfiguration.class);
		app.run(args);
	}
}
