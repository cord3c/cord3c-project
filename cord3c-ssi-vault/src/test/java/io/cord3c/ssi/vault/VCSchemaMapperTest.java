package io.cord3c.ssi.vault;

import io.cord3c.common.test.VCTestUtils;
import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.cord3c.ssi.vault.db.ClaimEntity;
import io.cord3c.ssi.vault.db.CredentialEntity;
import io.cord3c.ssi.vault.db.VCSchemaMapper;
import io.cord3c.ssi.vault.db.VCSchemaMapperImpl;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

public class VCSchemaMapperTest implements WithAssertions {


	@Test
	public void verify() {
		VerifiableCredential credential = VCTestUtils.generateMockCredentials();

		VCSchemaMapper mapper = new VCSchemaMapperImpl();
		CredentialEntity entity = new CredentialEntity();
		mapper.toEntity(entity, credential);

		VerifiableCredential parsed = mapper.fromEntity(entity);
		assertThat(parsed).isEqualToComparingFieldByField(credential);

		assertThat(entity.getCredentialId()).isEqualTo(credential.getId());
		assertThat(entity.getIssuanceDate()).isEqualTo(credential.getIssuanceDate());
		assertThat(entity.getIssuer()).isEqualTo(credential.getIssuer());
		assertThat(entity.getExpirationDate()).isEqualTo(credential.getExpirationDate());
		assertThat(entity.getContexts()).isEqualTo(credential.getContexts());
		assertThat(entity.getTypes()).isEqualTo(credential.getTypes());
		assertThat(entity.getClaims().size()).isEqualTo(credential.getClaims().size());
		assertThat(entity.getClaims()).hasSize(2);
		ClaimEntity claimEntity = entity.getClaims().values().stream().filter(it -> it.getId().getName().equals("hello")).findFirst().get();
		assertThat(claimEntity.getId().getName()).isEqualTo("hello");
		assertThat(claimEntity.getId().getCredentialHashId()).isEqualTo(entity.getHashId());
		assertThat(claimEntity.getStringValue()).isEqualTo("world");
	}
}
