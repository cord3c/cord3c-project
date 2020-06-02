package io.cord3c.monitor.metrics.internal;

import com.google.auto.service.AutoService;
import io.cord3c.server.http.HttpServletFactory;
import net.corda.core.node.AppServiceHub;

import javax.servlet.http.HttpServlet;


@AutoService(HttpServletFactory.class)
public class MetricServletFactory implements HttpServletFactory {

	private static AppServiceHub serviceHub;

	@Override
	public String getPattern() {
		return "/prometheus";
	}

	@Override
	public Class<? extends HttpServlet> getImplementation(AppServiceHub serviceHub) {
		return MetricsServlet.class;
	}
}
