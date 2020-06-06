package io.cord3c.ssi.vault.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.SerializeType;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;

/**
 * Represents a state in the history of a given identity.
 */
@Getter
@Setter
@JsonApiResource(type = "claim", nested = true)
@Entity
@Table(name = "c3c_claim")
@FieldNameConstants
public class ClaimEntity {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Id
	@JsonApiId
	private ClaimId id;

	@JsonIgnore
	private Long longValue;

	@JsonIgnore
	private Double doubleValue;

	@JsonIgnore
	private Boolean booleanValue;

	@JsonIgnore
	private String stringValue;

	@JsonIgnore
	private String jsonValue;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({@JoinColumn(name = "claim_id", insertable = false, updatable = false)})
	@JsonApiRelation(idField = "claimId", opposite = "claims", serialize = SerializeType.ONLY_ID)
	@ToString.Exclude
	private CredentialEntity credential;

	/**
	 * @param value
	 * @return field used to store the given value based on type
	 */
	public static String selectField(JsonNode value) {
		if (value.isLong()) {
			return Fields.longValue;
		} else if (value.isDouble()) {
			return Fields.doubleValue;
		} else if (value.isTextual()) {
			return Fields.stringValue;
		} else if (value.isNull()) {
			throw new UnsupportedOperationException("filtering by null not implemented");
		} else if (value.isBoolean()) {
			return Fields.booleanValue;
		} else {
			return Fields.jsonValue;
		}
	}

	public static Object toValue(JsonNode value) {
		if (value.isLong()) {
			return value.longValue();
		} else if (value.isDouble()) {
			return value.doubleValue();
		} else if (value.isTextual()) {
			return value.textValue();
		} else if (value.isNull()) {
			throw new UnsupportedOperationException("filtering by null not implemented");
		} else if (value.isBoolean()) {
			return value.booleanValue();
		} else {
			return value.textValue();
		}
	}

	@SneakyThrows
	public JsonNode getValue() {
		if (stringValue != null) {
			return objectMapper.valueToTree(stringValue);
		}
		if (booleanValue != null) {
			return objectMapper.valueToTree(booleanValue);
		}
		if (longValue != null) {
			return objectMapper.valueToTree(longValue);
		}
		if (doubleValue != null) {
			return objectMapper.valueToTree(doubleValue);
		}
		return objectMapper.readTree(jsonValue);
	}

	@SneakyThrows
	public void setValue(JsonNode value) {
		stringValue = null;
		jsonValue = null;
		longValue = null;
		booleanValue = null;
		doubleValue = null;
		if (value.isLong()) {
			setLongValue(value.longValue());
		} else if (value.isDouble()) {
			setDoubleValue(value.doubleValue());
		} else if (value.isTextual()) {
			setStringValue(value.textValue());
		} else if (value.isNull()) {
			// nothing to do
		} else if (value.isBoolean()) {
			setBooleanValue(value.booleanValue());
		} else {
			setJsonValue(objectMapper.writeValueAsString(value));
		}
	}
}
