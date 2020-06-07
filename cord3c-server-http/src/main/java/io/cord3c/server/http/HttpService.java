package io.cord3c.server.http;

import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.SingletonSerializeAsToken;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

import java.util.Iterator;
import java.util.ServiceLoader;

@CordaService
@Slf4j
@AutoService(SingletonSerializeAsToken.class)
public class HttpService extends SingletonSerializeAsToken {

	private static Server server = new Server();

	private static AppServiceHub serviceHub;

	@Getter
	private int port = 8090;

	@SneakyThrows
	public HttpService(AppServiceHub appServiceHub) {
		if (serviceHub == null) {
			serviceHub = appServiceHub;
			ServletHandler servletHandler = new ServletHandler();

			ServiceLoader<HttpServletFactory> loader = ServiceLoader.load(HttpServletFactory.class);
			Iterator<HttpServletFactory> iterator = loader.iterator();
			while (iterator.hasNext()) {
				HttpServletFactory servletFactory = iterator.next();
				servletHandler.addServletWithMapping(servletFactory.getImplementation(appServiceHub), servletFactory.getPattern());
			}

			ServerConnector connector = new ServerConnector(server);
			connector.setPort(port);

			server.setHandler(servletHandler);
			server.setConnectors(new Connector[]{connector});
			server.start();
			log.info("HTTP server running on http://127.0.0.1:" + port);
		} else {
			log.warn("HttpService instantiated multiple times");
		}
	}

	@SneakyThrows
	public void destroy() {
		server.stop();
		server.destroy();
	}
}
