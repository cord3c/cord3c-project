package io.cord3c.ssi.networkmap.adapter;

import io.cord3c.ssi.networkmap.adapter.config.VCNetworkMapConfiguration;
import org.springframework.boot.SpringApplication;

public class VCNetworkMapMain {

	public static void main(String[] args) {
		VCNetworkMapMain app = new VCNetworkMapMain();
		app.run();
	}

	public void run() {
		String[] args = new String[0];
		SpringApplication app = new SpringApplication(VCNetworkMapConfiguration.class);
		app.run(args);
	}
}
