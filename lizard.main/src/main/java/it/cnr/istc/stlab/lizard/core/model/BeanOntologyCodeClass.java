package it.cnr.istc.stlab.lizard.core.model;

import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.LizardClass;
import it.cnr.istc.stlab.lizard.commons.annotations.OntologyClass;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.inmemory.InMemoryLizardClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;

public class BeanOntologyCodeClass extends OntologyCodeClass {

	protected Set<OntClass> superClasses;

	BeanOntologyCodeClass(OntResource resource, OntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
		super(resource, ontologyModel, codeModel);

		init(resource);

		((JDefinedClass) super.jClass)._extends(InMemoryLizardClass.class);

		JExpression expression = jCodeModel.ref(ModelFactory.class).staticInvoke("createOntologyModel").invoke("createOntResource").arg(ontResource.getURI());

		JMethod constructor = ((JDefinedClass) super.jClass).getConstructor(new JType[] { jClass.owner()._ref(RDFNode.class) });
		JVar param = constructor.listParams()[0];
		// constructor.body().invoke("super").arg(param).arg(expression);
		constructor.body().invoke("this");
		constructor.body().assign(JExpr._super().ref(param), param);
		constructor.body().assign(JExpr._super().ref("classResource"), expression);

		// constructor.body().invoke("addInstanceToExtentionalClass");
	}

	private void init(OntResource resource) throws ClassAlreadyExistsException {
		super.ontologyClassType = OntologyCodeClassType.Class;

		if (resource.isURIResource()) {
			String artifactId = packageName + "." + Constants.BEAN_PACKAGE + ".";

			String localName = Constants.getJavaName(resource.getLocalName());

			super.entityName = artifactId + localName + Constants.BEAN_POSTFIX;
			try {
				super.jClass = jCodeModel._class(entityName, ClassType.CLASS);
				/*
				 * Create empty constructor
				 */
				((JDefinedClass) super.jClass).constructor(JMod.PUBLIC);

				/*
				 * Create the constructor that allows to instantiate individuals.
				 */
				JMethod constructor = ((JDefinedClass) super.jClass).constructor(JMod.PUBLIC);
				constructor.param(RDFNode.class, "individual");

				((JDefinedClass) super.jClass)._extends(LizardClass.class);

				JAnnotationUse annotation = ((JDefinedClass) super.jClass).annotate(OntologyClass.class);
				annotation.param("uri", ontResource.getURI());

				/*
				 * Create fields: "id", "isCompleted"
				 */
				((JDefinedClass) super.jClass).field(JMod.PRIVATE, String.class, "id");
				((JDefinedClass) super.jClass).field(JMod.PRIVATE, Boolean.class, "isCompleted");
				/*
				 * Create get and set method for "id" and "isCompleted"
				 */
				JMethod setIdMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, jCodeModel.VOID, "setId");
				JVar idParam = setIdMethod.param(String.class, "id");
				setIdMethod.body().assign(JExpr._this().ref("id"), idParam);

				JMethod getIdMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, String.class, "getId");
				getIdMethod.body()._return(JExpr._this().ref("id"));

				JMethod setIsCompletedMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, jCodeModel.VOID, "setIsCompleted");
				setIsCompletedMethod.body().assign(JExpr._this().ref("isCompleted"), setIsCompletedMethod.param(Boolean.class, "isCompleted"));

				JMethod getIsCompletedMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, Boolean.class, "getIsCompleted");
				getIsCompletedMethod.body()._return(JExpr._this().ref("isCompleted"));
				
			} catch (JClassAlreadyExistsException e) {
				throw new ClassAlreadyExistsException(ontResource);
			}
		}

	}


}
