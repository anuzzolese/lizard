package it.cnr.istc.stlab.lizard.commons;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface LizardInterface {

	@JsonIgnore
	RDFNode getIndividual();

	@JsonIgnore
	void setIndividual(RDFNode individual);

	@JsonIgnore
	OntResource getClassResource();

	@JsonIgnore
	void setClassResource(OntResource classResource);

	@JsonIgnore
	PropertyMap getPropertyMap();

	@JsonIgnore
	void setPropertyMap(PropertyMap propertyMap);

	@JsonIgnore
	Object getPropertyValue(OntResource ontResource, Class<? extends Object> objectClass);

	@JsonIgnore
	void setPropertyValue(OntResource ontResource, Object object);

	public <T extends LizardInterface> T as(Class<T> klass);

}
