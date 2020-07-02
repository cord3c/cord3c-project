package io.cord3c.server.http;

import com.google.auto.service.AutoService;
import net.corda.core.node.AppServiceHub;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@AutoService(HttpServletFactory.class)
public class HelloWorldServletFactory implements HttpServletFactory {
	@Override
	public String getPattern() {
		return "/hello";
	}

	@Override
	public HelloWorldServlet getImplementation(AppServiceHub serviceHub) {
		return new HelloWorldServlet();
	}

	public static class HelloWorldServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			PrintWriter writer = resp.getWriter();
			writer.write("World");

			resp.setStatus(200);
		}
	}
}
