package it.cnr.istc.stlab.lizard.commons.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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

	<T extends AbstractOntologyCodeClass> T getOntologyClass(OntResource ontResource, Class<T> ontologyClass);

	<T extends AbstractOntologyCodeClass> Map<OntResource, T> getOntologyClasses(Class<T> ontologyEntityClass);

	AbstractOntologyCodeMethod createMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass owner, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range);

	AbstractOntologyCodeClassImpl createClassImplements(AbstractOntologyCodeClassImpl ontologyClass, OntologyCodeInterface... ontologyInterfaces);

	JCodeModel asJCodeModel();

	OntModel asOntModel();

	OntModel getInfOntModel();

	void setInfOntModel(OntModel om);

	BooleanAnonClass createAnonClass(OntClass ontClass);

	Map<Class<? extends AbstractOntologyCodeClass>, Map<OntResource, AbstractOntologyCodeClass>> getEntityMap();

	Map<OntResource, Set<AbstractOntologyCodeMethod>> getMethodMap();

}
