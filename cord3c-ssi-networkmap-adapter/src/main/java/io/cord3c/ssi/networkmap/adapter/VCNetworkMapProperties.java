package io.cord3c.ssi.networkmap.adapter;

import java.security.cert.X509Certificate;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "cord3c.ssi.networkmap", ignoreUnknownFields = false)
@Data
public class VCNetworkMapProperties {

	private String networkMapUrl = "https://map.test.cord3c.net";

	private X509Certificate rootCertificate;
}
