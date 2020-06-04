package io.cord3c.ssi.corda.internal.information;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;

public class ReflectionValueAccessor<T> implements ValueAccessor<T> {

	private final BeanAttributeInformation beanAttributeInformation;

	public ReflectionValueAccessor(BeanAttributeInformation beanAttributeInformation) {
		this.beanAttributeInformation = beanAttributeInformation;

	}

	@Override
	public T getValue(Object resource) {
		return (T) beanAttributeInformation.getValue(resource);
	}

	@Override
	public void setValue(Object resource, T fieldValue) {
		beanAttributeInformation.setValue(resource, fieldValue);
	}

	@Override
	public Class getImplementationClass() {
		return beanAttributeInformation.getImplementationClass();
	}
}
