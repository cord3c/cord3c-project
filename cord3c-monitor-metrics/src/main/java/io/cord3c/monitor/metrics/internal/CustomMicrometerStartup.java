package io.cord3c.monitor.metrics.internal;

import com.google.auto.service.AutoService;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.SingletonSerializeAsToken;

@CordaService
@AutoService(SingletonSerializeAsToken.class)
public class CustomMicrometerStartup extends SingletonSerializeAsToken {

	public CustomMicrometerStartup(AppServiceHub appServiceHub) {
		new MicrometerDaemonThread(appServiceHub).start();
	}

}
