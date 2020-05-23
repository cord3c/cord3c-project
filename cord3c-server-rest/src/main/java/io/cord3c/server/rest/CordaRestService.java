package io.cord3c.server.rest;

import com.google.auto.service.AutoService;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.servlet.CrnkServlet;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.SingletonSerializeAsToken;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

@CordaService
@AutoService(SingletonSerializeAsToken.class)
public class CordaRestService extends SingletonSerializeAsToken {

	private final Server server = new Server();

	private static AppServiceHub serviceHub;

	public CordaRestService(AppServiceHub appServiceHub) {
		serviceHub = appServiceHub;
		ServletHandler servletHandler = new ServletHandler();
		ServletHolder servletHolder = servletHandler.addServletWithMapping(CordaCrnkServlet.class, "/api");

		ServerConnector connector = new ServerConnector(server);
		connector.setPort(8090);

		server.setHandler(servletHandler);
		server.setConnectors(new Connector[]{connector});
	}

	public static class CordaCrnkServlet extends CrnkServlet {

		@Override
		protected void initCrnk(CrnkBoot boot) {
			boot.addModule(new CordaRestModule(serviceHub));
		}
	}
}
