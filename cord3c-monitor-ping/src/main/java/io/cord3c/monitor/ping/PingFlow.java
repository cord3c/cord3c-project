package io.cord3c.monitor.ping;

import co.paralleluniverse.fibers.Suspendable;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.serialization.CordaSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;

@UtilityClass
public class PingFlow {


	@InitiatingFlow
	@StartableByRPC
	@RequiredArgsConstructor
	@StartableByService
	@Slf4j
	public static class PingFlowInitiator extends FlowLogic<PingMessage> {


		private final PingInput input;

		@Suspendable
		@Override
		public PingMessage call() throws FlowException {
			log.debug("performing ping: {}", input);
			CordaX500Name otherPartyName = CordaX500Name.parse(input.getOtherParty());
			Party party = getServiceHub().getIdentityService().wellKnownPartyFromX500Name(otherPartyName);
			FlowSession otherPartySession = initiateFlow(party);

			PingMessage message = new PingMessage();
			message.setMessage(input.getMessage());
			message.setData(input.getData());
			PingMessage pong = otherPartySession.sendAndReceive(PingMessage.class, message).unwrap(it -> it);
			log.debug("received pong: {}", pong);
			return pong;
		}
	}

	@InitiatedBy(PingFlowInitiator.class)
	@Slf4j
	public static class PingFlowAcceptor extends FlowLogic<Void> {

		private final FlowSession otherPartySession;

		public PingFlowAcceptor(FlowSession otherPartySession) {
			this.otherPartySession = otherPartySession;
		}

		@Suspendable
		@Override
		public Void call() throws FlowException {
			PingMessage request = otherPartySession.receive(PingMessage.class).unwrap(it -> {
				log.debug("received ping {} from {}", it, otherPartySession.getCounterparty());
				return it;
			});

			PingMessage response = new PingMessage();
			response.setMessage(request.getMessage().toUpperCase());
			otherPartySession.send(response);
			return null;
		}
	}
}
