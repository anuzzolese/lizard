package it.cnr.istc.stlab.lizard.commons;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;

public interface LizardInterface {

	RDFNode getIndividual();
    void setIndividual(RDFNode individual);
    OntResource getClassResource();
    void setClassResource(OntResource classResource);
    PropertyMap getPropertyMap();
    
    void setPropertyMap(PropertyMap propertyMap);
    
    Object getPropertyValue(OntResource ontResource, Class<? extends Object> objectClass);
    
    void setPropertyValue(OntResource ontResource, Object object);
    
    String getId();
    
    void setId(String id);
}
