/*
 * Author : AdNovum Informatik AG
 */

package io.cord3c.ssi.corda.internal;

import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.auto.service.AutoService;
import io.cord3c.rest.server.RestConfigurer;
import io.cord3c.rest.server.RestContext;
import io.cord3c.ssi.corda.VCService;
import io.cord3c.ssi.vault.VCQueryEngine;
import io.cord3c.ssi.vault.VCVault;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.core.module.SimpleModule;
import net.corda.core.node.AppServiceHub;

@AutoService(RestConfigurer.class)
public class VCRestConfigurer implements RestConfigurer {


	@Override
	public void configure(RestContext context, CrnkBoot boot) {
		AppServiceHub serviceHub = context.getServiceHub();
		Supplier<VCVault> vaultSupplier = () -> serviceHub.cordaService(VCService.class).getVault();

		VCRepositoryImpl vcRepository = new VCRepositoryImpl(vaultSupplier);
		SimpleModule module = new SimpleModule("vc");
		module.addRepository(vcRepository);
		boot.addModule(module);

		TransactionRunner transactionRunner = context.getTransactionRunner();

		VCQueryEngine queryEngine =
				querySpec -> transactionRunner.doInTransaction(() -> vcRepository.findAll(querySpec))
						.stream()
						.map(it -> it.getCredential())
						.collect(Collectors.toList());
		VCVault.registerQueryEngine(serviceHub, queryEngine);
	}


}
