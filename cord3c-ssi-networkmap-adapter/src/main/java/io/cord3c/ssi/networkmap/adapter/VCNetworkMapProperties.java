package io.cord3c.ssi.networkmap.adapter;

import java.net.URL;
import java.security.cert.X509Certificate;

import lombok.Data;
import lombok.SneakyThrows;
import org.bouncycastle.util.encoders.UrlBase64;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "cord3c.ssi.networkmap", ignoreUnknownFields = false)
@Data
public class VCNetworkMapProperties {

	private String url;

	private X509Certificate rootCertificate;

	private String host;

	@SneakyThrows
	public String getHost() {
		if (host == null) {
			return new URL(url).getHost();
		}
		return host;
	}
}
