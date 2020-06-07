package io.cord3c.ssi.corda.serialization;

import io.cord3c.ssi.api.SSIFactory;
import io.cord3c.ssi.api.vc.VCCrypto;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.api.internal.W3CHelper;
import io.cord3c.ssi.corda.internal.party.CordaPartyRegistry;
import io.cord3c.common.test.setup.VCTestState;
import io.cord3c.ssi.corda.state.VCMapper;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
public class VCMapperTest implements WithAssertions {

	private VCMapper mapper;

	private VCCrypto crypto = new SSIFactory().getCrypto();

	private Party party0;

	private Party party1;

	private CordaPartyRegistry partyRegistry;

	@BeforeEach
	public void setup() {
		party0 = mockParty("STAR Labs");
		party1 = mockParty("Wayne Enterprises");

		Supplier<List<Party>> partySupplier = () -> Arrays.asList(party0, party1);

		partyRegistry = new CordaPartyRegistry(partySupplier);
		partyRegistry.setNetworkMapUrl("mock-networkmap.org");

		VCSerializationScheme scheme = new VCSerializationScheme(partyRegistry, "http://localhost");
		mapper = scheme.getCredentialMapper();
	}

	@Test
	public void verifyBidirectionalStateCredentialMapping() {
		VCTestState state = new VCTestState();
		state.setIssuerNode(party0);
		state.setSubjectNode(party1);
		state.setTimestamp(Instant.now());
		state.setValue(12);
		state.setId("foo");

		VerifiableCredential credential = mapper.toCredential(state);
		log.info("{}", credential);
		assertThat(credential.getIssuanceDate()).isEqualTo(state.getTimestamp());
		assertThat(credential.getClaims().get("value").intValue()).isEqualTo(12);
		assertThat(credential.getIssuer()).isEqualTo(partyRegistry.toDid(party0));
		assertThat(credential.getClaims().get(W3CHelper.CLAIM_SUBJECT_ID).textValue()).isEqualTo(partyRegistry.toDid(party1));
		assertThat(credential.getId()).isEqualTo("http://localhost/test/foo");

		VCTestState mappedState = mapper.fromCredential(credential);
		assertThat(mappedState).isEqualToComparingFieldByField(state);
	}

	private Party mockParty(String name) {
		CordaX500Name cordaX500Name = new CordaX500Name(name, "Mock City", "US");
		PublicKey publicKey = crypto.generateKeyPair().getPublic();
		return new Party(cordaX500Name, publicKey);
	}

}
