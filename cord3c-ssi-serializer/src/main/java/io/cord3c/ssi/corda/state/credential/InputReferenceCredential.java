package io.cord3c.ssi.corda.state.credential;

import net.corda.core.contracts.StateRef;

import java.util.List;

/**
 * Allows a credential to hold input and reference states.
 */
public interface InputReferenceCredential extends TransactionCredential {

	List<StateRef> getReferences();

	void setReferences(List<StateRef> stateRefs);

	void setInputs(List<StateRef> stateRefs);

	List<StateRef> getInputs();
}
