package it.cnr.istc.stlab.lizard.commons;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Resource;

public class LizardAnonInterface extends LizardClass {

	public LizardAnonInterface() {
		super();
	}
	
	public LizardAnonInterface(Resource individual, OntResource classResource) {
    	super(individual, classResource);
    }
	
	public LizardAnonInterface(Resource individual, OntResource classResource, PropertyMap propertyMap) {
    	super(individual, classResource, propertyMap);
    }
	
}
