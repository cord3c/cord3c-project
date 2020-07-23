package io.cord3c.ssi.api.did;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DIDPublicKey {

	public static final String EC_KEY_ALG = "EC";

	private String id;

	private String controller;

	private String type;

	private String publicKeyBase58;

}
