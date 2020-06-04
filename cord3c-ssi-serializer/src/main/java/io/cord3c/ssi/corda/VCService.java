package io.cord3c.ssi.corda;

import com.google.auto.service.AutoService;
import io.cord3c.ssi.corda.vault.VCVault;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.SingletonSerializeAsToken;

@CordaService
@Slf4j
@AutoService(SingletonSerializeAsToken.class)
public class VCService extends SingletonSerializeAsToken {

	private final AppServiceHub serviceHub;

	@Getter
	private final VCVault vault;

	public VCService(AppServiceHub serviceHub) {
		this.serviceHub = serviceHub;
		this.vault = new VCVault(serviceHub);
	}


}
