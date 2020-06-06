package io.cord3c.ssi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When creating a class that implements {@link VerifiableCredentialType}, you will need to have a field that serves as the claim's issuer.
 * This field will be annotated with {@link IssuanceTimestamp}.
 * <p>
 * There must be one and only one such field per class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IssuanceTimestamp {

}
