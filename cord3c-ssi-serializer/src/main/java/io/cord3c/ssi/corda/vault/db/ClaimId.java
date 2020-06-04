package io.cord3c.ssi.corda.vault.db;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@JsonSerialize(using = ToStringSerializer.class)
@Embeddable
public class ClaimId implements Serializable {

	@JsonApiRelationId
	@Column(name = "claim_id")
	private String claimId;

	@Column
	private String name;

	public ClaimId() {
	}

	public ClaimId(String idString) {
		int sep = idString.lastIndexOf("#"); // FIXME good separator?
		claimId = idString.substring(0, sep);
		name = idString.substring(sep + 1);
	}

	public ClaimId(String claimId, String name) {
		this.claimId = claimId;
		this.name = name;
	}

	@Override
	public String toString() {
		return claimId + "#" + name;
	}
}
