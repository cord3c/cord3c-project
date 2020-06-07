package io.cord3c.example.cordapp;

import lombok.EqualsAndHashCode;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

public interface LeageCommands extends CommandData {

	void verify(@NotNull LedgerTransaction tx, CommandWithParties<LeageCommands> withParties);

	@EqualsAndHashCode
	class IssueMembership implements LeageCommands {

		@Override
		public void verify(@NotNull LedgerTransaction tx, CommandWithParties<LeageCommands> withParties) {
		}
	}
}
