package it.cnr.istc.stlab.lizard.commons;

import java.net.URI;

import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;

public class OntologyCodeProject {

	private OntologyCodeModel ontologyCodeModel;
	private URI ontologyURI;

	public OntologyCodeProject(URI ontologyURI, OntologyCodeModel ontologyCodeModel) {
		this.ontologyCodeModel = ontologyCodeModel;
		this.ontologyURI = ontologyURI;
	}

	public OntologyCodeModel getOntologyCodeModel() {
		return ontologyCodeModel;
	}

	public URI getOntologyURI() {
		return ontologyURI;
	}

	public void setOntologyURI(URI ontologyURI) {
		this.ontologyURI = ontologyURI;
	}

}
