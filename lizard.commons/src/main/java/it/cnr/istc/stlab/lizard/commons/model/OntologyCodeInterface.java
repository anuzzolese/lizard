package it.cnr.istc.stlab.lizard.commons.model;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.LizardInterface;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

public abstract class OntologyCodeInterface extends AbstractOntologyCodeClass {

	protected OntologyCodeClassType ontologyClassType;

	protected OntologyCodeInterface() {
		super.ontologyClassType = OntologyCodeClassType.Interface;
	}

	protected OntologyCodeInterface(OntResource resource, OntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
		super(resource, ontologyModel, codeModel);

		super.ontologyClassType = OntologyCodeClassType.Interface;

		if (resource.isURIResource()) {
			String artifactId = packageName + ".";

			String localName = Constants.getJavaName(resource.getLocalName());

			super.entityName = artifactId + localName;
			try {
				super.jClass = jCodeModel._class(entityName, ClassType.INTERFACE);
				if (super.jClass instanceof JDefinedClass)
					((JDefinedClass) super.jClass)._extends(LizardInterface.class);
			} catch (JClassAlreadyExistsException e) {
				throw new ClassAlreadyExistsException(ontResource);
			}
		}

	}

	protected void extendsClasses(AbstractOntologyCodeClass oClass) {
		if (oClass != null && oClass instanceof OntologyCodeInterface)
			this.extendedClass = oClass;
	}

	public Set<AbstractOntologyCodeClass> listSuperClasses() {
		Set<AbstractOntologyCodeClass> superClasses = new HashSet<AbstractOntologyCodeClass>();
		superClasses.add(extendedClass);
		return superClasses;
	}

}
