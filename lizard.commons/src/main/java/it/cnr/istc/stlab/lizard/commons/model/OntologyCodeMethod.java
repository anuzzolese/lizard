package it.cnr.istc.stlab.lizard.commons.model;

import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

import java.util.Collection;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;

public abstract class OntologyCodeMethod extends AbstractOntologyCodeMethod {
	

	protected OntologyCodeMethod(){
		super();
	}
	
	protected OntologyCodeMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass owner, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range, OntologyCodeModel ontologyModel, JCodeModel codeModel) {
        super(methodType, methodResource, owner, domain, range, ontologyModel, codeModel);
    }
    
    @Override
    public int hashCode() {
    	return methodType.hashCode() + super.hashCode();
    }
    

}
