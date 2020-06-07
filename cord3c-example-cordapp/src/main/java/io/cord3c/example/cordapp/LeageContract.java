package io.cord3c.example.cordapp;

import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

public class LeageContract implements Contract {

    @Override
    public void verify(@NotNull LedgerTransaction tx) {
        CommandWithParties<LeageCommands> withParties = requireSingleCommand(tx.getCommands(), LeageCommands.class);
        withParties.getValue().verify(tx, withParties);
    }
}
