package io.cord3c.rest.client;

import lombok.Data;

@Data
public class X500Name {

	private String commonName;

	private String organisationUnit;

	private String organisation;

	private String locality;

	private String state;

	private String country;
}