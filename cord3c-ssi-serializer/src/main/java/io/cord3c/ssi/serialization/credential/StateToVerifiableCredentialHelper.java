package io.cord3c.ssi.serialization.credential;

import com.google.common.base.Verify;
import io.cord3c.ssi.api.vc.Proof;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.api.vc.W3CHelper;
import io.cord3c.ssi.serialization.annotations.Claim;
import io.cord3c.ssi.serialization.annotations.VerifiableCredentialType;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

public class StateToVerifiableCredentialHelper {

	public static VerifiableCredential toUnsignedVerifiableCredential(Object state) {
		VerifiableCredentialHelper.assertIsVerifiableCredential(state);

		List<String> contexts = getDefaultContexts();
		String issuerDid = VerifiableCredentialHelper.getIssuer(state);
		String claimId = deriveClaimId(state);
		List<String> types = deriveTypes(state);
		Map<String, Object> claims = deriveClaims(state);
		Proof unsignedProof = new Proof(W3CHelper.SECP256K1_SIGNATURE);

		return new VerifiableCredential(contexts, claimId, types, issuerDid, getTimestamp(state), claims, unsignedProof);
	}


	public static VerifiableCredential addJwsProof(VerifiableCredential verifiableCredential, Instant timestamp, String jwsToken) {
		Verify.verify(!verifiableCredential.isSigned(), "Verifiable Credential is already signed");

		Proof proof = new Proof(W3CHelper.SECP256K1_SIGNATURE, timestamp, W3CHelper.PROOF_PURPOSE_ASSERTION_METHOD, verifiableCredential.getIssuer(), jwsToken);

		verifiableCredential.setProof(proof);
		return verifiableCredential;
	}

	/*
		public static List<VerifiableCredential> toUnsignedVerifiableCredential(List states) {
		return states.stream().map(StateToVerifiableCredentialHelper::toUnsignedVerifiableCredential).collect(Collectors.toList());
	}

	public static VerifiableCredential toSignedVerifiableCredential(Object state, ECPrivateKey privateKey) {
		return toUnsignedVerifiableCredential(state).sign(privateKey);
	}

	public static List<VerifiableCredential> toSignedVerifiableCredential(List states, ECPrivateKey privateKey) {
		return states.stream().map(state -> toUnsignedVerifiableCredential(state).sign(privateKey)).collect(Collectors.toList());
	}


	public static <T extends EventEntityBase, E extends VerifiableCredentialSignature, R extends VerifiableCredentialSignatureRepository<E>> void signState(T entity, R verifiableCredentialSignatureRepository) {
		QuerySpec querySpec = new QuerySpec(verifiableCredentialSignatureRepository.getResourceClass());
		querySpec.addFilter(new FilterSpec(PathSpec.of("stateRef"), FilterOperator.EQ, entity.getStateRef()));
		if (!verifiableCredentialSignatureRepository.findAll(querySpec).isEmpty()) {
			throw new IllegalStateException("It seems like the state/entity has already been signed");
		}

		E verifiableCredentialSignatureEntity;
		try {
			verifiableCredentialSignatureEntity = verifiableCredentialSignatureRepository.getResourceClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("Could not create new instance of '" + verifiableCredentialSignatureRepository.getResourceClass().getSimpleName() + "'", e);
		}
		verifiableCredentialSignatureEntity.setId(UUID.randomUUID());
		verifiableCredentialSignatureEntity.setStateRef(entity.getStateRef());
		verifiableCredentialSignatureEntity.setEventType(entity.getTypeId());

		verifiableCredentialSignatureEntity = verifiableCredentialSignatureRepository.create(verifiableCredentialSignatureEntity);

		Verify.verifyNotNull(verifiableCredentialSignatureEntity.getTimestamp());
		Verify.verifyNotNull(verifiableCredentialSignatureEntity.getPublishLevel());
		Verify.verifyNotNull(verifiableCredentialSignatureEntity.getJwsToken());
	}


	 * Check if the state passed as input has already been signed (ie. created an entry into a repository extending {@link VerifiableCredentialSignatureRepository}).
	 * <p>
	 * If so, return the signed verifiable credential representation of the state.
	 *
	 * @param state                                   annotated with {@link VerifiableCredential}
	 * @param entityRepository                        repository for the entity corresponding to the state
	 * @param verifiableCredentialSignatureRepository instance of {@link VerifiableCredentialSignatureRepository}
	 * @param <T>
	 * @return
	 * @throws {@link IllegalStateException} if the state hasn't been signed or if more than 1 signatures are present
	public static <T extends EventState, E extends VerifiableCredentialSignature, R extends VerifiableCredentialSignatureRepository<E>> VerifiableCredential getSignedVerifiableCredential(T state, ResourceRepository entityRepository,
																																														   R verifiableCredentialSignatureRepository) {
		// find the entity of that state
		EventEntityBase entity = getEntityFromState(state, entityRepository);

		// look up if its corresponding VerifiableCredentialSignature already exists
		QuerySpec querySpec = new QuerySpec(verifiableCredentialSignatureRepository.getResourceClass());
		querySpec.addFilter(new FilterSpec(PathSpec.of(VaultState.Fields.stateRef), FilterOperator.EQ, entity.getStateRef()));
		ResourceList<E> list = verifiableCredentialSignatureRepository.findAll(querySpec);

		if (list.isEmpty()) {
			throw new IllegalStateException("No signature was found for a state/VerifiableCredential that is supposedly ours");
		} else if (list.size() == 1) {
			VerifiableCredential unsignedVC = toUnsignedVerifiableCredential(state);
			return addJwsProof(unsignedVC, list.get(0).getTimestamp(), list.get(0).getJwsToken());
		} else {
			throw new IllegalStateException("There shouldn't be more than 1 signature for a state (found " + list.size() + ")");
		}
	}

	private static <T extends EventState> EventEntityBase getEntityFromState(T state, ResourceRepository entityRepository) {
		QuerySpec querySpec = VerifiableCredentialHelper.getVerifiableCredentialQuerySpec(state);
		ResourceList entities = entityRepository.findAll(querySpec);
		Verify.verify(entities.size() <= 1, "There is more than 1 entity (found " + entities.size() + ")");
		Verify.verify(entities.size() == 1, "Couldn't find an entity that we previously issued");
		return (EventEntityBase) entities.get(0);
	}
*/
	private static List<String> getDefaultContexts() {
		List<String> contexts = new ArrayList<>();
		contexts.add(W3CHelper.DEFAULT_VC_CONTEXT_1);
		contexts.add(W3CHelper.DEFAULT_VC_CONTEXT_2);
		return contexts;
	}

