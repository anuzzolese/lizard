package it.cnr.istc.stlab.lizard.core.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;

import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;

public class BeanOntologyCodeInterface extends OntologyCodeInterface {
    
    protected OntologyCodeClassType ontologyClassType;
    
    BeanOntologyCodeInterface(OntResource resource, OntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
        super(resource, ontologyModel, codeModel);
    }
    
    protected void extendsClasses(AbstractOntologyCodeClass oClass){
        if(oClass != null && oClass instanceof BeanOntologyCodeInterface)
            this.extendedClass = oClass;
    }
    
    public Set<AbstractOntologyCodeClass> listSuperClasses(){
        Set<AbstractOntologyCodeClass> superClasses = new HashSet<AbstractOntologyCodeClass>();
        superClasses.add(extendedClass);
        return superClasses;
    }

}
