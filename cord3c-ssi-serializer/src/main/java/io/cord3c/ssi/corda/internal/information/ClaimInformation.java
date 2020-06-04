package io.cord3c.ssi.corda.internal.information;

import lombok.Data;

@Data
public class ClaimInformation {

	private String name;

	private String jsonName;

	private ValueAccessor accessor;

}
