package it.cnr.istc.stlab.lizard.commons.model;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JGenerifiable;

public interface OntologyCodeEntity {
    
    JCodeModel getJCodeModel();
    String getPackageName();
    JGenerifiable getJCodeEntity();
    OntResource getOntResource();
    String getEntityName();
    OntologyCodeModel getOntologyModel();

}
