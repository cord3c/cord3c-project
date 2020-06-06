package io.cord3c.ssi.vault.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.time.Instant;

@Data
@Embeddable
public class ProofEmbeddable {

	private String type;

	@JsonInclude(Include.NON_NULL)
	@JsonSerialize(using = ToStringSerializer.class)
	private Instant created;

	@JsonInclude(Include.NON_NULL)
	private String proofPurpose;

	@JsonInclude(Include.NON_NULL)
	private String verificationMethod;

	@JsonInclude(Include.NON_NULL)
	@Setter
	private String jws;

}
