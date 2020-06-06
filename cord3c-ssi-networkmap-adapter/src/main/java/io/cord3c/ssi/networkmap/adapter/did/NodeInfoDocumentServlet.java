package io.cord3c.ssi.networkmap.adapter.did;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.cord3c.rest.client.map.PartyDTO;
import io.cord3c.ssi.api.did.DIDPublicKey;
import io.cord3c.ssi.api.internal.DIDGenerator;
import io.cord3c.ssi.api.internal.W3CHelper;
import io.cord3c.ssi.api.vc.VCCrypto;
import io.cord3c.ssi.networkmap.adapter.VCNetworkMapProperties;
import io.cord3c.ssi.networkmap.adapter.repository.NodeMapRepositoryImpl;
import io.cord3c.ssi.api.did.DIDDocument;
import io.cord3c.ssi.corda.internal.party.PartyToDIDMapper;
import io.crnk.core.engine.internal.utils.UrlUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeInfoDocumentServlet extends HttpServlet {

	private final NodeMapRepositoryImpl repository;

	private final PartyToDIDMapper didMapper;

	private final VCCrypto crypto;

	@SneakyThrows
	public NodeInfoDocumentServlet(NodeMapRepositoryImpl repository, VCNetworkMapProperties properties, VCCrypto crypto) {
		this.didMapper = new PartyToDIDMapper(properties.getHost());
		this.crypto = crypto;
		this.repository = repository;
	}


	@Override
	@SneakyThrows
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		String pathInfo = request.getPathInfo();
		String shortName = UrlUtils.removeTrailingSlash(UrlUtils.removeLeadingSlash(pathInfo));
		String did = didMapper.toDid(shortName);

		PartyDTO party = repository.findParty(did);
		if (party == null) {
			log.info("did not found party with did={}", did);
			response.setStatus(404);
		} else {
			DIDPublicKey publicKey = crypto.toDidPublicKey(party.getOwningKey(), did);

			DIDDocument doc = new DIDDocument();
			doc.setContext(W3CHelper.DID_CONTEXT_V1);
			doc.setId(did);
			doc.getPublicKeys().add(publicKey);
			doc.getAuthentications().add(crypto.toAuthentication(publicKey));
			DIDServletWriter.write(response, doc);
		}
	}

}
