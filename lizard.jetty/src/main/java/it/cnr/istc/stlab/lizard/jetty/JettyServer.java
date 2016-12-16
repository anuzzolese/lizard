package it.cnr.istc.stlab.lizard.jetty;

import it.cnr.istc.stlab.lizard.commons.inmemory.RestInterface;
import it.cnr.istc.stlab.lizard.commons.jersey.SetBodyWriter;
import it.cnr.istc.stlab.lizard.jetty.resources.Lizard;
import it.cnr.istc.stlab.lizard.jetty.utils.FileUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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

		Server jettyServer = new Server(port);

		ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletContextHandler.setContextPath("/");
		jettyServer.setHandler(servletContextHandler);

		// Lizard servlet
		ServletHolder servletHolder = servletContextHandler.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/lizard/*");
		servletHolder.setInitOrder(1);
		servletHolder.setInitParameter("jersey.config.server.provider.packages", "io.swagger.jaxrs.listing," + FileUtils.getNamePackage(SetBodyWriter.class) + "," + FileUtils.getNamePackage(Lizard.class));

		try {
			jettyServer.start();

			getLizardBootstrap().init(servletHolder.getServlet().getServletConfig());

			// Ontology servlet
			for (Bootstrap b : getBootstraps("localhost:" + port)) {
				System.out.println("Package " + b.get_package() + " on localhost:" + port + "/" + b.getPath() + "/");
				ServletHolder servletHolderRestOntology = servletContextHandler.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/" + b.getPath() + "/*");
				servletHolderRestOntology.setInitOrder(2);
				servletHolderRestOntology.setInitParameter("jersey.config.server.provider.packages", "io.swagger.jaxrs.listing," + FileUtils.getNamePackage(SetBodyWriter.class) + "," + b.get_package());
				b.init(servletHolderRestOntology.getServlet().getServletConfig());
			}

			jettyServer.join();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jettyServer.destroy();
		}
	}

	static Bootstrap getLizardBootstrap() {
		Bootstrap b = new Bootstrap();
		b.setTitle("Lizard");
		b.set_package(FileUtils.getNamePackage(Lizard.class));
		b.setBasePath("/lizard/");
		b.setDescription("Lizard automatically generates API Rest for managing ontologies");
		b.setHost("localhost:8080");
		return b;
	}

	static Collection<Bootstrap> getBootstraps(String host) {
		ServiceLoader<RestInterface> restInterfaceLoader = ServiceLoader.load(RestInterface.class);
		Collection<Bootstrap> result = new HashSet<Bootstrap>();
		Collection<String> aaPackages = new HashSet<String>();
		restInterfaceLoader.forEach(restInterface -> {
			String packageJavaName = FileUtils.getNamePackage(restInterface.getClass());
			if (!aaPackages.contains(packageJavaName)) {
				Bootstrap b = new Bootstrap();
				String basePathResources = packageJavaName.replace(".web", "");
				basePathResources = basePathResources.replaceAll("\\.", "_");
				b.setTitle(packageJavaName);
				b.set_package(packageJavaName);
				b.setBasePath("/" + basePathResources + "/");
				b.setPath(basePathResources);
				b.setDescription("Rest services defined in package " + packageJavaName + " for accessing the corresponding ontology.");
				b.setHost(host);
				b.setVersion("0.99"); // TODO
				aaPackages.add(packageJavaName);
				result.add(b);
			}
		});
		return result;
	}
}