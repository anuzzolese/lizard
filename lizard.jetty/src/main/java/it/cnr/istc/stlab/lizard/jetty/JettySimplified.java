package it.cnr.istc.stlab.lizard.jetty;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import it.cnr.istc.stlab.lizard.commons.web.RestImpl;
import it.cnr.istc.stlab.lizard.jetty.utils.FileUtils;

public class JettySimplified {

	public static void main(String[] args) {

		Logger log = Logger.getLogger(JettyServer.class);

		log.info("Starting Jetty.");
		

		int port;
		if (args.length > 0) {
			String portString = args[0];

			if (portString == null) {
				port = 8585;
			} else {
				try {
					port = Integer.valueOf(portString);
				} catch (NumberFormatException e) {
					port = 8585;
				}
			}
		} else {
			port = 8585;
		}

		// Jetty server
		Server jettyServer = new Server(port);

		// Main context handler
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletContextHandler.setContextPath("/");

		// Lizard servlets
		ServletHolder servletHolder = servletContextHandler.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		System.out.println("Lizard service will be available at " + "http://localhost:" + port + "/*");
		servletHolder.setInitOrder(1);
		servletHolder.setInitParameter("jersey.config.server.provider.packages", FileUtils.getNamePackage(RestImpl.class));
		servletHolder.setInitParameter("jersey.config.server.wadl.disableWadl", "true");
		servletHolder.setInitParameter("jersey.config.server.provider.classnames", "org.glassfish.jersey.jackson.JacksonFeature");

		// add main context to Jetty
		jettyServer.setHandler(servletContextHandler);

		try {
			jettyServer.start();
			System.out.println("Done! Jetty Server is up and running!");
			jettyServer.join();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				jettyServer.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
