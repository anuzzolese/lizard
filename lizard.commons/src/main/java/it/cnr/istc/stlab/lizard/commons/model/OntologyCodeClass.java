package it.cnr.istc.stlab.lizard.commons.model;

import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;

import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;

public abstract class OntologyCodeClass extends AbstractOntologyCodeClassImpl {
    
    protected Set<OntClass> superClasses;
    
    protected OntologyCodeClass(){
    	super();
    }
    
    protected OntologyCodeClass(OntResource resource, OntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
        super(resource, ontologyModel, codeModel);
    }
    
    public boolean isSingleInheritance(){
        if(superClasses.size() < 2) return true;
        else return false;
    }

}
