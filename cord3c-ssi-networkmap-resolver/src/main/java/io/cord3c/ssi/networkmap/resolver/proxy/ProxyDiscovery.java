package io.cord3c.ssi.networkmap.resolver.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyDiscovery {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyDiscovery.class);

	public static void readFromEnv() {
		if (getEnv("HTTP_PROXY") != null || getEnv("HTTPS_PROXY") != null) {
			setupProxy("http");
			setupProxy("https");

			String no_proxy = getEnv("NO_PROXY");
			if (no_proxy != null) {
				// see https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html
				String javaNoProxy = Arrays.asList(no_proxy.split("\\,")).stream().map(it -> it.startsWith(".") ? "*" + it : it).collect(Collectors.joining("|"));
				System.setProperty("http.nonProxyHosts", javaNoProxy);
				System.setProperty("https.nonProxyHosts", javaNoProxy);
			}
		}
		else {
			LOGGER.debug("no proxy configured");
		}
	}

	private static void setupProxy(String name) {
		String envName = name.toUpperCase() + "_PROXY";
		String value = getEnv(envName);
		if (value != null) {
			URL url;
			try {
				url = new URL(value);
			}
			catch (MalformedURLException e) {
				throw new IllegalStateException("invalid proxy url " + value + " in env " + envName);
			}

			System.setProperty(name + ".proxyHost", url.getHost());
			System.setProperty(name + ".proxyPort", Integer.toString(url.getPort()));
		}
	}

	private static String getEnv(String envName) {
		String value = System.getenv(envName.toUpperCase());
		if (value == null) {
			value = System.getenv(envName.toLowerCase());
		}
		return value;
	}
}
