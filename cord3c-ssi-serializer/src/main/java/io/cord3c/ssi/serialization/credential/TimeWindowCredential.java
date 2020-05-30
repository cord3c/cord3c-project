package io.cord3c.ssi.serialization.credential;

import net.corda.core.contracts.TimeWindow;

/**
 * Allows a credential to carry a time window that is mapped to a Corda transaction time window.
 */
public interface TimeWindowCredential extends TransactionCredential {

	TimeWindow getTimeWindow();

	void setTimeWindow(TimeWindow timeWindow);

}
