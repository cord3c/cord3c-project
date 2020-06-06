package io.cord3c.ssi.corda.state.credential;

import java.util.List;

/**
 * Allows a credential to hold its types dynamically rather than in the {@link VerifiableCredentialType#types}.
 */
public interface TypedCredential extends TransactionCredential {

	List<String> getTypes();

	void setTypes(List<String> typys);

}
