package io.cord3c.ssi.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Verify;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.serialization.credential.*;
import net.corda.core.contracts.*;
import net.corda.core.crypto.SecureHash;
import net.corda.core.identity.Party;
import net.corda.core.transactions.WireTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StateSerializer extends StdSerializer<ContractState> {


	private final String networkMapHost;

	private final String serverUrl;

	protected StateSerializer(String networkMapHost, String serverUrl) {
		super(ContractState.class);
		this.networkMapHost = networkMapHost;
		this.serverUrl = serverUrl;
	}

	@Override
	public void serialize(ContractState state, JsonGenerator gen, SerializerProvider provider) throws IOException {


	}

}
