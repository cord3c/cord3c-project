package io.cord3c.rest.api.map;

import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

import io.cord3c.rest.api.X500Name;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.Data;
import lombok.SneakyThrows;
import net.corda.core.crypto.Base58;

@Data
@JsonApiResource(type = "party")
public class PartyDTO {

	@JsonApiId
	private String id;

	private String did;

	private X500Name name;

	@SneakyThrows
	@Deprecated
	public java.security.PublicKey decodeOwningKey() {
		byte[] bytes = Base58.decode(owningKey);
		KeyFactory keyFactory = KeyFactory.getInstance("EdDSA");
		return keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
	}

	private String owningKey;

	// val certificate: X509Certificate
}
