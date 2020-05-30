package io.cord3c.ssi.api.vc;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SignatureVerificationException extends SecurityException {

	public SignatureVerificationException(String message) {
		super(message);
	}

	public SignatureVerificationException(Throwable cause) {
		super(cause);
	}

	public SignatureVerificationException(String message, Throwable cause) {
		super(message, cause);
	}

}
