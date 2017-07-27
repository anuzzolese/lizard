package it.cnr.istc.stlab.lizard.jetty;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Swagger;

public class Bootstrap extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String TITLE = "title";
	public static final String VERSION = "version";
	public static final String BASE_PATH = "basepath";
	public static final String HOST = "host";
	public static final String DESCRIPTION = "description";
	public static final String PACKAGE = "package";
	public static final String PATH = "path";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		BeanConfig beanConfig = new BeanConfig();

		// info
		beanConfig.setTitle(config.getInitParameter(TITLE));
		beanConfig.setDescription(config.getInitParameter(DESCRIPTION));
		beanConfig.setSchemes(new String[] { "http" });
		beanConfig.setHost(config.getInitParameter(HOST));
		// beanConfig.setUsePathBasedConfig(true);
		beanConfig.setBasePath(config.getInitParameter(BASE_PATH));
		beanConfig.setContact("stlab@cnr.it");
		beanConfig.setLicense("Apache 2.0");
		beanConfig.setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html");
		beanConfig.setPrettyPrint(true);
		beanConfig.setConfigId(config.getInitParameter("swagger.config.id"));
		beanConfig.setScannerId(config.getInitParameter("swagger.scanner.id"));
		beanConfig.setContextId(config.getInitParameter("swagger.context.id"));
		beanConfig.setVersion(config.getInitParameter(VERSION));

		// Resource package
		System.out.println("BEAN CONFIG SCAN FOR " + config.getInitParameter(PACKAGE));
		beanConfig.setResourcePackage(config.getInitParameter(PACKAGE));
		// beanConfig.setScan();
		beanConfig.setScan(true);

		Swagger swagger = new Swagger();
		new SwaggerContextService().withSwaggerConfig(beanConfig).updateSwagger(swagger);

	}

}