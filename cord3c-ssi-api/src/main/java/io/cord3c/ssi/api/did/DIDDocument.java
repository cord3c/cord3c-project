package io.cord3c.ssi.api.did;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
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
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<PublicKey> publicKeys = new ArrayList<>();

	@JsonProperty(value = "authentication")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<Authentication> authentications = new ArrayList<>();

	@JsonProperty(value = "service")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<Service> services = new ArrayList<>();

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
