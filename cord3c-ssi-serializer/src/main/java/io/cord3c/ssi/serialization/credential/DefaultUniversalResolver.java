package io.cord3c.ssi.serialization.credential;

import com.google.common.base.Verify;
import io.cord3c.ssi.api.internal.DIDGenerator;
import io.cord3c.ssi.api.vc.DIDDocument;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@AllArgsConstructor
public class DefaultUniversalResolver implements UniversalResolver {

	private boolean withHttps;

	public DefaultUniversalResolver() {
		this.withHttps = true;
	}

	@Override
	@SneakyThrows(IOException.class)
	public DIDDocument resolve(String did) {
		Verify.verify(did.startsWith(DIDGenerator.DID_WEB_PREFIX + ":"));

		String path = did.substring((DIDGenerator.DID_WEB_PREFIX + ":").length());
		path = path.replace(":", "/");

		URL url = new URL((withHttps ? "https" : "http") + "://" + path + "/.well-known/did.json");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		Verify.verify(conn.getResponseCode() <= 200);

		InputStream in = conn.getInputStream();
		String encoding = conn.getContentEncoding();
		encoding = encoding == null ? "UTF-8" : encoding;
		String body = IOUtils.toString(in, encoding);

		return DIDDocument.parse(body);
	}
}
