package it.cnr.istc.stlab.lizard.commons;

public class URICondition implements Condition {

	private String uri;
	
	public URICondition() {
		
	}
	
	public URICondition(String uri){
		this.uri = uri;
	}
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
}
