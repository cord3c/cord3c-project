package io.cord3c.ssi.vault;

import java.util.List;

import io.cord3c.ssi.api.vc.VerifiableCredential;
import io.crnk.core.queryspec.QuerySpec;
import kotlin.jvm.functions.Function1;

public interface VCQueryEngine extends Function1<QuerySpec, List<VerifiableCredential>> {

}
