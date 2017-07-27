package it.cnr.istc.stlab.lizard.commons.inmemory;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;

import com.fasterxml.jackson.annotation.JsonIgnore;

import it.cnr.istc.stlab.lizard.commons.ExtentionalLizardClass;
import it.cnr.istc.stlab.lizard.commons.LizardClass;
import it.cnr.istc.stlab.lizard.commons.LizardInterface;
import it.cnr.istc.stlab.lizard.commons.PropertyMap;

public class InMemoryLizardClass extends LizardClass {

	public InMemoryLizardClass() {
    	super();
    }
    
	@JsonIgnore
	public InMemoryLizardClass(RDFNode individual, OntResource classResource) {
    	super(individual, classResource);
    }
    
	@JsonIgnore
	public InMemoryLizardClass(RDFNode individual, OntResource classResource, PropertyMap propertyMap) {
    	super(individual, classResource, propertyMap);
    }
	
	@JsonIgnore
	protected void addInstanceToExtentionalClass(){
    	for(ExtentionalLizardClass<LizardInterface> extentionalClass : extentionalClasses)
    		extentionalClass.addIndividual(this);
    }
    
	@JsonIgnore
    protected void removeInstanceFromExtentionalClass(){
    	for(ExtentionalLizardClass<LizardInterface> extentionalClass : extentionalClasses)
    		extentionalClass.removeIndividual(this);
    }
	
}
