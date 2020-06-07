package io.cord3c.ssi.api.internal;

import lombok.experimental.UtilityClass;

@UtilityClass
public class VCUtils {

	public static String toHost(String networkMapUrl) {
		networkMapUrl = removeSuffix(networkMapUrl, "/");
		networkMapUrl = removeSuffix(networkMapUrl, ":80");
		networkMapUrl = removeSuffix(networkMapUrl, ":443");
		if (networkMapUrl.startsWith("http")) {
			return networkMapUrl.substring(networkMapUrl.indexOf("://") + 1);
		}
		return networkMapUrl;
	}

	private static String removeSuffix(String networkMapUrl, String s) {
		if (networkMapUrl.endsWith(s)) {
			return networkMapUrl.substring(0, networkMapUrl.length() - s.length());
		}
		return networkMapUrl;
	}
}
