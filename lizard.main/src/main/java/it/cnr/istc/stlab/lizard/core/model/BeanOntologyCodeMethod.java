package it.cnr.istc.stlab.lizard.core.model;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.LizardInterface;
import it.cnr.istc.stlab.lizard.commons.PrefixRegistry;
import it.cnr.istc.stlab.lizard.commons.annotations.ObjectPropertyAnnotation;
import it.cnr.istc.stlab.lizard.commons.jena.RuntimeJenaLizardContext;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClassImpl;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JWhileLoop;

public class BeanOntologyCodeMethod extends OntologyCodeMethod {

	private static Logger logger = LoggerFactory.getLogger(BeanOntologyCodeMethod.class);

	BeanOntologyCodeMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass owner, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range, OntologyCodeModel ontologyModel, JCodeModel codeModel) {
		super(methodType, methodResource, owner, domain, range, ontologyModel, codeModel);
		init();
	}

	private void init() {
		if (ontResource.isURIResource()) {

			initEntityName();

			logger.trace("Creating method for: " + ontResource.getURI() + " ");

			if (methodType == OntologyCodeMethodType.GET) {
				createGetSignature();
			} else if (methodType == OntologyCodeMethodType.SET) {
				createSetSignature();
			} else if (methodType == OntologyCodeMethodType.REMOVE_ALL) {
				createDeleteSignature();
			} else if (methodType == OntologyCodeMethodType.ADD_ALL) {
				createAddAllSignature();
			}

			annotateMethod();
		}

		if (owner instanceof OntologyCodeClass) {
			if (methodType == OntologyCodeMethodType.GET) {
				addGetBody();
			} else if (methodType == OntologyCodeMethodType.SET) {
				addSetBody();
			} else if (methodType == OntologyCodeMethodType.REMOVE_ALL) {
				addDeleteBody();
			} else if (methodType == OntologyCodeMethodType.ADD_ALL) {
				addAddAllBody();
			}
		}
	}

	private void addAddAllBody() {
		JDefinedClass ownerJClass = (JDefinedClass) owner.asJDefinedClass();
		if (owner instanceof OntologyCodeClass) {
			JBlock methodBody = jMethod.body();
			JVar ownerClassField = ownerJClass.fields().get(entityName);
			if (ownerClassField == null) {
				logger.debug(getClass() + " OWNER " + ontResource + " " + (domain == null));
				if (domain.size() > 0) {
					AbstractOntologyCodeClass fieldType = ((ArrayList<AbstractOntologyCodeClass>) domain).get(0);
					JType setClass = jCodeModel.ref(Set.class).narrow(fieldType.asJDefinedClass());
					ownerClassField = ownerJClass.field(JMod.PRIVATE, setClass, entityName);
				} else {
					ownerClassField = ownerJClass.field(JMod.PRIVATE, jCodeModel.ref(String.class), entityName);
				}
			}

			logger.debug("Number of parameters  " + jMethod.params().size());

			methodBody.invoke(ownerClassField, "addAll").arg(jMethod.params().get(0));

		}

	}

	private void annotateMethod() {

		JDefinedClass domainJClass = (JDefinedClass) owner.asJDefinedClass();

		char[] fieldNameChars = entityName.toCharArray();
		StringBuilder sb = new StringBuilder();

		Character previous = null;
		for (char fieldNameChar : fieldNameChars) {
			if (previous != null) {
				if (Character.isLowerCase(previous) && Character.isUpperCase(fieldNameChar))
					sb.append("_");
			}
			sb.append(Character.toUpperCase(fieldNameChar));
			previous = fieldNameChar;
		}

		if (owner instanceof OntologyCodeInterface) {
			JFieldVar staticField = domainJClass.fields().get(sb.toString());
			if (staticField == null)
				staticField = domainJClass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, String.class, sb.toString(), JExpr.lit(ontResource.getURI()));

			JAnnotationUse jAnnotationUse = jMethod.annotate(ObjectPropertyAnnotation.class);
			jAnnotationUse.param("uri", staticField);
			jAnnotationUse.param("method", methodType);

		} else {
			jMethod.annotate(Override.class);

			if (this.methodType == OntologyCodeMethodType.GET) {
				addClassCentricStaticMethod(jCodeModel, sb.toString());
				addClassCentricStaticMethodWithParam(jCodeModel, sb.toString());
			}
		}

	}

	private void initEntityName() {
		String namespace = ontResource.getNameSpace();

		String prefix = ontologyModel.asOntModel().getNsURIPrefix(namespace);
		// look-up on prefix.cc
		if (prefix == null)
			prefix = PrefixRegistry.getInstance().getNsPrefix(namespace);
		// if the prefix is again null, then we create it
		if (prefix == null)
			prefix = PrefixRegistry.getInstance().createNsPrefix(namespace);

		String localName = Constants.getJavaName(ontResource.getLocalName());

		if (prefix.isEmpty())
			entityName = localName;
		else
			entityName = prefix + "_" + localName;

	}

	private void createDeleteSignature() {
		// create delete method
		JDefinedClass domainJClass = (JDefinedClass) owner.asJDefinedClass();
		String methodName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
		jMethod = domainJClass.method(1, void.class, "removeAll" + methodName);

		if (domain != null) {
			for (AbstractOntologyCodeClass domainClass : domain) {
				String name = domainClass.getEntityName();
				name = name.substring(name.lastIndexOf(".") + 1);
				name = name.substring(0, 1).toLowerCase() + name.substring(1);
				JType setClass = jCodeModel.ref(Set.class).narrow(domainClass.asJDefinedClass());
				jMethod.param(setClass, name);
			}
		} else {
			JType setClass = jCodeModel.ref(Set.class).narrow(range.asJDefinedClass());
			jMethod.param(setClass, entityName);
		}

	}

	private void createAddAllSignature() {

		String methodName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
		JDefinedClass domainJClass = (JDefinedClass) owner.asJDefinedClass();
		jMethod = domainJClass.method(1, void.class, "addAll" + methodName);

		if (domain != null) {
			for (AbstractOntologyCodeClass domainClass : domain) {
				logger.trace("DOMAIN: " + domainClass.getOntResource().getURI());
				String name = domainClass.getEntityName();
				name = name.substring(name.lastIndexOf(".") + 1);
				name = name.substring(0, 1).toLowerCase() + name.substring(1);
				JType setClass = jCodeModel.ref(Set.class).narrow(domainClass.asJDefinedClass());
				jMethod.param(setClass, name);
			}
		} else {
			JType setClass = jCodeModel.ref(Set.class).narrow(range.asJDefinedClass());
			jMethod.param(setClass, entityName);
		}

	}

	private void createSetSignature() {

		String methodName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
		JDefinedClass domainJClass = (JDefinedClass) owner.asJDefinedClass();
		jMethod = domainJClass.method(1, void.class, "set" + methodName);

		if (domain != null) {
			for (AbstractOntologyCodeClass domainClass : domain) {
				logger.trace("DOMAIN: " + domainClass.getOntResource().getURI());
				String name = domainClass.getEntityName();
				name = name.substring(name.lastIndexOf(".") + 1);
				name = name.substring(0, 1).toLowerCase() + name.substring(1);
				JType setClass = jCodeModel.ref(Set.class).narrow(domainClass.asJDefinedClass());
				jMethod.param(setClass, name);
			}
		} else {
			JType setClass = jCodeModel.ref(Set.class).narrow(range.asJDefinedClass());
			jMethod.param(setClass, entityName);
		}

	}

	private void createGetSignature() {
		JType setClass = null;
		// if (ontResource.isDatatypeProperty()) {
		// OntResource rangeResource = ontResource.asDatatypeProperty().getRange();
		// if (rangeResource != null && rangeResource.isURIResource() && LizardCore.hasTypeMapper(rangeResource.getURI())) {
		// setClass = jCodeModel.ref(Set.class).narrow(TypeMapper.getInstance().getSafeTypeByName(rangeResource.getURI()).getJavaClass());
		// } else {
		// setClass = jCodeModel.ref(Set.class).narrow(String.class);
		// }
		// } else {
		setClass = jCodeModel.ref(Set.class).narrow(range.asJDefinedClass());
		// }
		String methodName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
		jMethod = ((JDefinedClass) owner.asJDefinedClass()).method(1, setClass, "get" + methodName);

	}

	@Override
	public int hashCode() {
		return methodType.hashCode() + super.hashCode();
	}

	private void addDeleteBody() {
		JDefinedClass ownerJClass = (JDefinedClass) owner.asJDefinedClass();
		if (owner instanceof OntologyCodeClass) {
			JBlock methodBody = jMethod.body();
			JVar ownerClassField = ownerJClass.fields().get(entityName);
			if (ownerClassField == null) {
				logger.debug(getClass() + " OWNER " + ontResource + " " + (domain == null));
				if (domain.size() > 0) {
					AbstractOntologyCodeClass fieldType = ((ArrayList<AbstractOntologyCodeClass>) domain).get(0);
					JType setClass = jCodeModel.ref(Set.class).narrow(fieldType.asJDefinedClass());
					ownerClassField = ownerJClass.field(JMod.PRIVATE, setClass, entityName);
				} else {
					ownerClassField = ownerJClass.field(JMod.PRIVATE, jCodeModel.ref(String.class), entityName);
				}
			}

			logger.debug("Number of parameters  " + jMethod.params().size());

			methodBody.invoke(ownerClassField, "removeAll").arg(jMethod.params().get(0));

		}
	}

	private void addGetBody() {
		JDefinedClass ownerJClass = (JDefinedClass) owner.asJDefinedClass();
		if (owner instanceof OntologyCodeClass) {

			JType setClass = null;
			JClass rangeJClass = null;

			// if (ontResource.isDatatypeProperty()) {
			// OntResource rangeResource = ontResource.asDatatypeProperty().getRange();
			// if (rangeResource != null && rangeResource.isURIResource() && LizardCore.hasTypeMapper(rangeResource.getURI())) {
			// rangeJClass = jCodeModel.ref(TypeMapper.getInstance().getSafeTypeByName(rangeResource.getURI()).getJavaClass());
			// } else {
			// rangeJClass = jCodeModel.ref(String.class);
			// }
			// } else {
			rangeJClass = range.asJDefinedClass();
			// }
			setClass = jCodeModel.ref(Set.class).narrow(rangeJClass);

			JVar ownerClassField = ownerJClass.fields().get(entityName);
			if (ownerClassField == null)
				ownerClassField = ownerJClass.field(JMod.PRIVATE, setClass, entityName);

			JClass hashSetClass = jCodeModel.ref(HashSet.class).narrow(rangeJClass);
			JMethod ownerClassConstructor = ownerJClass.getConstructor(new JType[] {});
			ownerClassConstructor.body().assign(ownerClassField, JExpr._new(hashSetClass));

			JBlock methodBody = jMethod.body();

			methodBody._return(ownerClassField);
		}

	}

	private void addSetBody() {
		JDefinedClass ownerJClass = (JDefinedClass) owner.asJDefinedClass();
		if (owner instanceof OntologyCodeClass) {
			JBlock methodBody = jMethod.body();
			JVar ownerClassField = ownerJClass.fields().get(entityName);
			if (ownerClassField == null) {
				logger.debug(getClass() + " OWNER " + ontResource + " " + (domain == null));
				if (domain.size() > 0) {
					AbstractOntologyCodeClass fieldType = ((ArrayList<AbstractOntologyCodeClass>) domain).get(0);
					JType setClass = jCodeModel.ref(Set.class).narrow(fieldType.asJDefinedClass());
					ownerClassField = ownerJClass.field(JMod.PRIVATE, setClass, entityName);
				} else {
					ownerClassField = ownerJClass.field(JMod.PRIVATE, jCodeModel.ref(String.class), entityName);
				}
			}

			logger.debug("Number of parameters  " + jMethod.params().size());

			methodBody.assign(ownerClassField, jMethod.params().get(0));
		}
	}

	private void addClassCentricStaticMethod(JCodeModel codeModel, String fieldName) {

		// Adding static methods in the class interface

		if (this.methodType == OntologyCodeMethodType.GET) {

			OntologyCodeInterface ontInterface = ontologyModel.getOntologyClass(owner.getOntResource(), BeanOntologyCodeInterface.class);

			if (ontInterface != null) {

				JDefinedClass interfaceClass = (JDefinedClass) ontInterface.asJDefinedClass();

				JFieldVar staticField = interfaceClass.fields().get(fieldName);

				if (staticField != null) {
					String staticMethodName = fieldName;
					staticMethodName = "getBy" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);

					JClass retType = codeModel.ref(Set.class).narrow(interfaceClass);
					JClass retTypeImpl = codeModel.ref(HashSet.class).narrow(interfaceClass);

					JMethod staticMethod = interfaceClass.method(JMod.PUBLIC | JMod.STATIC, retType, staticMethodName);
					JBlock staticMethodBlock = staticMethod.body();

					JVar retVar = staticMethodBlock.decl(retType, "ret", JExpr._new(retTypeImpl));

					JVar predicateVar = staticMethodBlock.decl(codeModel.ref(Property.class), "predicate", codeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(staticField));

					JVar modelVar = staticMethodBlock.decl(codeModel.ref(Model.class), "model", codeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));

					JVar stmtItVar = staticMethodBlock.decl(codeModel.ref(StmtIterator.class), "stmtIt", modelVar.invoke("listStatements").arg(JExpr._null()).arg(predicateVar).arg(JExpr.cast(codeModel._ref(RDFNode.class), JExpr._null())));

					/*
					 * While loop to iterate StmtIterator statements
					 */
					JWhileLoop whileLoop = staticMethodBlock._while(stmtItVar.invoke("hasNext"));
					JBlock whileLoopBlock = whileLoop.body();
					JVar stmtVar = whileLoopBlock.decl(codeModel.ref(Statement.class), "stmt", stmtItVar.invoke("next"));

					JVar subjVar = whileLoopBlock.decl(codeModel.ref(Resource.class), "subj", stmtVar.invoke("getSubject"));

					AbstractOntologyCodeClassImpl concreteClass = ontologyModel.getOntologyClass(owner.getOntResource(), JenaOntologyCodeClass.class);

					JVar indVar = whileLoopBlock.decl(ontInterface.asJDefinedClass(), "individual", JExpr._new(concreteClass.asJDefinedClass()).arg(subjVar));

					whileLoopBlock.add(retVar.invoke("add").arg(indVar));

					staticMethodBlock._return(retVar);
				}
			}
		}
	}

	private void addClassCentricStaticMethodWithParam(JCodeModel codeModel, String fieldName) {
		if (this.methodType == OntologyCodeMethodType.GET) {
			OntologyCodeInterface ontInterface = ontologyModel.getOntologyClass(owner.getOntResource(), BeanOntologyCodeInterface.class);

			if (ontInterface != null) {
				JDefinedClass interfaceClass = (JDefinedClass) ontInterface.asJDefinedClass();

				JFieldVar staticField = interfaceClass.fields().get(fieldName);

				if (staticField != null) {
					String staticMethodName = fieldName;
					staticMethodName = "getBy" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);

					JClass retType = codeModel.ref(Set.class).narrow(interfaceClass);
					JClass retTypeImpl = codeModel.ref(HashSet.class).narrow(interfaceClass);

					JMethod staticMethod = interfaceClass.method(JMod.PUBLIC | JMod.STATIC, retType, staticMethodName);
					JVar inputParam = staticMethod.param(LizardInterface.class, "value");

					JBlock staticMethodBlock = staticMethod.body();

					JVar retVar = staticMethodBlock.decl(retType, "ret", JExpr._new(retTypeImpl));

					JVar predicateVar = staticMethodBlock.decl(codeModel.ref(Property.class), "predicate", codeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(staticField));

					JVar modelVar = staticMethodBlock.decl(codeModel.ref(Model.class), "model", codeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));

					JVar stmtItVar = staticMethodBlock.decl(codeModel.ref(StmtIterator.class), "stmtIt", modelVar.invoke("listStatements").arg(JExpr._null()).arg(predicateVar).arg(inputParam.invoke("getIndividual")));
					/*
					 * While loop to iterate StmtIterator statements
					 */
					JWhileLoop whileLoop = staticMethodBlock._while(stmtItVar.invoke("hasNext"));
					JBlock whileLoopBlock = whileLoop.body();
					JVar stmtVar = whileLoopBlock.decl(codeModel.ref(Statement.class), "stmt", stmtItVar.invoke("next"));

					JVar subjVar = whileLoopBlock.decl(codeModel.ref(Resource.class), "subj", stmtVar.invoke("getSubject"));

					AbstractOntologyCodeClassImpl concreteClass = ontologyModel.getOntologyClass(owner.getOntResource(), JenaOntologyCodeClass.class);

					JVar indVar = whileLoopBlock.decl(ontInterface.asJDefinedClass(), "individual", JExpr._new(concreteClass.asJDefinedClass()).arg(subjVar));

					whileLoopBlock.add(retVar.invoke("add").arg(indVar));

					staticMethodBlock._return(retVar);
				}
			}
		}
	}

}
