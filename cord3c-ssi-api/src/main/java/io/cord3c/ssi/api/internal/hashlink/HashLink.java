package io.cord3c.ssi.api.internal.hashlink;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@UtilityClass
public class HashLink {

			/*
			meta encoding currently not used:
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new CborEncoder(baos).encode(new CborBuilder()
				.add(text)
				.addArray().add("http://example.org/hw.txt").end()
				.add("text/plain")
				.build());
		byte[] encodedBytes = baos.toByteArray();
		 */

	@SneakyThrows
	public String create(String text) {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
		Multihash multihash = new Multihash(Multihash.Type.sha2_256, hash);
		return Multibase.encode(Multibase.Base.Base58BTC, multihash.toBytes());
	}
}
