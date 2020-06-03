package io.cord3c.ssi.serialization.internal.mapper;

public class RepositoryUtils {



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
}
