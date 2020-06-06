package io.cord3c.ssi.corda.internal;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.corda.core.transactions.WireTransaction;

public class WireTransactionDeserializer extends StdDeserializer<WireTransaction> {

	public WireTransactionDeserializer() {
		super(WireTransaction.class);
	}

	@Override
	public WireTransaction deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		return null;
	}
}
