package io.cord3c.server.http;

import javax.servlet.Filter;

import net.corda.core.node.AppServiceHub;

public interface HttpFilterFactory {

	Filter getImplementation(AppServiceHub serviceHub);

	String getPathSpec();
}
