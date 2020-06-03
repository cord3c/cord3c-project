package io.cord3c.ssi.serialization.internal;

import com.google.common.base.Verify;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.serialization.annotations.Claim;
import io.cord3c.ssi.serialization.annotations.Subject;
import io.cord3c.ssi.serialization.annotations.VerifiableCredentialType;
import io.cord3c.ssi.serialization.credential.EventState;
import io.cord3c.ssi.serialization.internal.information.ReflectionValueAccessor;
import io.cord3c.ssi.serialization.internal.information.ValueAccessor;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.identity.Party;
import net.corda.core.node.services.vault.CriteriaExpression;
import net.corda.core.node.services.vault.QueryCriteria;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class VerifiableCredentialUtils {


	public static <T> ValueAccessor<T> getValueForAnnotation(Class annotationClass, Class<T> stateImplementationClass, Object state) {
		assertIsVerifiableCredential(state);
		for (Field field : getAllFields(state)) {
			if (field.getAnnotation(annotationClass) != null) {
				return (ValueAccessor<T>) new ReflectionValueAccessor(stateImplementationClass, field.getName(), field.getType());
			}
		}
		throw new IllegalStateException("Class annotated with '@" + VerifiableCredential.class.getSimpleName() + "' must contain at least one field annotated with '@" + annotationClass.getSimpleName() + "'");
	}


	/////////////////////// FIXME

	public static VerifiableCredentialType assertIsVerifiableCredential(Object state) {
		VerifiableCredentialType annotation = state.getClass().getAnnotation(VerifiableCredentialType.class);
		Verify.verify(annotation != null, "State must be annotated with '@" + VerifiableCredential.class.getSimpleName() + "'");
		return annotation;
	}


	public static String getSubjectFieldName(EventState state) {
		assertIsVerifiableCredential(state);

		for (Field field : getAllFields(state)) {
			if (field.getAnnotation(Subject.class) != null) {
				// FIXME
				if (field.getName().endsWith("Id")) {
					int separation = field.getName().indexOf("Id");
					return field.getName().substring(0, separation) + "." + field.getName().substring(separation).toLowerCase();
				} else {
					return field.getName();
				}
			}
		}

		throw new IllegalStateException("Class annotated with '@" + VerifiableCredential.class.getSimpleName() + "' must contain at least one field annotated with '@" + Subject.class.getSimpleName() + "'");
	}

	public static Class getSubjectFieldClass(EventState state) {
		assertIsVerifiableCredential(state);

		for (Field field : getAllFields(state)) {
			if (field.getAnnotation(Subject.class) != null) {
				try {
					return FieldUtils.readField(field, state, true).getClass();
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("Could not access field '" + field.getName() + "' of class '" + state.getClass() + "'", e);
				}
			}
		}

		throw new IllegalStateException("Class annotated with '@" + VerifiableCredential.class.getSimpleName() + "' must contain at least one field annotated with '@" + Subject.class.getSimpleName() + "'");
	}

	private static List<Field> getAllFields(Object state) {
		List<Field> fields = new ArrayList<>();
		Class clazz = state.getClass();
		do {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		} while (clazz != null);

		return fields;
	}


	/**
	 * Make use of the {@link Claim} annotation to compare two states annotated with {@link VerifiableCredential}.
	 * <p>
	 * This is done by checking equality for fields annotated with {@link Subject} and {@link Claim}.
	 *
	 * @param state1 annotated with {@link VerifiableCredential}
	 * @param state2 annotated with {@link VerifiableCredential}
	 * @return true if both states are "equivalent", false otherwise
	 */
	public static boolean compareVerifiableCredentials(EventState state1, EventState state2) {
		assertIsVerifiableCredential(state1);
		assertIsVerifiableCredential(state2);

		Verify.verify(state1.getClass() == state2.getClass(), "Both states must be of the same class, instead got '" + state1.getClass() + "' and '" + state2.getClass() + "'.");

		return getAllFields(state1).stream().filter(f -> f.getAnnotationsByType(Claim.class).length >= 1).allMatch(f -> {
			try {
				return FieldUtils.readField(f, state1, true).equals(FieldUtils.readField(f, state2, true));
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Could not access field '" + f.getName() + "' while comparing claims", e);
			}
		});
	}

	/**
	 * Make use of the {@link Claim} annotation to create a {@link QueryCriteria}.
	 * <p>
	 * This is done by adding to a {@link QueryCriteria} a {@link CriteriaExpression} of every field annotated with {@link Claim}. We also add to those a {@link CriteriaExpression} for the field annotated with {@link Subject}.
	 * <p>
	 * All of those are then ANDed together.
	 *
	 * @param state annotated with {@link VerifiableCredential}
	 * @return a {@link QueryCriteria} tailored to that state
	 */
	/*
	@SneakyThrows(NoSuchFieldException.class)
	public static <T extends EventState> QueryCriteria getVerifiableCredentialCriteria(T state) {
		assertIsVerifiableCredential(state);

		Class<? extends EventState> clazz = state.getClass();
		Class<? extends EventState> entityClass = EventRegistry.INSTANCE.getTypeForState(clazz).getPersistenceClass();

		FieldInfo fieldInfo = QueryCriteriaUtils.getField(getSubjectFieldName(state), entityClass);
		QueryCriteria criteria = new VaultCustomQueryCriteria(Builder.equal(fieldInfo, getSubject(state)));

		for (Field field : getAllFields(state)) {
			if (field.getAnnotationsByType(Claim.class).length >= 1) {
				try {
					FieldInfo info = QueryCriteriaUtils.getField(field.getName(), entityClass);
					CriteriaExpression expression = Builder.equal(info, FieldUtils.readField(field, state, true));
					criteria = criteria.and(new VaultCustomQueryCriteria(expression));
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}
		}

		return criteria;
	}

	public static <T extends EventState> QuerySpec getVerifiableCredentialQuerySpec(T state) {
		assertIsVerifiableCredential(state);

		Class<? extends EventState> entityClass = EventRegistry.INSTANCE.getTypeForState(state.getClass()).getPersistenceClass();

		QuerySpec querySpec = new QuerySpec(entityClass);
		Class subjectClazz = getSubjectFieldClass(state);
		Object reconstructedSubject;
		if (subjectClazz == String.class) {
			reconstructedSubject = getSubject(state);
		} else if (subjectClazz == UniqueIdentifier.class) {
			reconstructedSubject = new UniqueIdentifier(null, UUID.fromString(getSubject(state)));
		} else if (subjectClazz == UUID.class) {
			reconstructedSubject = UUID.fromString(getSubject(state));
		} else {
			throw new IllegalStateException("We don't handle field annotated with '@" + Subject.class.getSimpleName() + "' to be of type '" + subjectClazz.getSimpleName() + "'");
		}
		querySpec.addFilter(new FilterSpec(PathSpec.of(getSubjectFieldName(state)), FilterOperator.EQ, reconstructedSubject));

		for (Field field : getAllFields(state)) {
			if (field.getAnnotationsByType(Claim.class).length >= 1) {
				try {
					Object value = FieldUtils.readField(field, state, true);
					querySpec.addFilter(new FilterSpec(PathSpec.of(field.getName()), FilterOperator.EQ, value));
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}
		}

		return querySpec;
	}
*/

	/**
	 * Make use of the {@link Claim} annotation to extract a description about all the verifiable credential's claims.
	 *
	 * @param state annotated with {@link VerifiableCredential}
	 * @return a short {@link String} description of the {@link VerifiableCredential}
	 */
	/*
	public static <T extends EventState> String getVerifiableCredentialsDescription(T state) {
		assertIsVerifiableCredential(state);

		Class<? extends EventState> clazz = state.getClass();

		String equalString = "=";
		String delimiterString = ", ";

		StringBuilder builder = new StringBuilder();

		builder.append(VerifiableCredential.class.getSimpleName() + " '" + clazz.getSimpleName() + "': ");
		builder.append(getSubjectFieldName(state) + "=");
		builder.append(getSubject(state));
		builder.append(delimiterString);

		for (Field field : getAllFields(state)) {
			if (field.getAnnotationsByType(Claim.class).length >= 1) {
				try {
					builder.append(field.getName());
					builder.append(equalString);
					builder.append(FieldUtils.readField(field, state, true).toString());
					builder.append(delimiterString);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("Could not access field '" + field.getName() + "' of class '" + state.getClass() + "'", e);
				}
			}
		}

		// remove the last delimiter
		builder.setLength(builder.length() - delimiterString.length());

		return builder.toString();
	}

	 */

}
