package it.cnr.istc.stlab.lizard.jetty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

import org.babelnet.model.babelnet.web.Sense;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.w3._2001.XMLSchema.string;
import org.w3.ns.lemon.ontolex.web.LexicalEntry;

import it.cnr.istc.stlab.lizard.commons.inmemory.RestInterface;
import it.cnr.istc.stlab.lizard.jetty.utils.FileUtils;



public class JettyServer {

	public static void main(String[] args) {

		Server jettyServer = new Server(8080);

		ServletContextHandler servletContextHandler = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		servletContextHandler.setContextPath("/");
		jettyServer.setHandler(servletContextHandler);

		ServletHolder servletHolder = servletContextHandler.addServlet(
				org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		servletHolder.setInitOrder(1);

		// Tells the Jersey Servlet which REST service/class to load.
		
		ServiceLoader<RestInterface> restInterfaceLoader = ServiceLoader.load(RestInterface.class);
		
		Collection<String> packages = new ArrayList<String>();
		restInterfaceLoader.forEach(restInterface -> {
			String packageName = FileUtils.getNamePackage(restInterface.getClass());
			if(!packages.contains(packageName) 
					&& !packageName.equals("org.w3._2001.XMLSchema.web")) packages.add(packageName);
			System.out.println(restInterface.getClass());
		});
		
		StringBuilder sb = new StringBuilder();
		packages.forEach(pkg -> {
			if(sb.length() > 0) sb.append(",");
			sb.append(pkg);
				
		});
		
		System.out.println(sb.toString());
		/*
		servletHolder.setInitParameter(
				"jersey.config.server.provider.packages",
				"io.swagger.jaxrs.listing,"
						+ FileUtils.getNamePackage(Sense.class)
						+ "," + FileUtils.getNamePackage(string.class));
		*/
		servletHolder.setInitParameter(
				"jersey.config.server.provider.packages",
				"io.swagger.jaxrs.listing,"
						+ sb.toString());
		
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