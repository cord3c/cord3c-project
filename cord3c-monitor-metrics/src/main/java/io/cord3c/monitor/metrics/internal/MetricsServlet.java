package io.cord3c.monitor.metrics.internal;

import io.micrometer.core.lang.Nullable;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;

@Slf4j
public class MetricsServlet extends HttpServlet {

	@Getter
	private static PrometheusMeterRegistry registry = new PrometheusMeterRegistry(new PrometheusConfig() {
		@Override
		public Duration step() {
			return Duration.ofSeconds(10);
		}

		@Override
		@Nullable
		public String get(String k) {
			return null;
		}
	});

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String response = registry.scrape();
		resp.setStatus(200);

		OutputStream os = resp.getOutputStream();
		os.write(response.getBytes());
		os.close();
	}
}
