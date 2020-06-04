package io.cord3c.rest.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cord3c.rest.server.internal.*;
import io.crnk.core.module.Module;
import lombok.RequiredArgsConstructor;
import net.corda.core.node.AppServiceHub;


@RequiredArgsConstructor
public class CordaRestModule implements Module {

	private final AppServiceHub serviceHub;

	private final CordaMapper cordaMapper;

	@Override
	public String getModuleName() {
		return "cord3c-server-rest";
	}

	@Override
	public void setupModule(ModuleContext context) {
		ObjectMapper objectMapper = context.getObjectMapper();
		context.addRepository(new NodeRepositoryImpl(serviceHub, cordaMapper));
		context.addRepository(new NotaryRepositoryImpl(serviceHub, cordaMapper));
		context.addRepository(new PartyRepositoryImpl(serviceHub, cordaMapper));
		context.addRepository(new RunningFlowRepositoryImpl(serviceHub, objectMapper, cordaMapper));
		context.addRepository(new VaultStateRepositoryImpl(cordaMapper));
	}
}
