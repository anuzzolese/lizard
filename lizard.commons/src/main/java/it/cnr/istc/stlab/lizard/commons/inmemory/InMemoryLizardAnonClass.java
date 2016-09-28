package it.cnr.istc.stlab.lizard.commons.inmemory;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;

import it.cnr.istc.stlab.lizard.commons.PropertyMap;

public class InMemoryLizardAnonClass extends InMemoryLizardClass {

	public InMemoryLizardAnonClass() {
    	super();
    }
    
	public InMemoryLizardAnonClass(RDFNode individual, OntResource classResource) {
    	super(individual, classResource);
    }
    
	public InMemoryLizardAnonClass(RDFNode individual, OntResource classResource, PropertyMap propertyMap) {
    	super(individual, classResource, propertyMap);
    }
	
}
