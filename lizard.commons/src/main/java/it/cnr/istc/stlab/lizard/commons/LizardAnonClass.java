package it.cnr.istc.stlab.lizard.commons;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Resource;

public class LizardAnonClass extends LizardClass {

	public LizardAnonClass() {
		super();
	}
	
	public LizardAnonClass(Resource individual, OntResource classResource) {
    	super(individual, classResource);
    }
	
	public LizardAnonClass(Resource individual, OntResource classResource, PropertyMap propertyMap) {
    	super(individual, classResource, propertyMap);
    }
	
}
