package io.cord3c.rest.api.types;

import io.cord3c.rest.api.types.internal.PersistentStateRefStringMapper;
import io.crnk.core.module.Module;
import net.corda.core.schemas.PersistentStateRef;

public class CordaTypesModule implements Module {

	@Override
	public String getModuleName() {
		return "cord3c.types";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.getTypeParser().addMapper(PersistentStateRef.class, new PersistentStateRefStringMapper());
	}
}
