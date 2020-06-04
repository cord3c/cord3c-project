package io.cord3c.ssi.corda.internal.information;

public interface ValueAccessor<T> {

	T getValue(Object state);

	void setValue(Object state, T fieldValue);

	Class<? extends T> getImplementationClass();
}
