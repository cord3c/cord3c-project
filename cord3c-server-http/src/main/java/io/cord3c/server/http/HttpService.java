package io.cord3c.server.http;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

import com.google.auto.service.AutoService;
import io.cord3c.server.http.internal.PropertyUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.SingletonSerializeAsToken;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

@CordaService
@Slf4j
@AutoService(SingletonSerializeAsToken.class)
public class HttpService extends SingletonSerializeAsToken {

	public static int DEFAULT_PORT = 8080;

	private Server server = new Server();

	@Getter
	private int port;

	@SneakyThrows
	public HttpService(AppServiceHub appServiceHub) {
		ServletHandler servletHandler = new ServletHandler();

		Iterator<HttpFilterFactory> filterFactories = ServiceLoader.load(HttpFilterFactory.class).iterator();
		while (filterFactories.hasNext()) {
			HttpFilterFactory filterFactory = filterFactories.next();
			Filter filter = filterFactory.getImplementation(appServiceHub);
			servletHandler.addFilterWithMapping(new FilterHolder(filter), filterFactory.getPathSpec(),
					EnumSet.allOf(DispatcherType.class));
		}

		Iterator<HttpServletFactory> servletFactories = ServiceLoader.load(HttpServletFactory.class).iterator();

		while (servletFactories.hasNext()) {
			HttpServletFactory servletFactory = servletFactories.next();
			HttpServlet servlet = servletFactory.getImplementation(appServiceHub);
			servletHandler.addServletWithMapping(new ServletHolder(servlet), servletFactory.getPattern());
		}

		ServerConnector connector = new ServerConnector(server);
		if (!existsClass("org.junit.jupiter.api.Test")) {
			connector.setPort(Integer.parseInt(PropertyUtils.getProperty("cord3c.http.port", Integer.toString(DEFAULT_PORT))));
		}

		server.setHandler(servletHandler);
		server.setConnectors(new Connector[]{connector});
		server.start();

		port = connector.getLocalPort();
		log.info("HTTP server running on http://127.0.0.1:" + port);

	}

	private boolean existsClass(String name) {
		try {
			Class.forName(name);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	@SneakyThrows
	public void destroy() {
		server.stop();
		server.destroy();
	}
}
