package it.cnr.istc.stlab.lizard.commons.model;

import java.util.Set;

import javax.lang.model.SourceVersion;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.LizardClass;
import it.cnr.istc.stlab.lizard.commons.annotations.OntologyClass;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;

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
