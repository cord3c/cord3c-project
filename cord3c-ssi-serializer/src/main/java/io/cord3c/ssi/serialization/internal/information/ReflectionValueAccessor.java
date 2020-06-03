package io.cord3c.ssi.serialization.internal.information;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.internal.utils.PropertyException;
import io.crnk.core.engine.internal.utils.PropertyUtils;

public class ReflectionValueAccessor<T> implements ValueAccessor<T> {

	private final Field privateField;

	private Method getter;

	private Method setter;

	private Field field;

	private Class<?> stateImplementationType;

	private String fieldName;

	private Class<?> fieldType;

	public ReflectionValueAccessor(Class<?> stateImplementationType, String fieldName, Class<?> fieldType) {
		if (fieldName == null) {
			throw new IllegalArgumentException("no fieldName provided");
		}
		if (stateImplementationType == null) {
			throw new IllegalArgumentException("no resourceType provided");
		}
		if (fieldType == null) {
			throw new IllegalArgumentException("no fieldType provided");
		}
		this.stateImplementationType = stateImplementationType;
		this.fieldName = fieldName;
		this.fieldType = fieldType;

		BeanInformation beanInformation = BeanInformation.get(stateImplementationType);
		BeanAttributeInformation attribute = beanInformation.getAttribute(fieldName);
		if (attribute != null) {
			this.getter = attribute.getGetter();
			this.setter = attribute.getSetter();
			this.field = attribute.getField();
		}
		this.privateField = field;
		if (field != null && !Modifier.isPublic(field.getModifiers())) {
			this.field = null;
		}

	}

	public Field getField() {
		return privateField;
	}

	@Override
	public T getValue(Object resource) {
		if (resource == null) {
			String message = String.format("Cannot get value %s.%s for null", stateImplementationType.getCanonicalName(), fieldName);
			throw new PropertyException(message, stateImplementationType, fieldName);
		}
		try {
			if (field != null) {
				return (T) field.get(resource);
			} else if (getter != null) {
				return (T) getter.invoke(resource);
			}
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new PropertyException(e, stateImplementationType, fieldName);
		}

		String message = String.format("Cannot find an getter for %s.%s", stateImplementationType.getCanonicalName(), fieldName);
		throw new PropertyException(message, stateImplementationType, fieldName);
	}

	@Override
	public void setValue(Object resource, T fieldValue) {
		if (resource == null) {
			String message = String.format("Cannot set value %s.%s for null", stateImplementationType.getCanonicalName(), fieldName);
			throw new PropertyException(message, stateImplementationType, fieldName);
		}
		try {
			Object mappedValue = PropertyUtils.prepareValue(fieldValue, fieldType);
			if (field != null) {
				field.set(resource, mappedValue);
			} else if (setter != null) {
				setter.invoke(resource, mappedValue);
			} else {
				String message = String.format("Cannot find an setter for %s.%s", stateImplementationType.getCanonicalName(), fieldName);
				throw new PropertyException(message, stateImplementationType, fieldName);
			}
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new PropertyException(e, stateImplementationType, fieldName);
		}

	}

	@Override
	public Class getImplementationClass() {
		return fieldType;
	}

}
