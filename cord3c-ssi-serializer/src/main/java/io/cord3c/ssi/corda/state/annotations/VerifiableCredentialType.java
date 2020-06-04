package io.cord3c.ssi.corda.state.annotations;

import io.cord3c.ssi.api.vc.VerifiableCredential;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For classes that act as {@link VerifiableCredential}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VerifiableCredentialType {


	String type() default "";
}
