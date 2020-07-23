package io.cord3c.rest.api.types.internal;

import io.crnk.core.engine.parser.StringMapper;
import net.corda.core.schemas.PersistentStateRef;

public class PersistentStateRefStringMapper implements StringMapper<PersistentStateRef> {

	public static final PersistentStateRefStringMapper INSTANCE = new PersistentStateRefStringMapper();

	@Override
	public PersistentStateRef parse(String input) {
		int sep = input.lastIndexOf("-");
		String txId = input.substring(0, sep);
		String number = input.substring(sep + 1);
		return new PersistentStateRef(txId, Integer.parseInt(number));
	}

	@Override
	public String toString(PersistentStateRef stateRef) {
		return stateRef.getTxId() + "-" + stateRef.getIndex();
	}
}
