package io.cord3c.rest.server;

import io.crnk.core.boot.CrnkBoot;

public interface RestConfigurer {

	void configure(RestContext context, CrnkBoot boot);

}
