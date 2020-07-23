package io.cord3c.rest.api;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldNameConstants;
import net.corda.core.crypto.SecureHash;

@JsonApiResource(type = "flow")
@Data
@FieldNameConstants
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

	private boolean ended;

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
