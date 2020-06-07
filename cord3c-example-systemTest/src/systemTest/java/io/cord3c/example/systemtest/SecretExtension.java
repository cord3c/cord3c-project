package io.cord3c.example.systemtest;

import org.junit.jupiter.api.extension.Extension;

public class SecretExtension implements Extension {

	static {
		System.setProperty("spring.config.name", "systemtest");
		System.setProperty("spring.config.location",
				"classpath:/secrets/,classpath:/application-common.yaml,classpath:/,classpath:/secrets/,file:.//");
	}
}
