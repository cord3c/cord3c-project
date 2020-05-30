package io.cord3c.ssi.networkmap.adapter.did;

import java.io.Writer;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.cord3c.ssi.networkmap.adapter.repository.NodeInfoRepository;
import io.cord3c.ssi.api.vc.DIDDocument;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NodeInfoDocumentServlet extends HttpServlet {

	private final NodeInfoRepository repository;

	@Override
	@SneakyThrows
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {

		DIDDocument doc = null;// list.get(0).getDocument();

		Writer writer = response.getWriter();
		doc.writePrettyJsonToWriter(writer);
		writer.close();

		response.setStatus(200);
		response.setContentType("application/json");
	}
}
