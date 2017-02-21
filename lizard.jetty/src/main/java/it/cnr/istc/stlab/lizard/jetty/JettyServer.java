package it.cnr.istc.stlab.lizard.jetty;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import it.cnr.istc.stlab.lizard.commons.inmemory.RestInterface;
import it.cnr.istc.stlab.lizard.commons.jersey.SetBodyWriter;
import it.cnr.istc.stlab.lizard.jetty.resources.Lizard;
import it.cnr.istc.stlab.lizard.jetty.utils.FileUtils;

public class JettyServer {

	public static void main(String[] args) {

		Logger log = Logger.getLogger(JettyServer.class);

		log.info("Starting Jetty.");

		int port;
		if (args.length > 0) {
			String portString = args[0];

			if (portString == null) {
				port = 8080;
			} else {
				try {
					port = Integer.valueOf(portString);
				} catch (NumberFormatException e) {
					port = 8080;
				}
			}
		} else {
			port = 8080;
		}

		// Jetty server
		Server jettyServer = new Server(port);

		// Main context handler
		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletContextHandler.setContextPath("/");

		// Lizard servlets
		ServletHolder servletHolder = servletContextHandler.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/lizard/*");
		System.out.println("Lizard service will be available at " + "localhost:" + port + "/lizard/*");
		servletHolder.setInitOrder(1);
		servletHolder.setInitParameter("jersey.config.server.provider.packages", "io.swagger.jaxrs.listing," + FileUtils.getNamePackage(Lizard.class));
		servletHolder.setInitParameter("jersey.config.server.wadl.disableWadl", "true");
		servletHolder.setInitParameter("swagger.scanner.id", Lizard.class.getName());
		servletHolder.setInitParameter("swagger.context.id", Lizard.class.getName());
		servletHolder.setInitParameter("swagger.config.id", Lizard.class.getName());
		// servletHolder.setInitParameter("swagger.use.path.based.config","true");

		ServletHolder lizardRestHolder = servletContextHandler.addServlet(Bootstrap.class, "/" + Lizard.class.getName());
		lizardRestHolder.setInitOrder(2);
		lizardRestHolder.setInitParameter("swagger.scanner.id", Lizard.class.getName());
		lizardRestHolder.setInitParameter("swagger.context.id", Lizard.class.getName());
		lizardRestHolder.setInitParameter("swagger.config.id", Lizard.class.getName());
		// lizardRestHolder.setInitParameter("swagger.use.path.based.config","true");
		lizardRestHolder.setInitParameter(Bootstrap.TITLE, "Lizard");
		lizardRestHolder.setInitParameter(Bootstrap.PACKAGE, FileUtils.getNamePackage(Lizard.class));
		lizardRestHolder.setInitParameter(Bootstrap.BASE_PATH, "/lizard");
		lizardRestHolder.setInitParameter(Bootstrap.DESCRIPTION, "Lizard automatically generates Rest APIs for managing ontologies");
		lizardRestHolder.setInitParameter(Bootstrap.HOST, "localhost:" + port);
		lizardRestHolder.setInitParameter(Bootstrap.VERSION, "0.1");

		// Other servlets
		ServiceLoader<RestInterface> restInterfaceLoader = ServiceLoader.load(RestInterface.class);
		Set<String> aaPackages = new HashSet<String>();
		final int serverPort = port;
		restInterfaceLoader.forEach(restInterface -> {
			String packageJavaName = FileUtils.getNamePackage(restInterface.getClass());
			if (!aaPackages.contains(packageJavaName)) {

				String basePathResources = packageJavaName.replace(".web", "");
				basePathResources = basePathResources.replaceAll("\\.", "_");

				ServletHolder servletHolderRestOntology = servletContextHandler.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/" + basePathResources + "/*");
				System.out.println("Package " + packageJavaName + " - Service will be available at " + "http://localhost:" + serverPort + "/" + basePathResources + "/*");
				servletHolderRestOntology.setInitOrder(1);
				servletHolderRestOntology.setInitParameter("jersey.config.server.provider.packages", "io.swagger.jaxrs.listing," + FileUtils.getNamePackage(SetBodyWriter.class) + "," + packageJavaName);
				servletHolderRestOntology.setInitParameter("jersey.config.server.wadl.disableWadl", "true");
				servletHolderRestOntology.setInitParameter("swagger.scanner.id", packageJavaName);
				servletHolderRestOntology.setInitParameter("swagger.context.id", packageJavaName);
				servletHolderRestOntology.setInitParameter("swagger.config.id", packageJavaName);

				ServletHolder ontologySwaggerHolder = servletContextHandler.addServlet(Bootstrap.class, "/swagger/" + basePathResources);
				ontologySwaggerHolder.setInitOrder(2);
				ontologySwaggerHolder.setInitParameter("swagger.scanner.id", packageJavaName);
				ontologySwaggerHolder.setInitParameter("swagger.context.id", packageJavaName);
				ontologySwaggerHolder.setInitParameter("swagger.config.id", packageJavaName);
				// lizardRestHolder.setInitParameter("swagger.use.path.based.config","true");
				ontologySwaggerHolder.setInitParameter(Bootstrap.TITLE, packageJavaName);
				ontologySwaggerHolder.setInitParameter(Bootstrap.PACKAGE, packageJavaName);
				ontologySwaggerHolder.setInitParameter(Bootstrap.BASE_PATH, "/" + basePathResources);
				ontologySwaggerHolder.setInitParameter(Bootstrap.DESCRIPTION, "Rest services defined in package " + packageJavaName + " for accessing the corresponding ontology.");
				ontologySwaggerHolder.setInitParameter(Bootstrap.HOST, "localhost:" + serverPort);
				ontologySwaggerHolder.setInitParameter(Bootstrap.VERSION, "0.99"); // TODO
				aaPackages.add(packageJavaName);

			}
		});

		// add main context to Jetty
		jettyServer.setHandler(servletContextHandler);

		try {
			jettyServer.start();
			System.out.println("Done! Jetty Server is up and running!");
			jettyServer.join();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jettyServer.destroy();
		}
	}
}