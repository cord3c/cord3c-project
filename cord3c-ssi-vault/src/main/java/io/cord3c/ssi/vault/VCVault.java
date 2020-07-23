package io.cord3c.ssi.vault;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.common.base.Verify;
import io.cord3c.ssi.api.rest.VerifiableCredentialDTO;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.vault.db.CredentialEntity;
import io.cord3c.ssi.vault.db.VCSchemaMapper;
import io.cord3c.ssi.vault.db.VCSchemaMapperImpl;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import kotlin.jvm.functions.Function1;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.node.AppServiceHub;

@Slf4j
public class VCVault {


	// unfortunately, CordaService has no lifecycle methods, so we must do VCVault/VCQueryEngineBinding lazily
	private static ConcurrentHashMap<Object, VCQueryEngine> queryEngineBinding = new ConcurrentHashMap<>();

	private final AppServiceHub serviceHub;

	private final VCSchemaMapper mapper = new VCSchemaMapperImpl();

	private VCQueryEngine queryEngine;

	public VCVault(AppServiceHub serviceHub) {
		this.serviceHub = serviceHub;

		log.info("initializing VC vault");
	}

	@Deprecated // try to get rid of that
	public static void registerQueryEngine(AppServiceHub serviceHub, VCQueryEngine queryEngine) {
		queryEngineBinding.put(serviceHub.getDatabase(), queryEngine);
	}

	private void requireQueryEngine() {
		if (queryEngine == null) {
			// attempt lazy binding
			queryEngine = queryEngineBinding.get(serviceHub.getDatabase());
		}
		if (queryEngine == null) {
			throw new IllegalStateException(
					"currently no query engine added to VCVault. For now it is as VCRepository in cord3c-rest-server cordapp. In"
							+ " the future this dependency limitation will likely go away.");
		}
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
			Set<String> hashIds = credentials.stream().map(it -> it.toHashId()).collect(Collectors.toSet());
			Map<String, CredentialEntity> existing = getEntities(hashIds).stream()
					.collect(Collectors.toMap(it -> it.getHashId(), it -> it));

			for (VerifiableCredential credential : credentials) {
				Verify.verify(credential.getId() != null);
				String hashId = credential.toHashId();
				CredentialEntity entity = existing.get(hashId);
				if (entity == null) {
					entity = new CredentialEntity();
					mapper.toEntity(entity, credential);
					Verify.verify(entity.getCredentialId() != null);
					em.persist(entity);
				}
			}
			return null;
		});
	}

	public void delete(VerifiableCredential credential) {
		record(Arrays.asList(credential));
	}

	public void delete(List<VerifiableCredential> credentials) {
		withEntityManager(em -> {
			for (VerifiableCredential credential : credentials) {
				String id = credential.toHashId();
				CredentialEntity entity = em.find(CredentialEntity.class, id);
				if (entity != null) {
					em.remove(entity);
				}
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

	public List<VerifiableCredential> find(FilterSpec filterSpec) {
		QuerySpec querySpec = new QuerySpec(VerifiableCredentialDTO.class);
		querySpec.addFilter(filterSpec);
		return find(querySpec);
	}

	public List<VerifiableCredential> find(QuerySpec querySpec) {
		requireQueryEngine();
		return queryEngine.invoke(querySpec);
	}

	private List<CredentialEntity> getEntities(Collection<String> ids) {
		return withEntityManager(em -> {
			TypedQuery<CredentialEntity> query =
					em.createQuery("select c from CredentialEntity c where c.hashId IN :ids", CredentialEntity.class);
			query.setParameter("ids", ids);
			return query.getResultList();
		});
	}

	private <T> T withEntityManager(Function1<EntityManager, T> function) {
		return serviceHub.getDatabase()
				.transaction(sessionScope -> serviceHub.withEntityManager((Function1<EntityManager, T>) function::invoke));
	}

	public void setQueryEngine(VCQueryEngine queryEngine) {
		log.info("initializing VC query engine");
		this.queryEngine = queryEngine;
	}
}
