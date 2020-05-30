package io.cord3c.server.rest;

import io.cord3c.server.rest.internal.NodeRepositoryImpl;
import io.cord3c.server.rest.internal.VaultStateRepositoryImpl;
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
		context.addRepository(new NodeRepositoryImpl(serviceHub));
		context.addRepository(new NodeRepositoryImpl(serviceHub));
		context.addRepository(new VaultStateRepositoryImpl());
	}
}
