package io.cord3c.rest.server;

import io.crnk.core.engine.transaction.TransactionRunner;
import lombok.Data;
import net.corda.core.node.AppServiceHub;

@Data
public class RestContext {

	private TransactionRunner transactionRunner;

	private AppServiceHub serviceHub;

}
