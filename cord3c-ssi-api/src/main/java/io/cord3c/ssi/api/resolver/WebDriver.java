package io.cord3c.ssi.api.resolver;

import com.google.common.base.Verify;
import io.cord3c.ssi.api.did.DID;
import io.cord3c.ssi.api.internal.DIDGenerator;
import io.cord3c.ssi.api.did.DIDDocument;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
		HttpURLConnection conn;
		try {
			URL url = toUrl(did, true);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
		} catch (IOException e) {
			if (allowHttp) {
				URL url = toUrl(did, false);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
			} else {
				throw e;
			}
		}

		Verify.verify(conn.getResponseCode() <= 200);
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
		String path = didString.substring((DIDGenerator.DID_WEB_PREFIX + ":").length());
		path = path.replace(":", "/");
		return new URL((useHttps ? "https" : "http") + "://" + path + "/.well-known/did.json");
	}
}
