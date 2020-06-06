package io.cord3c.ssi.corda.internal.information;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class VerifiableCredentialInformation {

	private Class<?> implementationType;

	private List<String> types = new ArrayList<>();

	private Map<String, ClaimInformation> claims = new LinkedHashMap<>();

	private ValueAccessor<Instant> timestampAccessor;

	private ValueAccessor<String> issuerAccessor;

	private ValueAccessor<String> subjectAccessor;

	private List<String> contexts = new ArrayList<>();

	private ValueAccessor<String> idAccessor;

}
