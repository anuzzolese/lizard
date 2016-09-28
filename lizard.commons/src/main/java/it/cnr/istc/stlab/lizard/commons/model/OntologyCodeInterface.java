package it.cnr.istc.stlab.lizard.commons.model;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.SourceVersion;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;

import it.cnr.istc.stlab.lizard.commons.LizardInterface;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.exception.NotAvailableOntologyCodeEntityException;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

public abstract class OntologyCodeInterface extends AbstractOntologyCodeClass {
    
    protected OntologyCodeClassType ontologyClassType;
    
    protected OntologyCodeInterface(OntResource resource, OntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
        super(resource, ontologyModel, codeModel);
        
        super.ontologyClassType = OntologyCodeClassType.Interface;
        
        if(resource.isURIResource()){
            String artifactId = packageName + ".";
            
            String localName = resource.getLocalName();
            
            if(!SourceVersion.isName(localName)) localName = "_" + localName;
            
            super.entityName = artifactId + localName;
            try {
                super.jClass = jCodeModel._class(entityName, ClassType.INTERFACE);
                super.jClass._extends(LizardInterface.class);
            } catch (JClassAlreadyExistsException e) {
                throw new ClassAlreadyExistsException(ontResource);
            }
        }
    
    }
    
    public abstract AbstractOntologyCodeMethod createMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass range);
    
    public abstract void createMethods();
    
    void extendsClasses(AbstractOntologyCodeClass oClass){
        if(oClass != null && oClass instanceof OntologyCodeInterface)
            this.extendedClass = oClass;
    }
    
    public Set<AbstractOntologyCodeClass> listSuperClasses(){
        Set<AbstractOntologyCodeClass> superClasses = new HashSet<AbstractOntologyCodeClass>();
        superClasses.add(extendedClass);
        return superClasses;
    }

}
