package io.cord3c.ssi.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.cord3c.ssi.api.did.Authentication;
import io.cord3c.ssi.api.did.DIDDocument;
import io.cord3c.ssi.api.did.PublicKey;
import io.cord3c.ssi.api.did.Service;
import io.cord3c.ssi.api.internal.DIDGenerator;
import io.cord3c.ssi.api.vc.*;
import net.corda.core.crypto.Base58;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;

@TestInstance(Lifecycle.PER_CLASS)
public class DIDDocumentTest implements WithAssertions {

	@TempDir
	File tempDir;

	private static final String FILE_NAME = "DIDDocument.txt";

	private static final String FILE_NAME_RECONSTRUCTED = "DIDDocumentReconstructed.txt";

	private static final String FILE_NAME_MISSING_PUBLIC_KEY_FIELDS = "src/test/data" + "/ExampleDIDDocumentWithMissingPublicKeyFields.txt";

	private static final String TEST_DOMAIN = "test";

	@Test
	public void createDIDDocumentFileAndConvertBack() throws IOException {
		String did = DIDGenerator.generateRandomDid(TEST_DOMAIN);
		java.security.PublicKey publicKey = KeyFactoryHelper.generateKeyPair().getPublic();
		List<PublicKey> publicKeys = new ArrayList<>();
		String publicKeyHex = Base58.encode(publicKey.getEncoded());
		publicKeys.add(new PublicKey(did + "#keys-1", did, publicKey.getAlgorithm(), publicKeyHex));
		List<Authentication> authentications = new ArrayList<>();
		authentications.add(new Authentication(W3CHelper.JwsVerificationKey2020, Arrays.asList(publicKeys.get(0).getId())));
		List<Service> services = new ArrayList<>();

		DIDDocument didDocument = new DIDDocument(W3CHelper.DID_CONTEXT_V1, did, publicKeys, authentications, services);

		// Write DIDDocument to JSON
		File jsonFile = new File(tempDir, FILE_NAME);
		didDocument.writeJsonToFile(jsonFile);

		// Parse DIDDocument from JSON
		didDocument = DIDDocument.parse(jsonFile);

		assertThat(didDocument.getId()).isEqualTo(did);
		assertThat(didDocument.getPublicKeys().get(0).getType()).isEqualTo(publicKey.getAlgorithm());
		byte[] convertedPublicKeyHex = Base58.decode(didDocument.getPublicKeys().get(0).getPublicKeyBase58());
		assertThat(convertedPublicKeyHex).isEqualTo(publicKey.getEncoded());

		// Write the reconstructed DIDDocument to a file
		File jsonFileReconstructed = new File(tempDir, FILE_NAME_RECONSTRUCTED);
		didDocument.writeJsonToFile(jsonFileReconstructed);

		// Compare both files
		assertThat(Files.readAllBytes(jsonFile.toPath())).isEqualTo(Files.readAllBytes(jsonFileReconstructed.toPath()));
	}

	@Test
	public void tryParsingDIDDocumentWithMissingPublicKeyFields() {
		File file = new File(FILE_NAME_MISSING_PUBLIC_KEY_FIELDS);

		assertThatThrownBy(() -> DIDDocument.parse(file)).isInstanceOf(UnrecognizedPropertyException.class).hasMessageContaining("Unrecognized field \"publicKeys\"");
	}

}
