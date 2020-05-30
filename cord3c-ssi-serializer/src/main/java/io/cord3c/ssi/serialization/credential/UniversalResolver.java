package io.cord3c.ssi.serialization.credential;


import io.cord3c.ssi.api.vc.DIDDocument;

public interface UniversalResolver {

	DIDDocument resolve(String did);
}
