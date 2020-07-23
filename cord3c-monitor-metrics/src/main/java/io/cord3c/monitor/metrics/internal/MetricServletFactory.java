package io.cord3c.monitor.metrics.internal;

import javax.servlet.http.HttpServlet;

import com.google.auto.service.AutoService;
import io.cord3c.server.http.HttpServletFactory;
import net.corda.core.node.AppServiceHub;


@AutoService(HttpServletFactory.class)
public class MetricServletFactory implements HttpServletFactory {

	private static AppServiceHub serviceHub;

	@Override
	public String getPattern() {
		return "/prometheus";
	}

	@Override
	public HttpServlet getImplementation(AppServiceHub serviceHub) {
		return new MetricsServlet();
	}
}
