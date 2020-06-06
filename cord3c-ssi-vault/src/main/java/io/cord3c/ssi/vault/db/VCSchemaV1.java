package io.cord3c.ssi.vault.db;

import com.google.common.collect.ImmutableList;
import net.corda.core.schemas.MappedSchema;

public class VCSchemaV1 extends MappedSchema {

	public VCSchemaV1() {
		super(VCSchema.class, 1,
				ImmutableList.of(CredentialEntity.class, ClaimEntity.class));
	}
}
