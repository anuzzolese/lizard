package it.cnr.istc.stlab.lizard.jetty;

import io.swagger.jaxrs.config.BeanConfig;
import it.cnr.istc.stlab.lizard.jetty.resources.Lizard;
import it.cnr.istc.stlab.lizard.jetty.utils.FileUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class Bootstrap extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		BeanConfig beanConfig = new BeanConfig();

		// info
		beanConfig.setTitle("Lizard");
		beanConfig
				.setDescription("Lizard automatically generates API Rest for managing ontologies");
		beanConfig.setSchemes(new String[] { "http" });
		beanConfig.setHost("localhost:8080"); // TODO
		beanConfig.setBasePath("/");
		beanConfig.setVersion("0.99");
		beanConfig.setContact("stlab@cnr.it");
		beanConfig.setLicense("Apache 2.0");
		beanConfig
				.setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html");
		beanConfig.setPrettyPrint(true);

		// Resource package
		beanConfig.setResourcePackage(FileUtils.getNamePackage(Lizard.class)
				+ "," + JettyServer.getRestinterfaces());
		beanConfig.setScan(true);

	}
}