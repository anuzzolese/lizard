package it.cnr.istc.stlab.lizard.commons.model;

import java.util.Collection;
import java.util.Map;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;

import it.cnr.istc.stlab.lizard.commons.exception.NotAvailableOntologyCodeEntityException;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

public interface OntologyCodeModel {
    
	String getBaseNamespace();
    <T extends AbstractOntologyCodeClass> T createOntologyClass(OntResource resource, Class<T> ontologyClass) throws NotAvailableOntologyCodeEntityException;
    <T extends AbstractOntologyCodeClass> T createOntologyClass(OntResource resource, Class<T> ontologyClass, OntologyCodeInterface...ontologyInterfaces) throws NotAvailableOntologyCodeEntityException;
    ExtentionalOntologyCodeClass createExtentionalOntologyClass(OntologyCodeInterface ontologyCodeInterface) throws NotAvailableOntologyCodeEntityException;
    OntologyCodeInterface getOntologyInterface(OntResource ontResource);
    AbstractOntologyCodeClassImpl getOntologyClass(OntResource ontResource);
    <T extends AbstractOntologyCodeClassImpl> Map<OntResource,T> getClassMap();
    Map<OntResource,OntologyCodeInterface> getInterfaceMap();
    AbstractOntologyCodeMethod createMethod(OntologyCodeMethodType methodType, OntResource methodResource, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range);
    AbstractOntologyCodeMethod createMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass owner, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range);
    AbstractOntologyCodeClassImpl createClassImplements(AbstractOntologyCodeClassImpl ontologyClass, OntologyCodeInterface...ontologyInterfaces);
    JCodeModel asJCodeModel();
    OntModel asOntModel();
    BooleanAnonClass createAnonClass(OntClass ontClass);
    public OntologyCodeClass createClass(OntResource resource);
    
    void addExtentionalClasses(AbstractOntologyCodeClassImpl ontologyClass, ExtentionalOntologyCodeClass...extentionalOntologyCodeClasses);

}
