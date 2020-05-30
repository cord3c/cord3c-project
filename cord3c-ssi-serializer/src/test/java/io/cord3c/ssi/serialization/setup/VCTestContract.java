package io.cord3c.ssi.serialization.setup;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

public class VCTestContract implements Contract {

    @Override
    public void verify(@NotNull LedgerTransaction tx) {
        CommandWithParties<VCTestCommands> withParties = requireSingleCommand(tx.getCommands(), VCTestCommands.class);
        withParties.getValue().verify(tx, withParties);
    }
}
