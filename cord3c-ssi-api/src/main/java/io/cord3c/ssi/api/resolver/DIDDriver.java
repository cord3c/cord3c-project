package io.cord3c.ssi.api.resolver;

import io.cord3c.ssi.api.did.DID;
import io.cord3c.ssi.api.did.DIDDocument;

public interface DIDDriver {

	String getMethod();

	DIDDocument resolve(DID did);
}
