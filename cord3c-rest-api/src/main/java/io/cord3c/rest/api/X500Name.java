package io.cord3c.rest.api;

import com.google.common.base.Verify;
import lombok.Data;

@Data
public class X500Name {

	private String commonName;

	private String organisationUnit;

	private String organisation;

	private String locality;

	private String state;

	private String country;

	public String toKey() {
		StringBuilder builder = new StringBuilder();
		appendElement(builder, getCommonName());
		appendElement(builder, getOrganisation());
		appendElement(builder, getOrganisationUnit());
		appendElement(builder, getLocality());
		appendElement(builder, getState());
		appendElement(builder, getCountry());
		return builder.toString();
	}

	private void appendElement(StringBuilder builder, String value) {
		if (value != null) {
			if (builder.length() > 0) {
				builder.append("_");
			}
			builder.append(value.toLowerCase().replace(" ", ""));
		}
	}

	@Override
	public String toString() {
		Verify.verify(organisationUnit == null, "no yet implemented: %s", organisationUnit);
		Verify.verify(state == null, "no yet implemented: %s", state);
		Verify.verify(commonName == null, "no yet implemented: %s", commonName);
		return "O=" + organisation + ", L=" + locality + ", C=" + country;

		/*
		n build(principal: X500Principal): CordaX500Name {
            val attrsMap = principal.toAttributesMap(supportedAttributes)
            val CN = attrsMap[BCStyle.CN]
            val OU = attrsMap[BCStyle.OU]
            val O = requireNotNull(attrsMap[BCStyle.O]) { "Corda X.500 names must include an O attribute" }
            val L = requireNotNull(attrsMap[BCStyle.L]) { "Corda X.500 names must include an L attribute" }
            val ST = attrsMap[BCStyle.ST]
            val C = requireNotNull(attrsMap[BCStyle.C]) { "Corda X.500 names must include an C attribute" }
            return CordaX500Name(CN, OU, O, L, ST, C)
		 */
	}
}