package io.cord3c.ssi.networkmap.resolver;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.cert.X509Certificate;

@ConfigurationProperties(value = "cord3c.ssi.networkmap", ignoreUnknownFields = false)
@Data
public class NetworkMapResolverProperties {

	private String url;

	private X509Certificate rootCertificate;

}
