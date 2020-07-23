package io.cord3c.rest.api;

import io.crnk.core.repository.ResourceRepository;
import net.corda.core.schemas.PersistentStateRef;

public interface VaultStateRepository extends ResourceRepository<VaultStateDTO, PersistentStateRef> {

}
