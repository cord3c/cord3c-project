package io.cord3c.rest.api.types;

import com.google.auto.service.AutoService;
import io.crnk.client.module.ClientModuleFactory;

@AutoService(ClientModuleFactory.class)
public class CordaModuleFactory implements ClientModuleFactory {

	@Override
	public CordaTypesModule create() {
		return new CordaTypesModule();
	}
}
