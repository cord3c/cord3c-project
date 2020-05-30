package io.cord3c.server.rest;

import net.corda.core.node.AppServiceHub;

import javax.servlet.http.HttpServlet;

public interface ServletFactory {

	String getPattern();

	Class<? extends HttpServlet> getImplementation(AppServiceHub serviceHub);

}
