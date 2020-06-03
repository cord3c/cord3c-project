package io.cord3c.ssi.api.did;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DIDDocument {

	@JsonProperty(value = "@context")
	private String context;

	private String id;

	@JsonProperty(value = "publicKey")
	private List<Secp256K1PublicKey> publicKeys;

	@JsonProperty(value = "authentication")
	private List<Authentication> authentications;

	@JsonProperty(value = "service")
	private List<Service> services;

	private static ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

	public void writeJsonToFile(File file) throws IOException {
		mapper.writeValue(file, this);
	}

	public void writePrettyJsonToWriter(Writer writer) throws IOException {
		mapper.writerWithDefaultPrettyPrinter().writeValue(writer, this);
	}

	public static DIDDocument parse(File jsonFile) throws IOException {
		return mapper.readValue(jsonFile, DIDDocument.class);
	}

	public static DIDDocument parse(String jsonString) throws IOException {
		return mapper.readValue(jsonString, DIDDocument.class);
	}
}
