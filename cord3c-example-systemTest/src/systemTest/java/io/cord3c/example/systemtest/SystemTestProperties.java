package io.cord3c.example.systemtest;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "cord3c.systemtest", ignoreUnknownFields = false)
@Data
public class SystemTestProperties {

	private String apiUrl;

	private String networkMapUrl;
}
