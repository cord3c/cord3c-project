package io.cord3c.ssi.networkmap.adapter.did;

import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.cord3c.ssi.api.did.DIDDocument;
import io.cord3c.ssi.networkmap.adapter.VCNetworkMapProperties;
import io.cord3c.ssi.networkmap.adapter.repository.VCNetworkMapUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NetworkMapDocumentServlet extends HttpServlet {

	private final VCNetworkMapProperties properties;

	@Override
	@SneakyThrows
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		X509Certificate rootCertificate = properties.getRootCertificate();

		DIDDocument doc = new DIDDocument();

		VCNetworkMapUtils.writeDid(response, doc);
	}

}
