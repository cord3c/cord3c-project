package io.cord3c.ssi.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.cord3c.ssi.api.did.Authentication;
import io.cord3c.ssi.api.did.DIDDocument;
import io.cord3c.ssi.api.did.DIDPublicKey;
import io.cord3c.ssi.api.did.Service;
import io.cord3c.ssi.api.internal.DIDGenerator;
import io.cord3c.ssi.api.internal.W3CHelper;
import io.cord3c.ssi.api.resolver.DefaultUniversalResolver;
import io.cord3c.ssi.api.vc.VCCrypto;
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

	private static final String TEST_DOMAIN = "http://test";

	@Test
	public void createDIDDocumentFileAndConvertBack() throws IOException {
		SSIFactory factory = new SSIFactory();
		VCCrypto crypto = factory.getCrypto();

		String did = DIDGenerator.generateRandomDid(TEST_DOMAIN);
		java.security.PublicKey publicKey = crypto.generateKeyPair().getPublic();

		DIDPublicKey didPublicKey = crypto.toDidPublicKey(publicKey, did);
		List<DIDPublicKey> publicKeys = Arrays.asList(didPublicKey);
		List<Authentication> authentications = Arrays.asList(crypto.toAuthentication(didPublicKey));
		List<Service> services = new ArrayList<>();

		DIDDocument didDocument = new DIDDocument(W3CHelper.DID_CONTEXT_V1, did, publicKeys, authentications, services);

		// Write DIDDocument to JSON
		File jsonFile = new File(tempDir, FILE_NAME);
		didDocument.writeJsonToFile(jsonFile);

		// Parse DIDDocument from JSON
		didDocument = DIDDocument.parse(jsonFile);

		assertThat(didDocument.getId()).isEqualTo(did);
		assertThat(didDocument.getPublicKeys().get(0).getType()).isEqualTo("EcdsaSecp256k1VerificationKey2019");
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
