package it.cnr.istc.stlab.lizard.core.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMod;

import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;

public class BeanOntologyCodeInterface extends OntologyCodeInterface {

	protected OntologyCodeClassType ontologyClassType;
	private Set<BeanOntologyCodeInterface> intefacesToExtend = new HashSet<>();

	BeanOntologyCodeInterface(OntResource resource, OntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
		super(resource, ontologyModel, codeModel);
		init();
	}

	private void init() {
		((JDefinedClass) super.jClass).field(JMod.FINAL | JMod.PUBLIC | JMod.STATIC, super.jCodeModel.ref(String.class), "CLASS_IRI", JExpr.lit(ontResource.getURI()));
	}

	public void addInterfaceToExtend(BeanOntologyCodeInterface interfaceToExtend) {
		intefacesToExtend.add(interfaceToExtend);
		((JDefinedClass) super.jClass)._extends((JDefinedClass) interfaceToExtend.jClass);
	}

	public Set<BeanOntologyCodeInterface> listSuperInterfaces() {
		return intefacesToExtend;
	}

	protected void extendsClasses(AbstractOntologyCodeClass oClass) {
		if (oClass != null && oClass instanceof BeanOntologyCodeInterface)
			this.extendedClass = oClass;
	}

	public Set<AbstractOntologyCodeClass> listSuperClasses() {
		Set<AbstractOntologyCodeClass> superClasses = new HashSet<AbstractOntologyCodeClass>();
		superClasses.add(extendedClass);
		return superClasses;
	}

}
