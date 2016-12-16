package it.cnr.istc.stlab.lizard.jetty;

import io.swagger.jaxrs.config.BeanConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class Bootstrap extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String title, version, basepath, host, description, _package, path;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		BeanConfig beanConfig = new BeanConfig();

		// info
		beanConfig.setTitle(this.title);
		beanConfig.setDescription(this.description);
		beanConfig.setSchemes(new String[] { "http" });
		beanConfig.setHost(this.host);
		beanConfig.setBasePath(this.basepath);
		beanConfig.setVersion(this.version);
		beanConfig.setContact("stlab@cnr.it");
		beanConfig.setLicense("Apache 2.0");
		beanConfig.setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html");
		beanConfig.setPrettyPrint(true);

		// Resource package
		beanConfig.setResourcePackage(this._package);
		beanConfig.setScan(true);
		
		
		

	}

	public String getBasePath() {
		return basepath;
	}

	public void setBasePath(String basepath) {
		this.basepath = basepath;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String get_package() {
		return _package;
	}

	public void set_package(String _package) {
		this._package = _package;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}