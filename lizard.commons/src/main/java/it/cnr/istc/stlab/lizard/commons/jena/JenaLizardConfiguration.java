package it.cnr.istc.stlab.lizard.commons.jena;

import java.util.Properties;

public class JenaLizardConfiguration {

	public static final String TYPE = "type";
	public static final String MODEL_FILEPATH = "modelFilepath";
	public static final String MODEL_LANG = "lang";
	public static final String TDB_LOCATION = "location";
	public static final String VIRTUOSO_USER = "user";
	public static final String VIRTUOSO_PASSWORD = "password";
	public static final String VIRTUOSO_HOST = "host";
	public static final String VIRTUOSO_PORT = "port";
	public static final String GRAPH = "graph";
	public static final String INFERENCE_ON_DATA = "inference_on_data";
	public static final String ONTOLOGIES_FILE = "ontologies_file";
	public static final String SWAGGER = "swagger";
	public static final String JENA_DOC_MANAGER = "jan_doc_manager";

	private String tdbLocation;
	private String modelFilePath;
	private RepositoryType type;
	private String virtuosoUser;
	private String virtuosoPassword;
	private String virtuosoHost;
	private String virtuosoPort;
	private String graph;
	private String lang;
	private boolean inference;
	private boolean swagger;
	private String ontologies_file;
	private String jena_doc_manager;

	public JenaLizardConfiguration() {

	}

	public JenaLizardConfiguration(Properties props) {
		this();

		String typeString = props.getProperty(TYPE);
		if (typeString != null) {
			typeString = typeString.toLowerCase().trim();
			this.inference = Boolean.parseBoolean(props.getProperty(INFERENCE_ON_DATA));
			this.ontologies_file = props.getProperty(ONTOLOGIES_FILE);
			this.swagger = Boolean.parseBoolean(props.getProperty(SWAGGER));
			this.jena_doc_manager = props.getProperty(JENA_DOC_MANAGER);

			if (typeString.equals("virtuoso")) {
				type = RepositoryType.Virtuoso;
				setType(type);
				setVirtuosoUser(props.getProperty(VIRTUOSO_USER));
				setVirtuosoPassword(props.getProperty(VIRTUOSO_PASSWORD));
				setVirtuosoHost(props.getProperty(VIRTUOSO_HOST));
				setVirtuosoPort(props.getProperty(VIRTUOSO_PORT));
				setGraph(props.getProperty(GRAPH));
			} else if (typeString.equals("tdb")) {
				type = RepositoryType.TDB;
				setType(type);
				typeString = props.getProperty(TDB_LOCATION);
				if (typeString != null)
					setTdbLocation(typeString);
			} else if (typeString.equals("inmemory")) {
				type = RepositoryType.InMemory;
			} else if (typeString.equalsIgnoreCase("file")) {
				type = RepositoryType.File;
				this.setModelFilePath(props.getProperty(MODEL_FILEPATH));
				this.setLang(props.getProperty(MODEL_LANG));
			}

		}

	}

	public String getTdbLocation() {
		return tdbLocation;
	}

	public void setTdbLocation(String tdbLocation) {
		this.tdbLocation = tdbLocation;
	}

	public RepositoryType getType() {
		return type;
	}

	public void setType(RepositoryType type) {
		this.type = type;
	}

	public String getVirtuosoUser() {
		return virtuosoUser;
	}

	public void setVirtuosoUser(String virtuosoUser) {
		this.virtuosoUser = virtuosoUser;
	}

	public String getVirtuosoPassword() {
		return virtuosoPassword;
	}

	public void setVirtuosoPassword(String virtuosoPassword) {
		this.virtuosoPassword = virtuosoPassword;
	}

	public String getVirtuosoHost() {
		return virtuosoHost;
	}

	public void setVirtuosoHost(String virtuosoHost) {
		this.virtuosoHost = virtuosoHost;
	}

	public String getVirtuosoPort() {
		return virtuosoPort;
	}

	public void setVirtuosoPort(String virtuosoPort) {
		this.virtuosoPort = virtuosoPort;
	}

	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	public String getModelFilePath() {
		return modelFilePath;
	}

	public void setModelFilePath(String modelFilePath) {
		this.modelFilePath = modelFilePath;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public boolean getInference() {
		return inference;
	}

	public void setInference(boolean inference) {
		this.inference = inference;
	}

	public String getOntologies_file() {
		return ontologies_file;
	}

	public void setOntologies_file(String ontologies_file) {
		this.ontologies_file = ontologies_file;
	}

	public boolean isSwagger() {
		return swagger;
	}

	public void setSwagger(boolean swagger) {
		this.swagger = swagger;
	}

	public String getJena_doc_manager() {
		return jena_doc_manager;
	}

	public void setJena_doc_manager(String jena_doc_manager) {
		this.jena_doc_manager = jena_doc_manager;
	}

	@Override
	public String toString() {
		return "JenaLizardConfiguration [tdbLocation=" + tdbLocation + ", modelFilePath=" + modelFilePath + ", type=" + type + ", virtuosoUser=" + virtuosoUser + ", virtuosoPassword=" + virtuosoPassword + ", virtuosoHost=" + virtuosoHost + ", virtuosoPort=" + virtuosoPort + ", graph=" + graph + ", lang=" + lang + ", inference=" + inference + ", swagger=" + swagger + ", ontologies_file=" + ontologies_file + ", jena_doc_manager=" + jena_doc_manager + "]";
	}
	
	
	

}
