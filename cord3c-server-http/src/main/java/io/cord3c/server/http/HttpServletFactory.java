package io.cord3c.server.http;

import javax.servlet.http.HttpServlet;

import net.corda.core.node.AppServiceHub;

public interface HttpServletFactory {

	String getPattern();

	HttpServlet getImplementation(AppServiceHub serviceHub);

}
