package io.cord3c.ssi.api.resolver;


import io.cord3c.ssi.api.did.DIDDocument;

public interface UniversalResolver {

	DIDDocument resolve(String did);
}
