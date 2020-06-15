package io.cord3c.rest.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.*;
import net.corda.core.crypto.SecureHash;

import java.time.Instant;
import java.util.UUID;

@JsonApiResource(type = "flow")
@Data
public class FlowExecutionDTO<T> {

	public static final String ENDED_STEP = "Ended";

	@JsonApiId
	private UUID id;

	private String flowClass;

	private JsonNode parameters;

	private SecureHash transactionId;

	private String currentStep;

	private Instant lastModified;

	private JsonNode result;

	private JsonNode error;

	@JsonIgnore
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private Class resultType;

	@JsonIgnore
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private ObjectMapper objectMapper;

	@JsonIgnore
	@SneakyThrows
	public T toTypedResult() {
		return (T) objectMapper.treeToValue(result, resultType);
	}

	public void prepareResultMapper(Class<?> resultType, ObjectMapper objectMapper) {
		this.resultType = resultType;
		this.objectMapper = objectMapper;
	}
}
