package it.cnr.istc.stlab.lizard.commons;

import java.util.Set;

import org.apache.jena.ontology.OntResource;

public interface ExtentionalLizardClass <T extends LizardInterface> {
	
	OntResource getClassResource();
    
    void setClassResource(OntResource classResource);
    
    void addIndividual(T individual);
    
    T removeIndividual(T individual);
    
    T getIndividual(String id);
    
    Set<T> getIndividuals();
    
    Set<T> getIndividualsByProperty(OntResource property);

}
