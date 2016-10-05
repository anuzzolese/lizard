package it.cnr.istc.stlab.lizard.commons.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntResource;

import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

public abstract class ExtentionalOntologyCodeClass extends AbstractOntologyCodeClass {
    
	protected OntologyCodeClassType ontologyClassType;
    
	protected ExtentionalOntologyCodeClass(OntologyCodeInterface ontologyInteface) throws ClassAlreadyExistsException {
        super(ontologyInteface.getOntResource(), ontologyInteface.getOntologyModel(), ontologyInteface.getJCodeModel());
    
    }
    
    public abstract AbstractOntologyCodeMethod createMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass range);
    
    protected void extendsClasses(AbstractOntologyCodeClass oClass){
        if(oClass != null && oClass instanceof ExtentionalOntologyCodeClass)
            this.extendedClass = oClass;
    }
    
    public Set<AbstractOntologyCodeClass> listSuperClasses(){
        Set<AbstractOntologyCodeClass> superClasses = new HashSet<AbstractOntologyCodeClass>();
        superClasses.add(extendedClass);
        return superClasses;
    }

}
