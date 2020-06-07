package io.cord3c.example.cordapp;

import co.paralleluniverse.fibers.Suspendable;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.corda.VCService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.CordaSerializable;

import java.time.Instant;

@UtilityClass
public class IssueLeagueMembership {

	@CordaSerializable
	@Data
	public class IssueMembershipInput {

		private String name;

		private String did;

	}


	@InitiatingFlow
	@StartableByRPC
	@RequiredArgsConstructor
	@StartableByService
	@Slf4j
	public static class IssueMembershipInititor extends FlowLogic<VerifiableCredential> {


		private final IssueMembershipInput input;

		@Suspendable
		@Override
		public VerifiableCredential call() throws FlowException {
			LeagueMembership membership = new LeagueMembership();
			membership.setTimestamp(Instant.now());
			membership.setName(input.getName());
			membership.setSubject(input.getDid());
			membership.setIssuer(getOurIdentity());

			VCService vcService = getServiceHub().cordaService(VCService.class);

			return vcService.issue(membership);
		}
	}

	/*
	@InitiatedBy(IssueMembershipInititor.class)
	@Slf4j
	public static class PingFlowAcceptor extends FlowLogic<Void> {

		private final FlowSession otherPartySession;

		public PingFlowAcceptor(FlowSession otherPartySession) {
			this.otherPartySession = otherPartySession;
		}

		@Suspendable
		@Override
		public Void call() throws FlowException {
			io.cord3c.example.cordapp.PingMessage request = otherPartySession.receive(io.cord3c.example.cordapp.PingMessage.class).unwrap(it -> {
				log.debug("received ping {} from {}", it, otherPartySession.getCounterparty());
				return it;
			});

			io.cord3c.example.cordapp.PingMessage response = new PingMessage();
			response.setMessage(request.getMessage().toUpperCase());
			otherPartySession.send(response);
			return null;
		}
	}
	 */
}
