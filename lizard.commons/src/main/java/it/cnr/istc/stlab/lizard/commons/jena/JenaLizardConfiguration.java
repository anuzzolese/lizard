package it.cnr.istc.stlab.lizard.commons.jena;

import java.util.Properties;

public class JenaLizardConfiguration {
	
	public static final String LOCATION = "it.cnr.istc.stlab.lizard.jena.model.location";
	public static final String TDB = "it.cnr.istc.stlab.lizard.jena.model.tdb";
	
	private String location;
	private boolean tdb;
	
	public JenaLizardConfiguration() {
		
	}
	
	public JenaLizardConfiguration(Properties props) {
		this();
		String tmp = props.getProperty(LOCATION);
		if(tmp != null) setLocation(tmp);
		
		tmp = props.getProperty(TDB);
		if(tmp != null) {
			boolean tmpTdb = Boolean.valueOf(tmp);
			this.tdb = tmpTdb;
		}
		else tdb = false;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public boolean isTdb() {
		return tdb;
	}
	
	public void setTdb(boolean tdb) {
		this.tdb = tdb;
	}
	
	@Override
	public int hashCode() {
		return location.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof JenaLizardConfiguration){
			String loc = ((JenaLizardConfiguration)obj).getLocation();
			if(loc != null)
				return loc.equals(location);
		}
		return false;
	}

}
