package io.cord3c.ssi.api.did;

import io.cord3c.ssi.api.internal.parser.Displayer;
import io.cord3c.ssi.api.internal.parser.Parser;
import io.cord3c.ssi.api.internal.parser.ParserException;
import io.cord3c.ssi.api.internal.parser.Rule;
import io.cord3c.ssi.api.internal.parser.Rule_did;
import io.cord3c.ssi.api.internal.parser.Rule_did_fragment;
import io.cord3c.ssi.api.internal.parser.Rule_did_path;
import io.cord3c.ssi.api.internal.parser.Rule_did_query;
import io.cord3c.ssi.api.internal.parser.Rule_did_reference;
import io.cord3c.ssi.api.internal.parser.Rule_method;
import io.cord3c.ssi.api.internal.parser.Rule_service;
import io.cord3c.ssi.api.internal.parser.Rule_specific_idstring;
import io.cord3c.ssi.api.internal.parser.Terminal_NumericValue;
import io.cord3c.ssi.api.internal.parser.Terminal_StringValue;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;


// TODO remo contribute
@JsonSerialize(using = ToStringSerializer.class)
public class DID implements Comparable<DID> {

	public static final String URI_SCHEME = "did";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private String didReference;
	private transient String did;
	private transient String method;
	private transient String specificId;
	private transient String service;
	private transient String path;
	private transient String query;
	private transient String fragment;

	private DID() {

	}

	private DID(String didReference) throws IllegalArgumentException, ParserException {

		this.didReference = didReference;

		this.parse();
	}

	private void parse() throws IllegalArgumentException, ParserException {

		Rule_did_reference rule = (Rule_did_reference) Parser.parse("did-reference", this.didReference);
		rule.accept(new DIDVisitor());
	}

	/*
	 * Factory methods
	 */

	public static DID fromString(String string) {

		return new DID(string);
	}

	public static DID fromUri(URI uri) throws IllegalArgumentException, ParserException {

		return fromString(uri.toString());
	}

	/*
	 * Serialization
	 */

	public static DID fromJson(String json) throws JsonParseException, JsonMappingException, IOException {

		return objectMapper.readValue(json, DID.class);
	}

	public String toJson() throws JsonProcessingException {

		return objectMapper.writeValueAsString(this);
	}

	@Override
	public int compareTo(DID o) {
		return toString().compareTo(o.toString());
	}

	/*
	 * Helper classes
	 */

	private class DIDVisitor extends Displayer {

		public Object visit(Rule_did rule) {

			DID.this.did = rule.spelling;
			return visitRules(rule.rules);
		}

		public Object visit(Rule_method rule) {

			DID.this.method = rule.spelling;
			return visitRules(rule.rules);
		}

		public Object visit(Rule_specific_idstring rule) {

			DID.this.specificId = rule.spelling;
			return visitRules(rule.rules);
		}

		public Object visit(Rule_service rule) {

			DID.this.service = rule.spelling;
			return visitRules(rule.rules);
		}

		public Object visit(Rule_did_path rule) {

			DID.this.path = rule.spelling;
			return visitRules(rule.rules);
		}

		public Object visit(Rule_did_query rule) {

			DID.this.query = rule.spelling;
			return visitRules(rule.rules);
		}

		public Object visit(Rule_did_fragment rule) {

			DID.this.fragment = rule.spelling;
			return visitRules(rule.rules);
		}

		@Override
		public Object visit(Terminal_StringValue value) {

			return null;
		}

		@Override
		public Object visit(Terminal_NumericValue value) {
			return null;
		}

		private Object visitRules(ArrayList<Rule> rules) {

			for (Rule rule : rules) rule.accept(this);
			return null;
		}
	}

	/*
	 * Getters
	 */

	@JsonGetter
	public final String getDidReference() {

		return this.didReference;
	}

	@JsonSetter
	public final void setDidReference(String didReference) {

		this.didReference = didReference;
	}

	@JsonGetter
	public final String getDid() {

		return this.did;
	}

	@JsonSetter
	public final void setDid(String did) {

		this.did = did;
	}

	@JsonGetter
	public final String getMethod() {

		return this.method;
	}

	@JsonSetter
	public final void setMethod(String method) {

		this.method = method;
	}

	@JsonGetter
	public final String getSpecificId() {

		return this.specificId;
	}

	@JsonSetter
	public final void setSpecificId(String specificId) {

		this.specificId = specificId;
	}

	@JsonGetter
	public final String getService() {

		return this.service;
	}

	@JsonSetter
	public final void setService(String service) {

		this.service = service;
	}

	@JsonGetter
	public final String getPath() {

		return this.path;
	}

	@JsonSetter
	public final void setPath(String path) {

		this.path = path;
	}

	@JsonGetter
	public final String getQuery() {

		return this.query;
	}

	@JsonSetter
	public final void setQuery(String query) {

		this.query = query;
	}

	@JsonGetter
	public final String getFragment() {

		return this.fragment;
	}

	@JsonSetter
	public final void setFragment(String fragment) {

		this.fragment = fragment;
	}

	/*
	 * Object methods
	 */

	@Override
	public int hashCode() {

		return this.didReference.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DID && (this.didReference.equals(((DID) obj).didReference));
	}

	@Override
	public String toString() {

		return this.didReference.toString();
	}
}
