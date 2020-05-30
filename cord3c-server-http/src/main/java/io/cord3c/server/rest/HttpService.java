package io.cord3c.server.rest;

import com.google.auto.service.AutoService;
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
@AutoService(SingletonSerializeAsToken.class)
public class HttpService extends SingletonSerializeAsToken {

	private final Server server = new Server();

	private static AppServiceHub serviceHub;

	public HttpService(AppServiceHub appServiceHub) {
		serviceHub = appServiceHub;
		ServletHandler servletHandler = new ServletHandler();

		ServiceLoader<ServletFactory> loader = ServiceLoader.load(ServletFactory.class);
		Iterator<ServletFactory> iterator = loader.iterator();
		while (iterator.hasNext()) {
			ServletFactory servletFactory = iterator.next();
			servletHandler.addServletWithMapping(servletFactory.getImplementation(appServiceHub), servletFactory.getPattern());
		}

		ServerConnector connector = new ServerConnector(server);
		connector.setPort(8090);

		server.setHandler(servletHandler);
		server.setConnectors(new Connector[]{connector});
	}

}
