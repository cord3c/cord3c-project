package io.cord3c.example.cordapp;

import lombok.EqualsAndHashCode;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

public interface LeagueCommands extends CommandData {

	void verify(@NotNull LedgerTransaction tx, CommandWithParties<LeagueCommands> withParties);

	@EqualsAndHashCode
	class IssueMembership implements LeagueCommands {

		@Override
		public void verify(@NotNull LedgerTransaction tx, CommandWithParties<LeagueCommands> withParties) {
		}
	}
}
