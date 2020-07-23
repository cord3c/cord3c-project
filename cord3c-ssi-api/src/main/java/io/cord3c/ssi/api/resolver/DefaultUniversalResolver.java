package io.cord3c.ssi.api.resolver;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Verify;
import io.cord3c.ssi.api.did.DID;
import io.cord3c.ssi.api.did.DIDDocument;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultUniversalResolver implements UniversalResolver {


	private Map<String, DIDDriver> driverMap = new ConcurrentHashMap<>();

	public DefaultUniversalResolver() {
		registerDriver(new WebDriver());
	}

	public void registerDriver(DIDDriver driver) {
		driverMap.put(driver.getMethod(), driver);
	}

	@Override
	public DIDDocument resolve(String did) {
		DID didObject = DID.fromString(did);
		String method = didObject.getMethod();
		DIDDriver driver = driverMap.get(method);
		Verify.verify(driver != null, "no driver found for method=%s", method);
		return driver.resolve(didObject);
	}

	public Set<String> getSupportedMethods() {
		return driverMap.keySet();
	}
}
