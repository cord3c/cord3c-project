package io.cord3c.ssi.corda.state.credential;

/**
 * Base implementation for any credential that can also be treated as a corda transaction. Other kind of
 * redentials can still participate in transactions as input or o output
 * state using {@link GenericTransactionCredential}.
 */
public interface TransactionCredential {

	String getNetwork();

	void setNetwork(String network);

	byte[] getPrivacySalt();

	void setPrivacySalt(byte[] privacySalt);

}
