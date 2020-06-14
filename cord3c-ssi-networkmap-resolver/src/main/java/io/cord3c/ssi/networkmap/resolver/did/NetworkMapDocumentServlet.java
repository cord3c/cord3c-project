package io.cord3c.ssi.networkmap.resolver.did;

import io.cord3c.ssi.api.did.DIDDocument;
import io.cord3c.ssi.api.did.DIDPublicKey;
import io.cord3c.ssi.api.internal.DIDGenerator;
import io.cord3c.ssi.api.internal.W3CHelper;
import io.cord3c.ssi.api.vc.VCCrypto;
import io.cord3c.ssi.networkmap.resolver.NetworkMapResolverProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.cert.X509Certificate;

@Slf4j
@RequiredArgsConstructor
public class NetworkMapDocumentServlet extends HttpServlet {

	private final NetworkMapResolverProperties properties;

	private final VCCrypto crypt;

	@Override
	@SneakyThrows
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		X509Certificate rootCertificate = properties.getRootCertificate();

		String did = DIDGenerator.toWellKnownDid(properties.getUrl());
		DIDPublicKey publicKey = crypt.toDidPublicKey(rootCertificate.getPublicKey(), did);

		DIDDocument doc = new DIDDocument();
		doc.setContext(W3CHelper.DID_CONTEXT_V1);
		doc.setId(did);
		doc.getPublicKeys().add(publicKey);
		doc.getAuthentications().add(crypt.toAuthentication(publicKey));
		DIDServletWriter.write(response, doc);

		DIDServletWriter.write(response, doc);
	}

}
