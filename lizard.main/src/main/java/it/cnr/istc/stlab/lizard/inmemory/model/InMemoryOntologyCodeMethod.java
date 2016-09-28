package it.cnr.istc.stlab.lizard.inmemory.model;

import java.util.Collection;
import java.util.Set;

import javax.lang.model.SourceVersion;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;

import it.cnr.istc.stlab.lizard.commons.PrefixRegistry;
import it.cnr.istc.stlab.lizard.commons.annotations.ObjectPropertyAnnotation;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

public class InMemoryOntologyCodeMethod extends OntologyCodeMethod {

    InMemoryOntologyCodeMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass owner, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range, OntologyCodeModel ontologyModel, JCodeModel codeModel) {
        super(methodType, methodResource, owner, domain, range, ontologyModel, codeModel);
    }
    
    
    
    public void annotate(OntologyAnnotation annotation){
        jMethod.annotate(annotation.asJCode());
    }
    
    @Override
    public int hashCode() {
    	return methodType.hashCode() + super.hashCode();
    }

}
