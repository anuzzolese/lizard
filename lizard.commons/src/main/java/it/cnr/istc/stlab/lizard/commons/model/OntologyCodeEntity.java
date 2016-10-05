package it.cnr.istc.stlab.lizard.commons.model;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;

public interface OntologyCodeEntity {
    
    JCodeModel getJCodeModel();
    String getPackageName();
    OntResource getOntResource();
    String getEntityName();
    OntologyCodeModel getOntologyModel();

}
