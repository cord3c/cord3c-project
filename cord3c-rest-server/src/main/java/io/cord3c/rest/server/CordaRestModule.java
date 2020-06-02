package io.cord3c.rest.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cord3c.rest.server.internal.*;
import io.crnk.core.module.Module;
import lombok.RequiredArgsConstructor;
import net.corda.core.node.AppServiceHub;


@RequiredArgsConstructor
public class CordaRestModule implements Module {

	private final AppServiceHub serviceHub;

	@Override
	public String getModuleName() {
		return "cord3c-server-rest";
	}

	@Override
	public void setupModule(ModuleContext context) {
		ObjectMapper objectMapper = context.getObjectMapper();
		context.addRepository(new NodeRepositoryImpl(serviceHub));
		context.addRepository(new NotaryRepositoryImpl(serviceHub));
		context.addRepository(new PartyRepositoryImpl(serviceHub));
		context.addRepository(new RunningFlowRepositoryImpl(serviceHub, objectMapper));
		context.addRepository(new VaultStateRepositoryImpl());
	}
}
