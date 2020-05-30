package io.cord3c.monitor.ping;

import co.paralleluniverse.fibers.Suspendable;
import lombok.experimental.UtilityClass;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UtilityClass
public class PingFlow {

	@InitiatingFlow
	@StartableByRPC
	public static class PingFlowInitiator extends FlowLogic<Void> {

		private static final Logger LOGGER = LoggerFactory.getLogger(PingFlowInitiator.class);

		private final Party otherParty;

		public PingFlowInitiator(Party otherParty) {
			this.otherParty = otherParty;
		}

		@Suspendable
		@Override
		public Void call() throws FlowException {
			FlowSession otherPartySession = initiateFlow(otherParty);
			LOGGER.debug("Pinging: {}", otherParty);
			otherPartySession.sendAndReceive(String.class, "ping").unwrap(s -> s);

			return null;
		}
	}

	@InitiatedBy(PingFlowInitiator.class)
	public static class PingFlowAcceptor extends FlowLogic<Void> {

		private static final Logger LOGGER = LoggerFactory.getLogger(PingFlowAcceptor.class);

		private final FlowSession otherPartySession;

		public PingFlowAcceptor(FlowSession otherPartySession) {
			this.otherPartySession = otherPartySession;
		}

		@Suspendable
		@Override
		public Void call() throws FlowException {
			otherPartySession.receive(String.class).unwrap(s -> {
				LOGGER.debug("Received {} from: {}", s, otherPartySession.getCounterparty());
				return s;
			});
			otherPartySession.send("pong");

			return null;
		}
	}
}
