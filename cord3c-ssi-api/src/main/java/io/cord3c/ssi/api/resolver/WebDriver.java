package io.cord3c.ssi.api.resolver;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.common.base.Verify;
import io.cord3c.ssi.api.did.DID;
import io.cord3c.ssi.api.did.DIDDocument;
import io.cord3c.ssi.api.internal.DIDGenerator;
import io.cord3c.ssi.api.internal.IOUtils;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.math.NumberUtils;

@Data
public class WebDriver implements DIDDriver {

	public static final String DID_METHOD = "web";

	private boolean allowHttp = true;

	@Override
	public String getMethod() {
		return DID_METHOD;
	}

	@Override
	@SneakyThrows
	public DIDDocument resolve(DID did) {
		try {
			return resolve(did, true);
		} catch (Exception e) {
			if (allowHttp) {
				return resolve(did, false);
			} else {
				throw e;
			}
		}
	}

	@SneakyThrows
	private DIDDocument resolve(DID did, boolean useHttps) {
		URL url = toUrl(did, useHttps);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		Verify.verify(conn.getResponseCode() <= 200, "failed to retrieve DID from %s, got=%s", url, conn.getResponseMessage());
		InputStream in = conn.getInputStream();
		String encoding = conn.getContentEncoding();
		encoding = encoding == null ? "UTF-8" : encoding;
		String body = IOUtils.toString(in, encoding);

		return DIDDocument.parse(body);
	}

	@SneakyThrows
	private URL toUrl(DID did, boolean useHttps) {
		String didString = did.toString();
		Verify.verify(didString.startsWith(DIDGenerator.DID_WEB_PREFIX + ":"));
		String[] path = didString.substring((DIDGenerator.DID_WEB_PREFIX + ":").length()).split("\\:");

		StringBuilder builder = new StringBuilder();
		builder.append(useHttps ? "https" : "http");
		builder.append(":/");

		boolean hasPort = false;
		for (int i = 0; i < path.length; i++) {
			String element = path[i];
			if (i == 1 && NumberUtils.isParsable(element)) {
				// we have a port
				hasPort = true;
				builder.append(":");
			} else {
				builder.append("/");
			}
			builder.append(element);
		}

		if (path.length == (hasPort ? 2 : 1)) {
			builder.append("/.well-known/did.json");
		}
		return new URL(builder.toString());
	}
}
