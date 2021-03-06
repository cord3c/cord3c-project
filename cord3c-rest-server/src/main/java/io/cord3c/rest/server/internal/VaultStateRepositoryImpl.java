package io.cord3c.rest.server.internal;

import io.cord3c.rest.api.VaultStateDTO;
import io.cord3c.rest.api.VaultStateRepository;
import io.crnk.data.jpa.JpaEntityRepositoryBase;
import io.crnk.data.jpa.JpaRepositoryConfig;
import io.crnk.data.jpa.mapping.JpaMapper;
import io.crnk.data.jpa.query.Tuple;
import lombok.RequiredArgsConstructor;
import net.corda.core.schemas.PersistentStateRef;
import net.corda.node.services.vault.VaultSchemaV1;

public class VaultStateRepositoryImpl extends JpaEntityRepositoryBase<VaultStateDTO, PersistentStateRef> implements
		VaultStateRepository {


	public VaultStateRepositoryImpl(CordaMapper cordaMapper) {
		super(JpaRepositoryConfig.builder(VaultSchemaV1.VaultStates.class, VaultStateDTO.class,
				new VaultStateMapper(toVaultMapper(cordaMapper))).build());
	}

	private static VaultMapper toVaultMapper(CordaMapper cordaMapper) {
		VaultMapperImpl mapper = new VaultMapperImpl();
		mapper.setCordaMapper(cordaMapper);
		return mapper;
	}

	@RequiredArgsConstructor
	public static class VaultStateMapper implements JpaMapper<VaultSchemaV1.VaultStates, VaultStateDTO> {

		private final VaultMapper mapper;

		@Override
		public VaultStateDTO map(Tuple tuple) {
			VaultSchemaV1.VaultStates state = tuple.get(0, VaultSchemaV1.VaultStates.class);
			return mapper.map(state);
		}

		@Override
		public VaultSchemaV1.VaultStates unmap(VaultStateDTO dto) {
			throw new UnsupportedOperationException();
		}
	}

}
