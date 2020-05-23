package io.crnk.example.service.model;

import io.crnk.core.queryspec.AbstractPathSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.internal.typed.PrimitivePathSpec;
import io.crnk.core.queryspec.internal.typed.ResourcePathSpec;
import java.lang.String;
import java.util.Map;
import javax.annotation.Generated;

@Generated("Generated by Crnk annotation processor")
public class PersonPathSpec extends ResourcePathSpec {
 public static PersonPathSpec personPathSpec = new PersonPathSpec();

 public PersonPathSpec() {
  super(PathSpec.empty());
 }

 public PersonPathSpec(PathSpec pathSpec) {
  super(pathSpec);
 }

 protected PersonPathSpec(AbstractPathSpec spec) {
  super(spec);
 }

 public PrimitivePathSpec<Map<String, String>> properties() {
  PathSpec updatedPath = append("properties");
  return boundSpec != null ? new io.crnk.core.queryspec.internal.typed.PrimitivePathSpec<java.util.Map<java.lang.String, java.lang.String>>(boundSpec) : new io.crnk.core.queryspec.internal.typed.PrimitivePathSpec<java.util.Map<java.lang.String, java.lang.String>>(updatedPath);
 }

 protected PersonPathSpec bindSpec(AbstractPathSpec spec) {
  return new PersonPathSpec(spec);
 }
}