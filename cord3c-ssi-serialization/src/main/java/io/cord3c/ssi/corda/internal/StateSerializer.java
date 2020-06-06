package io.cord3c.ssi.corda.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.corda.core.contracts.*;

import java.io.IOException;

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
