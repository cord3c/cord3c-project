package io.cord3c.server.http;

import net.corda.core.node.AppServiceHub;

import javax.servlet.http.HttpServlet;

public interface HttpServletFactory {

	String getPattern();

	Class<? extends HttpServlet> getImplementation(AppServiceHub serviceHub);

}
