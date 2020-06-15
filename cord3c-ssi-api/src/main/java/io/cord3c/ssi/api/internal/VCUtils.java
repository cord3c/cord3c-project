package io.cord3c.ssi.api.internal;

import lombok.experimental.UtilityClass;

import java.net.URI;
import java.util.Objects;

@UtilityClass
public class VCUtils {

	public static String toHost(String networkMapUrl) {
		URI uri = URI.create(networkMapUrl);
		String host = Objects.requireNonNull(uri.getHost());
		int port = uri.getPort();
		return port != 80 && port != 443 && port != -1 ? host + ":" + port : host;
	}
}
