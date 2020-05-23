package io.cord3c.server.rest.internal;

import io.cord3c.server.rest.VaultStateDTO;
import io.cord3c.server.rest.VaultStateRepository;
import io.crnk.data.jpa.JpaEntityRepositoryBase;
import io.crnk.data.jpa.JpaRepositoryConfig;
import io.crnk.data.jpa.mapping.JpaMapper;
import io.crnk.data.jpa.query.Tuple;
import net.corda.core.schemas.PersistentStateRef;
import net.corda.node.services.vault.VaultSchemaV1;
import org.mapstruct.factory.Mappers;

public class VaultStateRepositoryImpl extends JpaEntityRepositoryBase<VaultStateDTO, PersistentStateRef> implements VaultStateRepository {


	public VaultStateRepositoryImpl() {
		super(JpaRepositoryConfig.builder(VaultSchemaV1.VaultStates.class, VaultStateDTO.class, new VaultStateMapper()).build());
	}

	public static class VaultStateMapper implements JpaMapper<VaultSchemaV1.VaultStates, VaultStateDTO> {

		private final CordaMapper mapper = Mappers.getMapper(CordaMapper.class);

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
