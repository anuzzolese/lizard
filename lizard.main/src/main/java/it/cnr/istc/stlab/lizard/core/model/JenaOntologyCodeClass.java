package it.cnr.istc.stlab.lizard.core.model;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.LizardClass;
import it.cnr.istc.stlab.lizard.commons.annotations.OntologyClass;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.inmemory.InMemoryLizardClass;
import it.cnr.istc.stlab.lizard.commons.jena.RuntimeJenaLizardContext;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JWhileLoop;

public class JenaOntologyCodeClass extends OntologyCodeClass {

	protected Set<OntClass> superClasses;

	protected JenaOntologyCodeClass() {
		super();
	}

	JenaOntologyCodeClass(OntResource resource, OntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
		super(resource, ontologyModel, codeModel);

		init(resource);
		addStaticReferencerMethodInInterface();

	}

	private void createBodyConstructors() {

		((JDefinedClass) super.jClass)._extends(InMemoryLizardClass.class);

		// Constructor taking RDFNode
		JExpression expression = jCodeModel.ref(ModelFactory.class).staticInvoke("createOntologyModel").invoke("createOntResource").arg(ontResource.getURI());

		JMethod constructor = ((JDefinedClass) super.jClass).getConstructor(new JType[] { jClass.owner()._ref(RDFNode.class) });
		JVar param = constructor.listParams()[0];
		JBlock constructorBody = constructor.body();
		constructorBody.invoke("super").arg(param).arg(expression);

		JVar jenaModelVar = constructorBody.decl(jCodeModel._ref(Model.class), "model", jCodeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
		JBlock ifThenBlock = constructorBody._if(jenaModelVar.invoke("contains").arg(JExpr.cast(jCodeModel._ref(Resource.class), JExpr._super().ref("individual"))).arg(jCodeModel.ref(RDF.class).staticRef("type")).arg(expression).not())._then();

		JInvocation invocation = jenaModelVar.invoke("add").arg(JExpr.cast(jCodeModel._ref(Resource.class), JExpr._super().ref("individual"))).arg(jCodeModel.ref(RDF.class).staticRef("type")).arg(expression);
		ifThenBlock.add(invocation);

		// Constructor taking URI

		JMethod constructorURI = ((JDefinedClass) super.jClass).getConstructor(new JType[] { jCodeModel.ref(String.class) });
		JVar paramURI = constructorURI.listParams()[0];
		JBlock constructorBodyURI = constructorURI.body();
		JExpression expressionCreateResource = jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createResource").arg(paramURI);
		constructorBodyURI.invoke("super").arg(expressionCreateResource).arg(expression);

		JVar jenaModelVarURI = constructorBodyURI.decl(jCodeModel._ref(Model.class), "model", jCodeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
		JBlock ifThenBlockURI = constructorBodyURI._if(jenaModelVarURI.invoke("contains").arg(JExpr.cast(jCodeModel._ref(Resource.class), JExpr._super().ref("individual"))).arg(jCodeModel.ref(RDF.class).staticRef("type")).arg(expression).not())._then();

		JInvocation invocationURI = jenaModelVarURI.invoke("add").arg(JExpr.cast(jCodeModel._ref(Resource.class), JExpr._super().ref("individual"))).arg(jCodeModel.ref(RDF.class).staticRef("type")).arg(expression);
		ifThenBlockURI.add(invocationURI);

	}

	private void init(OntResource resource) throws ClassAlreadyExistsException {
		super.ontologyClassType = OntologyCodeClassType.Class;

		if (resource.isURIResource()) {
			String artifactId = packageName + "." + Constants.JENA_PACKAGE + ".";

			String localName = Constants.getJavaName(resource.getLocalName());

			super.entityName = artifactId + localName + Constants.JENA_POSTFIX;
			try {
				super.jClass = jCodeModel._class(entityName, ClassType.CLASS);

				/*
				 * Create the constructor that allows to instantiate individuals.
				 */
				JMethod constructor = ((JDefinedClass) super.jClass).constructor(1);
				constructor.param(RDFNode.class, "individual");

				JMethod constructorURI = ((JDefinedClass) super.jClass).constructor(1);
				constructorURI.param(String.class, "individual");

				((JDefinedClass) super.jClass)._extends(LizardClass.class);

				JAnnotationUse annotation = ((JDefinedClass) super.jClass).annotate(OntologyClass.class);
				annotation.param("uri", ontResource.getURI());

				JMethod setIdMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, jCodeModel.VOID, "setId");
				setIdMethod.param(String.class, "id");
				setIdMethod.body().directStatement("throw new UnsupportedOperationException(\"Unsupported Operation!\");");

				JMethod getIdMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, String.class, "getId");
				getIdMethod.body().directStatement("return super.individual.asResource().getURI();");

				JMethod setIsCompletedMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, jCodeModel.VOID, "setIsCompleted");
				setIsCompletedMethod.param(Boolean.class, "isCompletedMethod");
				setIsCompletedMethod.body().directStatement("throw new UnsupportedOperationException(\"Unsupported Operation!\");");

				JMethod getIsCompletedMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, Boolean.class, "getIsCompleted");
				getIsCompletedMethod.body().directStatement("return true;");

			} catch (JClassAlreadyExistsException e) {
				throw new ClassAlreadyExistsException(ontResource);
			}
		}
		createBodyConstructors();
	}

	private void addStaticReferencerMethodInInterface() {
		// Adding static methods to interface to retrieve a single target object and all objects of the class
		addingGetMethod();
		getAllMethod();
	}

	private void getAllMethod() {
		AbstractOntologyCodeClass interfaceClass = ontologyModel.getOntologyClass(ontResource, BeanOntologyCodeInterface.class);

		JClass retType = ontologyModel.asJCodeModel().ref(Set.class).narrow(interfaceClass.asJDefinedClass());
		JClass retTypeImpl = ontologyModel.asJCodeModel().ref(HashSet.class).narrow(interfaceClass.asJDefinedClass());

		JMethod getAllMethod = ((JDefinedClass) interfaceClass.asJDefinedClass()).method(JMod.PUBLIC | JMod.STATIC, retType, "getAll");

		JBlock staticMethodBlock = getAllMethod.body();

		JVar retVar = staticMethodBlock.decl(retType, "ret", JExpr._new(retTypeImpl));

		JExpression rdfTypeExpr = jCodeModel.ref(RDF.class).staticRef("type");
		JExpression owlTypeExpr = jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createResource").arg(ontResource.toString());

		JVar modelVar = staticMethodBlock.decl(ontologyModel.asJCodeModel().ref(Model.class), "model", ontologyModel.asJCodeModel().ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
		JVar stmtItVar = staticMethodBlock.decl(ontologyModel.asJCodeModel().ref(StmtIterator.class), "stmtIt", modelVar.invoke("listStatements").arg(JExpr._null()).arg(rdfTypeExpr).arg(owlTypeExpr));

		/*
		 * While loop to iterate StmtIterator statements
		 */
		JWhileLoop whileLoop = staticMethodBlock._while(stmtItVar.invoke("hasNext"));
		JBlock whileLoopBlock = whileLoop.body();
		JVar stmtVar = whileLoopBlock.decl(jCodeModel.ref(Statement.class), "stmt", stmtItVar.invoke("next"));

		JVar subjVar = whileLoopBlock.decl(jCodeModel.ref(Resource.class), "subj", stmtVar.invoke("getSubject"));

		JVar indVar = whileLoopBlock.decl(interfaceClass.asJDefinedClass(), "individual", JExpr._new(super.jClass).arg(subjVar));

		whileLoopBlock.add(retVar.invoke("add").arg(indVar));

		staticMethodBlock._return(retVar);
	}

	private void addingGetMethod() {

		AbstractOntologyCodeClass interfaceClass = ontologyModel.getOntologyClass(ontResource, BeanOntologyCodeInterface.class);

		JMethod getMethod = ((JDefinedClass) interfaceClass.asJDefinedClass()).method(JMod.PUBLIC | JMod.STATIC, interfaceClass.asJDefinedClass(), "get");

		JVar param = getMethod.param(String.class, "entityURI");
		JBlock methodBlock = getMethod.body();

		JVar retEntity = methodBlock.decl(interfaceClass.asJDefinedClass(), "_entity", JExpr._null());
		JExpression resourceExpr = jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createResource").arg(param);
		JExpression rdfTypeExpr = jCodeModel.ref(RDF.class).staticRef("type");
		JExpression owlTypeExpr = jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createResource").arg(ontResource.toString());

		JExpression modelExpr = jCodeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel");
		JVar modelVar = methodBlock.decl(jCodeModel._ref(Model.class), "model", modelExpr);

		JConditional ifBlock = methodBlock._if(modelVar.invoke("contains").arg(resourceExpr).arg(rdfTypeExpr).arg(owlTypeExpr));
		JBlock ifThenBlock = ifBlock._then();
		ifThenBlock.assign(retEntity, JExpr._new(super.jClass).arg(resourceExpr));

		methodBlock._return(retEntity);
	}

}
