package io.cord3c.ssi.api.vc;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Proof {

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

	public Proof(String type) {
		this.type = type;
	}
}
