package io.cord3c.ssi.api.did;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Secp256K1PublicKey {

	private String id;

	private String controller;

	private String type;

	private String publicKeyBase58;
}
