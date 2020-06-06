package io.cord3c.common.test.setup;

import lombok.EqualsAndHashCode;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

public interface VCTestCommands extends CommandData {

	void verify(@NotNull LedgerTransaction tx, CommandWithParties<VCTestCommands> withParties);

	@EqualsAndHashCode
	class Test implements VCTestCommands {

		@Override
		public void verify(@NotNull LedgerTransaction tx, CommandWithParties<VCTestCommands> withParties) {
		}
	}
}
