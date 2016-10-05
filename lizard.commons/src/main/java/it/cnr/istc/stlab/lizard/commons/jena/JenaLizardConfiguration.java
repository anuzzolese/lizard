package it.cnr.istc.stlab.lizard.commons.jena;

import java.util.Properties;

public class JenaLizardConfiguration {
	
	public static final String TYPE = "type";
	public static final String TDB_LOCATION = "location";
	public static final String VIRTUOSO_USER = "user";
	public static final String VIRTUOSO_PASSWORD = "password";
	public static final String VIRTUOSO_HOST = "host";
	public static final String VIRTUOSO_PORT = "port";
	
	private String tdbLocation;
	private RepositoryType type;
	private String virtuosoUser;
	private String virtuosoPassword;
	private String virtuosoHost;
	private String virtuosoPort;
	
	public JenaLizardConfiguration() {
		
	}
	
	public JenaLizardConfiguration(Properties props) {
		this();
		
		String tmp = props.getProperty(TYPE);
		if(tmp != null) {
			tmp = tmp.toLowerCase().trim();
			if(tmp.equals("virtuoso")) {
				type = RepositoryType.Virtuoso;
				setType(type);
				setVirtuosoUser(props.getProperty(VIRTUOSO_USER, "dba"));
				setVirtuosoPassword(props.getProperty(VIRTUOSO_PASSWORD, "dba"));
				setVirtuosoHost(props.getProperty(VIRTUOSO_HOST, "localhost"));
				setVirtuosoPort(props.getProperty(VIRTUOSO_PORT, "1111"));
				
			}
			else if(tmp.equals("tdb")){
				type = RepositoryType.TDB;
				setType(type);
				tmp = props.getProperty(TDB_LOCATION);
				if(tmp != null) setTdbLocation(tmp);
			}
			else if(tmp.equals("inmemory")) type = RepositoryType.InMemory;
			
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
	
	
	@Override
	public int hashCode() {
		int code = -1;
		switch (type) {
		case Virtuoso:
			code = (getVirtuosoHost()+getVirtuosoPort()).hashCode();
			break;

		case TDB:
			code = getTdbLocation().hashCode();
			break;
		default:
			break;
		}
		return code;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof JenaLizardConfiguration){
			if(hashCode() == obj.hashCode())
				return true;
		}
		return false;
	}

}
