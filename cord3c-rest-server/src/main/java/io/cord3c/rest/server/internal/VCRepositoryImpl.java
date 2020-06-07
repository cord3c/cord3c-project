package io.cord3c.rest.server.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Verify;
import io.cord3c.rest.client.VCRepository;
import io.cord3c.rest.client.VerifiableCredentialDTO;
import io.cord3c.ssi.api.internal.hashlink.Base58;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.vault.VCVault;
import io.cord3c.ssi.vault.db.ClaimEntity;
import io.cord3c.ssi.vault.db.CredentialEntity;
import io.cord3c.ssi.vault.db.VCSchemaMapper;
import io.crnk.core.queryspec.*;
import io.crnk.data.jpa.JpaEntityRepositoryBase;
import io.crnk.data.jpa.JpaRepositoryConfig;
import io.crnk.data.jpa.mapping.JpaMapper;
import io.crnk.data.jpa.query.Tuple;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VCRepositoryImpl extends JpaEntityRepositoryBase<VerifiableCredentialDTO, String> implements VCRepository {

	private final VCVault vault;

	public VCRepositoryImpl(VCVault vault) {
		super(JpaRepositoryConfig.builder(CredentialEntity.class, VerifiableCredentialDTO.class,
				new VCJpaMapper()).build());
		this.vault = vault;

		this.vault.setQueryEngine((QuerySpec querySpec) -> this.findAll(querySpec).stream().map(it -> it.getCredential()).collect(Collectors.toList()));
	}

	@Override
	public <S extends VerifiableCredentialDTO> S create(S credential) {
		return save(credential);
	}

	@Override
	public <S extends VerifiableCredentialDTO> S save(S credential) {
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

		Optional<FilterSpec> optIdFilter = clone.findFilter(PathSpec.of(VerifiableCredentialDTO.Fields.id));
		if (optIdFilter.isPresent()) {
			FilterSpec filterSpec = optIdFilter.get();
			filterSpec.setPath(PathSpec.of("hashId"));
		}

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

					JsonNode value = filterSpec.getValue();
					String dbField = ClaimEntity.selectField(value);
					filterSpec.setPath(filterSpec.getPath().append(dbField));
					filterSpec.setValue(ClaimEntity.toValue(value));
				}
				return true;
			}
		});

		return super.optimizeQuerySpec(clone);
	}

	@RequiredArgsConstructor
	public static class VCJpaMapper implements JpaMapper<CredentialEntity, VerifiableCredentialDTO> {

		private static final VCSchemaMapper MAPPER = Mappers.getMapper(VCSchemaMapper.class);

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