	private static String deriveClaimId(Object state) {
		return W3CHelper.DEFAULT_VC_CONTEXT_1 + "/" + getTypes(state) + "/" + getTimestamp(state);
	}

	private static Instant getTimestamp(Object state) {
		if (state instanceof EventState) {
			OffsetDateTime timestamp = ((EventState) state).getTimestamp();
			return timestamp != null ? timestamp.toInstant() : null;
		}
		throw new IllegalStateException();
	}

	private static List<String> deriveTypes(Object state) {
		List<String> types = new ArrayList<>();
		types.add(W3CHelper.DEFAULT_VERIFIABLE_CREDENTIAL);
		types.addAll(getTypes(state));
		return types;
	}

	private static List<String> getTypes(Object state) {
		if (state instanceof TypedCredential) {
			return ((TypedCredential) state).getTypes();
		}
		VerifiableCredentialType annotation = state.getClass().getAnnotation(VerifiableCredentialType.class);
		String type = annotation.type();
		return Arrays.asList(type);
	}

	@SneakyThrows(IllegalAccessException.class)
	private static Map<String, Object> deriveClaims(Object state) {
		Map<String, Object> map = new LinkedHashMap<>();

		String subjectId = VerifiableCredentialHelper.getSubject(state);
		map.put(W3CHelper.CLAIM_SUBJECT_ID, subjectId);

		Verify.verify(nbOfClaimFields(state) >= 1, "Object of type '" + EventState.class.getSimpleName() + "' should contain at least one field annotated with '@" + Claim.class.getSimpleName() + "'");

		for (Field field : state.getClass().getDeclaredFields()) {
			if (field.getAnnotation(Claim.class) != null) {
				Object val = FieldUtils.readField(field, state, true);
				map.put(field.getName(), val);
			}
		}

		return map;
	}

	private static int nbOfClaimFields(Object state) {
		return (int) Arrays.stream(state.getClass().getDeclaredFields()).map(field -> field.getAnnotation(Claim.class)).filter(Objects::nonNull).count();
	}
}
