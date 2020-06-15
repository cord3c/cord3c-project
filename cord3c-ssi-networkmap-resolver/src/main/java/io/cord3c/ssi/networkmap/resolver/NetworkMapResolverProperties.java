package io.cord3c.ssi.networkmap.resolver;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.cert.X509Certificate;

@ConfigurationProperties(value = "cord3c.networkmap", ignoreUnknownFields = false)
@Data
public class NetworkMapResolverProperties {

	private String url;

	private String externalUrl;

	public String getExternalUrl() {
		return externalUrl != null ? externalUrl : url;
	}

	private X509Certificate rootCertificate;

}
