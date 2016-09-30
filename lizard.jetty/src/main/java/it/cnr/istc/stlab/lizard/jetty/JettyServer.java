package it.cnr.istc.stlab.lizard.jetty;

import it.cnr.istc.stlab.lizard.jetty.resources.Lizard;
import it.cnr.istc.stlab.lizard.jetty.utils.FileUtils;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


public class JettyServer {

	public static void main(String[] args) {

		Server jettyServer = new Server(8080);

		ServletContextHandler servletContextHandler = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		servletContextHandler.setContextPath("/");
		jettyServer.setHandler(servletContextHandler);

		ServletHolder servletHolder = servletContextHandler.addServlet(
				org.glassfish.jersey.servlet.ServletContainer.class, "/api/*");
		servletHolder.setInitOrder(1);

		// Tells the Jersey Servlet which REST service/class to load.
		servletHolder.setInitParameter(
				"jersey.config.server.provider.packages",
				"io.swagger.jaxrs.listing,"
						+ FileUtils.getNamePackage(Lizard.class));

		ServletHolder servletHolderBootrstrap = servletContextHandler
				.addServlet(
						org.glassfish.jersey.servlet.ServletContainer.class, "");
		servletHolderBootrstrap.setInitParameter(
				"jersey.config.server.provider.classnames",
				Bootstrap.class.getCanonicalName());
		servletHolderBootrstrap.setInitOrder(2);

		try {
			jettyServer.start();
			new Bootstrap().init(servletHolderBootrstrap.getServlet()
					.getServletConfig());
			jettyServer.join();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jettyServer.destroy();
		}
	}
}