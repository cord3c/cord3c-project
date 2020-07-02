package io.cord3c.ssi.corda.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Verify;
import io.cord3c.ssi.api.rest.VCRepository;
import io.cord3c.ssi.api.rest.VerifiableCredentialDTO;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.vault.VCVault;
import io.cord3c.ssi.vault.db.ClaimEntity;
import io.cord3c.ssi.vault.db.CredentialEntity;
import io.cord3c.ssi.vault.db.VCSchemaMapper;
import io.cord3c.ssi.vault.db.VCSchemaMapperImpl;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.QuerySpecVisitorBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.jpa.JpaEntityRepositoryBase;
import io.crnk.data.jpa.JpaRepositoryConfig;
import io.crnk.data.jpa.mapping.JpaMapper;
import io.crnk.data.jpa.query.Tuple;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Supplier;

public class VCRepositoryImpl extends JpaEntityRepositoryBase<VerifiableCredentialDTO, String> implements VCRepository {

	private final Supplier<VCVault> vaultSupplier;

	public VCRepositoryImpl(Supplier<VCVault> vault) {
		super(JpaRepositoryConfig.builder(CredentialEntity.class, VerifiableCredentialDTO.class,
				new VCJpaMapper()).build());
		this.vaultSupplier = vault;
	}

	@Override
	public <S extends VerifiableCredentialDTO> S create(S credential) {
		return save(credential);
	}

	@Override
	public <S extends VerifiableCredentialDTO> S save(S credential) {
		VCVault vault = vaultSupplier.get();
		vault.record(credential.getCredential());
		return credential;
	}

	/**
	 * @param querySpec
	 * @return querySpec mapped to underlying JPA model.
	 */
	@Override
	protected QuerySpec optimizeQuerySpec(QuerySpec querySpec) {
		QuerySpec clone = querySpec.clone();
		clone.accept(new QuerySpecVisitorBase() {

			@Override
			public void visitPath(PathSpec pathSpec) {
				List<String> elements = pathSpec.getElements();
				if (VerifiableCredentialDTO.Fields.credential.equals(elements.get(0))) {
					pathSpec.setElements(elements.subList(1, elements.size()));
				}
			}

			@Override
			public boolean visitFilterEnd(FilterSpec filterSpec) {
				List<String> elements = filterSpec.getPath().getElements();
				if (VerifiableCredential.Fields.claims.equals(elements.get(0))) {
					Verify.verify(elements.size() == 2, "cannot do nested filtering for claims: %s", elements);

					Object value = filterSpec.getValue();

					if (value instanceof JsonNode) {
						JsonNode valueNode = (JsonNode) value;
						String dbField = ClaimEntity.selectField(valueNode);
						filterSpec.setPath(filterSpec.getPath().append(dbField));
						filterSpec.setValue(ClaimEntity.toValue(valueNode));
					} else {
						String dbField = ClaimEntity.selectField(value);
						filterSpec.setPath(filterSpec.getPath().append(dbField));
						filterSpec.setValue(value);
					}
				}
				return true;
			}
		});

		System.out.println("WTF: " + clone);
		System.out.println(clone.getFilters());

		return super.optimizeQuerySpec(clone);
	}

	@Override
	public ResourceList<VerifiableCredentialDTO> findAll(QuerySpec querySpec) {

		return super.findAll(querySpec);
	}

	@RequiredArgsConstructor
	public static class VCJpaMapper implements JpaMapper<CredentialEntity, VerifiableCredentialDTO> {

		private static final VCSchemaMapper MAPPER = new VCSchemaMapperImpl();

		@Override
		public VerifiableCredentialDTO map(Tuple tuple) {
			CredentialEntity entity = tuple.get(0, CredentialEntity.class);
			return new VerifiableCredentialDTO(MAPPER.fromEntity(entity));
		}

		@Override
		public CredentialEntity unmap(VerifiableCredentialDTO dto) {
			throw new UnsupportedOperationException();
		}
	}
}
