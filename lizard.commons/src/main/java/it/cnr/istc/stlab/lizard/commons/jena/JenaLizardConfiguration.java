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
	public static final String JENA_ONTO_POLICY_DOC = "jena_onto_policy";
	public static final String SWAGGER_API_DESCRIPTION_FOLDER = "swagger_api_description_folder";

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
	private String ontologies_file;
	private String jena_doc_manager;
	private String swaggerApiDescriptionFolder;

	public String getSwaggerApiDescriptionFolder() {
		return swaggerApiDescriptionFolder;
	}

	public JenaLizardConfiguration() {

	}

	public JenaLizardConfiguration(Properties props) {
		this();

		String typeString = props.getProperty(TYPE);
		if (typeString != null) {
			typeString = typeString.toLowerCase().trim();
			this.inference = Boolean.parseBoolean(props.getProperty(INFERENCE_ON_DATA));
			this.ontologies_file = props.getProperty(ONTOLOGIES_FILE);
			this.jena_doc_manager = props.getProperty(JENA_ONTO_POLICY_DOC);
			this.swaggerApiDescriptionFolder = props.getProperty(SWAGGER_API_DESCRIPTION_FOLDER);

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

	public String getJenaOntoPolicyDoc() {
		return jena_doc_manager;
	}

	public void setJenaOntoPolicyDoc(String file) {
		this.jena_doc_manager = file;
	}

	public void setSwaggerApiDescriptionFolder(String swaggerApiDescriptionFolder) {
		this.swaggerApiDescriptionFolder = swaggerApiDescriptionFolder;
	}

	@Override
	public String toString() {
		return "JenaLizardConfiguration [modelFilePath=" + modelFilePath + ", type=" + type + ", lang=" + lang + ", inference=" + inference + ", ontologies_file=" + ontologies_file + ", swaggerApiDescriptionFolder=" + swaggerApiDescriptionFolder + "]";
	}

}
