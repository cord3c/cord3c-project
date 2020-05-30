package io.cord3c.server.rest.internal;

import com.google.auto.service.AutoService;
import io.cord3c.server.rest.CordaRestModule;
import io.cord3c.server.rest.ServletFactory;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.servlet.CrnkServlet;
import net.corda.core.node.AppServiceHub;

import javax.servlet.http.HttpServlet;


@AutoService(ServletFactory.class)
public class RestServletFactory implements ServletFactory {

	private static AppServiceHub serviceHub;

	@Override
	public String getPattern() {
		return "/api";
	}

	@Override
	public Class<? extends HttpServlet> getImplementation(AppServiceHub serviceHub) {
		RestServletFactory.serviceHub = serviceHub;

		return CordaCrnkServlet.class;
	}

	public static class CordaCrnkServlet extends CrnkServlet {

		@Override
		protected void initCrnk(CrnkBoot boot) {
			boot.addModule(new CordaRestModule(serviceHub));
		}
	}
}
