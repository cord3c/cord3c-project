package io.cord3c.ssi.corda.vault;

import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.corda.vault.db.CredentialEntity;
import io.cord3c.ssi.corda.vault.db.VCSchemaMapper;
import kotlin.jvm.functions.Function1;
import net.corda.core.node.AppServiceHub;
import org.mapstruct.factory.Mappers;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.stream.Collectors;

public class VCVault {

	private final AppServiceHub serviceHub;

	private final VCSchemaMapper mapper = Mappers.getMapper(VCSchemaMapper.class);


	public VCVault(AppServiceHub serviceHub) {
		this.serviceHub = serviceHub;
	}

	public void record(VerifiableCredential credential) {
		record(Arrays.asList(credential));
	}

	/**
	 * Inserts or updates the given VCs.
	 *
	 * @param credentials
	 */
	public void record(List<VerifiableCredential> credentials) {
		withEntityManager(em -> {
			Set<String> ids = credentials.stream().map(it -> it.getId()).collect(Collectors.toSet());
			Map<String, CredentialEntity> existing = getEntities(ids).stream()
					.collect(Collectors.toMap(it -> it.getId(), it -> it));

			for (VerifiableCredential credential : credentials) {
				CredentialEntity entity = existing.get(credential.getId());
				if (entity == null) {
					entity = new CredentialEntity();
				}
				mapper.toEntity(entity, credential);
				em.persist(entity);
			}
			return null;
		});
	}

	public VerifiableCredential get(String id) {
		return withEntityManager(em -> {
			CredentialEntity entity = em.find(CredentialEntity.class, id);
			return mapper.fromEntity(entity);
		});
	}

	public List<VerifiableCredential> get(List<String> ids) {
		List<CredentialEntity> entities = getEntities(ids);
		return entities.stream().map(it -> mapper.fromEntity(it)).collect(Collectors.toList());
	}

	private List<CredentialEntity> getEntities(Collection<String> ids) {
		return withEntityManager(em -> {
			TypedQuery<CredentialEntity> query = em.createQuery("select c from CredentialEntity c where c.id IN :ids", CredentialEntity.class);
			query.setParameter("ids", ids);
			return query.getResultList();
		});
	}

	private <T> T withEntityManager(Function1<EntityManager, T> function) {
		return serviceHub.getDatabase().transaction(sessionScope -> serviceHub.withEntityManager((Function1<EntityManager, T>) function::invoke));
	}
}
