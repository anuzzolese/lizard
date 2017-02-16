package it.cnr.istc.stlab.lizard.core.model;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.PrefixRegistry;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

public class RestOntologyCodeMethod extends OntologyCodeMethod {

	private static Logger logger = LoggerFactory.getLogger(RestOntologyCodeMethod.class);

	RestOntologyCodeMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass owner, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range, OntologyCodeModel ontologyModel, JCodeModel codeModel) {
		super(methodType, methodResource, owner, domain, range, ontologyModel, codeModel);

		if (methodResource.isURIResource()) {

			String namespace = methodResource.getNameSpace();

			String prefix = ontologyModel.asOntModel().getNsURIPrefix(namespace);

			// look-up on prefix.cc
			if (prefix == null)
				prefix = PrefixRegistry.getInstance().getNsPrefix(namespace);
			// if the prefix is again null, then we create it
			if (prefix == null)
				prefix = PrefixRegistry.getInstance().createNsPrefix(namespace);

			String localName = Constants.getJavaName(methodResource.getLocalName());

			if (prefix.isEmpty())
				entityName = localName;
			else
				entityName = prefix + "_" + localName;

			String methodName;
			JType responseType = codeModel.ref(Response.class);

			OntologyCodeInterface javaInterface = ontologyModel.getOntologyClass(owner.getOntResource(), BeanOntologyCodeInterface.class);
			JType setType = codeModel.ref(Set.class).narrow(javaInterface.asJDefinedClass());

			switch (methodType) {
			case Get:
				methodName = "getBy" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);

				JMethod temp = ((JDefinedClass) owner.asJDefinedClass()).getMethod(methodName, new JType[] { codeModel._ref(String.class) });

				if (temp == null) {
					jMethod = ((JDefinedClass) owner.asJDefinedClass()).method(JMod.PUBLIC, responseType, methodName);

					if (owner instanceof OntologyCodeClass) {

						// Create annotation GET method
						jMethod.annotate(GET.class);
						jMethod.annotate(Path.class).param("value", "/" + entityName);
						String operationId = ((RestOntologyCodeClass) owner).getPath().substring(1) + "_" + entityName;
						jMethod.annotate(ApiOperation.class).param("value", "Get by " + entityName).param("nickname", operationId);

						// GET Parameter
						JVar param = jMethod.param(String.class, "constraint");
						param.annotate(ApiParam.class).param("value", entityName).param("required", false);
						param.annotate(QueryParam.class).param("value", entityName);

						JBlock methodBody = jMethod.body();

						JVar responseBuilderVar = methodBody.decl(codeModel._ref(ResponseBuilder.class), "_responseBuilder", JExpr._null());

						JType hashSetType = codeModel.ref(HashSet.class).narrow(javaInterface.asJDefinedClass());

						JVar kbSetVar = methodBody.decl(setType, "_kbSet", javaInterface.asJDefinedClass().staticInvoke(methodName));
						JVar retSetVar = methodBody.decl(setType, "_retSet", JExpr._new(hashSetType));

						JConditional ifBlock = methodBody._if(kbSetVar.ne(JExpr._null()));
						/*
						 * Then
						 */
						JBlock ifThenBlock = ifBlock._then();
						JForEach forEach = ifThenBlock.forEach(javaInterface.asJDefinedClass(), "_obj", kbSetVar);

						JBlock forEachBlock = forEach.body();
						JExpression castExpression = JExpr.cast(ontologyModel.getOntologyClass(owner.getOntResource(), JenaOntologyCodeClass.class).asJDefinedClass(), forEach.var());
						forEachBlock.add(retSetVar.invoke("add").arg(castExpression.invoke("asMicroBean")));

						ifThenBlock.assign(responseBuilderVar, codeModel.ref(Response.class).staticInvoke("ok").arg(retSetVar));

						/*
						 * Else
						 */
						JBlock ifElseBlock = ifBlock._else();
						ifElseBlock.assign(responseBuilderVar, codeModel.ref(Response.class).staticInvoke("status").arg(codeModel.ref(Status.class).staticRef("NOT_FOUND")));

						methodBody._return(responseBuilderVar.invoke("build"));

						/*
						 * Entity-centric method
						 */
						methodName = "_entityGetBy" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);

						JMethod jMethod = ((JDefinedClass) owner.asJDefinedClass()).method(JMod.PUBLIC, responseType, methodName);

						jMethod.annotate(GET.class);
						jMethod.annotate(Path.class).param("value", "/entity/" + entityName);
						jMethod.annotate(ApiOperation.class).param("value", "Get " + entityName + " values of this entity").param("nickname", "entity_" + operationId);

						JVar idVar = jMethod.param(String.class, "id");
						idVar.annotate(ApiParam.class).param("value", "id").param("required", true);
						idVar.annotate(QueryParam.class).param("value", "id");

						param = jMethod.param(String.class, "constraint");
						param.annotate(ApiParam.class).param("value", entityName).param("required", false);
						param.annotate(QueryParam.class).param("value", entityName);

						JBlock entityMethodBody = jMethod.body();

						JVar entityResponseBuilderVar = entityMethodBody.decl(codeModel._ref(ResponseBuilder.class), "_responseBuilder", JExpr._null());

						AbstractOntologyCodeClass r = ((ArrayList<AbstractOntologyCodeClass>) domain).get(0);
						AbstractOntologyCodeClass o = ontologyModel.getOntologyClass(owner.getOntResource(), BeanOntologyCodeInterface.class);

						OntResource rOntRes = r.getOntResource();
						OntologyCodeClass beanClass = null;

						boolean anon = false;
						if (rOntRes.isURIResource()) {
							beanClass = ontologyModel.getOntologyClass(rOntRes, BeanOntologyCodeClass.class);
						} else {
							anon = true;
							beanClass = ontologyModel.getOntologyClass(rOntRes, BooleanAnonClass.class);
							if (beanClass == null) {
								beanClass = ontologyModel.createAnonClass(rOntRes.asClass());
							}
						}

						JType entityBeanSetType = null;
						JType entityBeanHashSetType = null;

						String getMethodName = "get" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);

						JDefinedClass jdc = (JDefinedClass) ontologyModel.getOntologyClass(owner.getOntResource(), BeanOntologyCodeInterface.class).asJDefinedClass();
						// JMethod meth = jdc.getMethod(entityName, new
						// JType[]{});

						logger.debug("Method name: " + getMethodName);

						JMethod meth = jdc.getMethod(getMethodName, new JType[] {});

						JType methRetType = meth.type();

						JType methRetNarrowedType = methRetType.boxify().getTypeParameters().get(0);

						JType entitySetType = methRetType;

						entityBeanSetType = methRetType;
						entityBeanHashSetType = codeModel.ref(HashSet.class).narrow(methRetNarrowedType);

						/*
						 * if(beanClass != null && methodResource.isObjectProperty()){ if(methodResource.isDatatypeProperty()){ entityBeanSetType = codeModel.ref(Set.class).narrow(beanClass .asJDefinedClass()); entityBeanHashSetType = codeModel .ref(HashSet.class).narrow(beanClass.asJDefinedClass
						 * ()); } else{ entityBeanSetType = codeModel.ref(Set.class ).narrow(beanClass.asJDefinedClass()); entityBeanHashSetType = codeModel.ref(HashSet.class).narrow (beanClass.asJDefinedClass()); } } else{ entityBeanSetType = codeModel.ref(Set.class).narrow(rangeJClass);
						 * entityBeanHashSetType = codeModel.ref(HashSet.class).narrow(rangeJClass); }
						 */

						JVar entityVar = entityMethodBody.decl(o.asJDefinedClass(), "_entity", o.asJDefinedClass().staticInvoke("get").arg(idVar));

						JVar entitykbSetVar = entityMethodBody.decl(entitySetType, "_kbSet", entityVar.invoke(getMethodName));
						// JVar entitykbSetVar =
						// entityMethodBody.decl(entitySetType, "_kbSet",
						// entityVar.invoke(entityName));
						JVar entityRetSetVar = entityMethodBody.decl(entityBeanSetType, "_retSet", JExpr._new(entityBeanHashSetType));

						JConditional entityIfBlock = entityMethodBody._if(kbSetVar.ne(JExpr._null()));

						/*
						 * Then
						 */
						JBlock entityIfThenBlock = entityIfBlock._then();
						JForEach entityForEach = entityIfThenBlock.forEach(methRetNarrowedType, "_obj", entitykbSetVar);

						JBlock entityForEachBlock = entityForEach.body();
						if (methodResource.isDatatypeProperty()) {
							entityForEachBlock.add(entityRetSetVar.invoke("add").arg(forEach.var()));
						} else if (!anon) {
							castExpression = JExpr.cast(ontologyModel.getOntologyClass(beanClass.getOntResource(), JenaOntologyCodeClass.class).asJDefinedClass(), forEach.var());
							entityForEachBlock.add(entityRetSetVar.invoke("add").arg(castExpression.invoke("asMicroBean")));
						} else {
							castExpression = JExpr.cast(ontologyModel.getOntologyClass(beanClass.getOntResource(), BooleanAnonClass.class).asJDefinedClass(), forEach.var());
							entityForEachBlock.add(entityRetSetVar.invoke("add").arg(castExpression));
						}

						entityIfThenBlock.assign(entityResponseBuilderVar, codeModel.ref(Response.class).staticInvoke("ok").arg(retSetVar));

						/*
						 * Else
						 */
						JBlock entityIfElseBlock = entityIfBlock._else();
						entityIfElseBlock.assign(entityResponseBuilderVar, codeModel.ref(Response.class).staticInvoke("status").arg(codeModel.ref(Status.class).staticRef("NOT_FOUND")));

						entityMethodBody._return(responseBuilderVar.invoke("build"));
					}
				}
				break;

			case Set:

				if (methodResource.isDatatypeProperty()) {
					// The property corresponding to the method is a datatype property
					createSetMethodForDatatypeProperty();
				} else {
					// The property corresponding to the method is a object property
					createSetMethodForObjectProperty();
				}

				break;
			case Delete:

				if (methodResource.isDatatypeProperty()) {
					// The property corresponding to the method is a datatype property
					createDeleteMethodForDatatypeProperty();
				} else {
					// The property corresponding to the method is a object property
					createDeleteMethodForObjectProperty();
				}
				break;
			default:
				break;
			}
		}
	}

	public void createDeleteMethodForObjectProperty() {

		JType responseType = super.jCodeModel.ref(Response.class);
		String methodName = "delete" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);

		JMethod tempSet = ((JDefinedClass) owner.asJDefinedClass()).getMethod(methodName, new JType[] { super.jCodeModel._ref(String.class), super.jCodeModel._ref(String.class) });

		if (tempSet == null) {
			jMethod = ((JDefinedClass) owner.asJDefinedClass()).method(JMod.PUBLIC, responseType, methodName);
			if (owner instanceof OntologyCodeClass) {

				// Create annotation SET method
				jMethod.annotate(POST.class);
				jMethod.annotate(Path.class).param("value", "/entity/delete" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1));
				String operationId = "delete_" + ((RestOntologyCodeClass) owner).getPath().substring(1) + "_" + entityName;
				jMethod.annotate(ApiOperation.class).param("value", "Delete " + entityName).param("nickname", operationId);

				// Create SET parameter
				// IRI of the target individual where the property will be added
				JVar idParam = jMethod.param(String.class, "id");
				idParam.annotate(ApiParam.class).param("value", "id").param("required", true);
				idParam.annotate(QueryParam.class).param("value", "id");

				JVar iriRangeParam = jMethod.param(String.class, "value");
				iriRangeParam.annotate(ApiParam.class).param("value", "value").param("required", true);
				iriRangeParam.annotate(QueryParam.class).param("value", "value");

				/*
				 * Method Body
				 */
				JBlock methodBody = jMethod.body();

				// Getting the target individual where the property will be added
				AbstractOntologyCodeClass ownerInterface = ontologyModel.getOntologyClass(owner.getOntResource(), BeanOntologyCodeInterface.class);
				AbstractOntologyCodeClass rangeJenaClass = null;
				AbstractOntologyCodeClass rangeJenaInterface = null;
				if (range == null) {
					rangeJenaClass = ontologyModel.getOntologyClass(ModelFactory.createOntologyModel().getOntResource(OWL.Thing), JenaOntologyCodeClass.class);
					rangeJenaInterface = ontologyModel.getOntologyClass(ModelFactory.createOntologyModel().getOntResource(OWL.Thing), BeanOntologyCodeInterface.class);
				} else {
					rangeJenaClass = ontologyModel.getOntologyClass(range.getOntResource(), JenaOntologyCodeClass.class);
					rangeJenaInterface = ontologyModel.getOntologyClass(range.getOntResource(), BeanOntologyCodeInterface.class);
					if (rangeJenaClass == null) {
						// The range is a boolean class
						rangeJenaClass = ontologyModel.getOntologyClass(range.getOntResource(), BooleanAnonClass.class);
						rangeJenaInterface = ontologyModel.getOntologyClass(range.getOntResource(), BooleanAnonClass.class);
					}
				}

				// Creting set to be added
				JType hashSetType_range = super.jCodeModel.ref(HashSet.class).narrow(rangeJenaInterface.asJDefinedClass());
				JType setType_range = super.jCodeModel.ref(Set.class).narrow(rangeJenaInterface.asJDefinedClass());
				JVar kbSetVar = methodBody.decl(setType_range, "toDelete", JExpr._new(hashSetType_range));
				methodBody.add(kbSetVar.invoke("add").arg(JExpr._new(rangeJenaClass.asJDefinedClass()).arg(jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createResource").arg(iriRangeParam))));

				// add set to the individual
				JVar entityVar = methodBody.decl(ownerInterface.asJDefinedClass(), "_entity", ownerInterface.asJDefinedClass().staticInvoke("get").arg(idParam));
				methodBody.add(entityVar.invoke(methodName).arg(kbSetVar));

				// Respond OK
				JVar responseBuilderVar = methodBody.decl(super.jCodeModel._ref(ResponseBuilder.class), "_responseBuilder", super.jCodeModel.ref(Response.class).staticInvoke("ok"));
				methodBody._return(responseBuilderVar.invoke("build"));
			}
		}

	}

	public void createDeleteMethodForDatatypeProperty() {
		JType responseType = super.jCodeModel.ref(Response.class);
		String methodName = "delete" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);

		JMethod tempSet = ((JDefinedClass) owner.asJDefinedClass()).getMethod(methodName, new JType[] { super.jCodeModel._ref(String.class), super.jCodeModel._ref(String.class) });

		if (tempSet == null) {
			jMethod = ((JDefinedClass) owner.asJDefinedClass()).method(JMod.PUBLIC, responseType, methodName);
			if (owner instanceof OntologyCodeClass) {

				// Create annotation DELETE method
				jMethod.annotate(POST.class);
				jMethod.annotate(Path.class).param("value", "/entity/delete" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1));
				String operationId = "delete_" + ((RestOntologyCodeClass) owner).getPath().substring(1) + "_" + entityName;
				jMethod.annotate(ApiOperation.class).param("value", "Delete " + entityName).param("nickname", operationId);

				// Create SET parameter
				// IRI of the target individual where the property will be added
				JVar idParam = jMethod.param(String.class, "id");
				idParam.annotate(ApiParam.class).param("value", "id").param("required", true);
				idParam.annotate(QueryParam.class).param("value", "id");

				JVar rangeValueParam = jMethod.param(String.class, "value");
				rangeValueParam.annotate(ApiParam.class).param("value", "value").param("required", true);
				rangeValueParam.annotate(QueryParam.class).param("value", "value");

				/*
				 * Method Body
				 */
				JBlock methodBody = jMethod.body();

				// Getting the target individual where the property will be added
				AbstractOntologyCodeClass ownerInterface = ontologyModel.getOntologyClass(owner.getOntResource(), BeanOntologyCodeInterface.class);

				// Getting range class
				Class<?> rangeClass = null;

				logger.debug("OWNER " + owner.getEntityName() + " " + this.ontResource.getLocalName());

				if (range == null) {
					rangeClass = String.class;
				} else {
					logger.debug(range.getOntResource().getLocalName());
					rangeClass = TypeMapper.getInstance().getTypeByName(range.getOntResource().getURI()).getJavaClass();
				}

				// Creting set to be added
				JType hashSetType_range = super.jCodeModel.ref(HashSet.class).narrow(rangeClass);
				JType setType_range = super.jCodeModel.ref(Set.class).narrow(rangeClass);
				JVar kbSetVar = methodBody.decl(setType_range, "toDelete", JExpr._new(hashSetType_range));

				// Adding value to the set that will be added
				if (range == null || rangeClass.equals(String.class)) {
					methodBody.add(kbSetVar.invoke("add").arg(rangeValueParam));
				} else {
					JVar value = methodBody.decl(jCodeModel.ref(rangeClass), "datatype", JExpr.cast(jCodeModel.ref(rangeClass), jCodeModel.ref(TypeMapper.class).staticInvoke("getInstance").invoke("getTypeByClass").arg(jCodeModel.ref(rangeClass).dotclass()).invoke("parse").arg(rangeValueParam)));
					methodBody.add(kbSetVar.invoke("add").arg(value));
				}
				// Add set to the individual
				JVar entityVar = methodBody.decl(ownerInterface.asJDefinedClass(), "_entity", ownerInterface.asJDefinedClass().staticInvoke("get").arg(idParam));
				methodBody.add(entityVar.invoke(methodName).arg(kbSetVar));

				// Respond OK
				JVar responseBuilderVar = methodBody.decl(super.jCodeModel._ref(ResponseBuilder.class), "_responseBuilder", super.jCodeModel.ref(Response.class).staticInvoke("ok"));
				methodBody._return(responseBuilderVar.invoke("build"));
			}
		}

	}

	public void createSetMethodForObjectProperty() {
		JType responseType = super.jCodeModel.ref(Response.class);
		String methodName = "set" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);

		JMethod tempSet = ((JDefinedClass) owner.asJDefinedClass()).getMethod(methodName, new JType[] { super.jCodeModel._ref(String.class), super.jCodeModel._ref(String.class) });

		if (tempSet == null) {
			jMethod = ((JDefinedClass) owner.asJDefinedClass()).method(JMod.PUBLIC, responseType, methodName);
			if (owner instanceof OntologyCodeClass) {

				// Create annotation SET method
				jMethod.annotate(POST.class);
				jMethod.annotate(Path.class).param("value", "/entity/set" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1));
				String operationId = "set_" + ((RestOntologyCodeClass) owner).getPath().substring(1) + "_" + entityName;
				jMethod.annotate(ApiOperation.class).param("value", "Set " + entityName).param("nickname", operationId);

				// Create SET parameter
				// IRI of the target individual where the property will be added
				JVar idParam = jMethod.param(String.class, "id");
				idParam.annotate(ApiParam.class).param("value", "id").param("required", true);
				idParam.annotate(QueryParam.class).param("value", "id");

				JVar iriRangeParam = jMethod.param(String.class, "value");
				iriRangeParam.annotate(ApiParam.class).param("value", "value").param("required", true);
				iriRangeParam.annotate(QueryParam.class).param("value", "value");

				/*
				 * Method Body
				 */
				JBlock methodBody = jMethod.body();

				// Getting the target individual where the property will be added
				AbstractOntologyCodeClass ownerInterface = ontologyModel.getOntologyClass(owner.getOntResource(), BeanOntologyCodeInterface.class);
				AbstractOntologyCodeClass rangeJenaClass = null;
				AbstractOntologyCodeClass rangeJenaInterface = null;
				if (range == null) {
					rangeJenaClass = ontologyModel.getOntologyClass(ModelFactory.createOntologyModel().getOntResource(OWL.Thing), JenaOntologyCodeClass.class);
					rangeJenaInterface = ontologyModel.getOntologyClass(ModelFactory.createOntologyModel().getOntResource(OWL.Thing), BeanOntologyCodeInterface.class);
				} else {
					rangeJenaClass = ontologyModel.getOntologyClass(range.getOntResource(), JenaOntologyCodeClass.class);
					rangeJenaInterface = ontologyModel.getOntologyClass(range.getOntResource(), BeanOntologyCodeInterface.class);
					if (rangeJenaClass == null) {
						// The range is a boolean class
						rangeJenaClass = ontologyModel.getOntologyClass(range.getOntResource(), BooleanAnonClass.class);
						rangeJenaInterface = ontologyModel.getOntologyClass(range.getOntResource(), BooleanAnonClass.class);
					}
				}

				// Creting set to be added
				JType hashSetType_range = super.jCodeModel.ref(HashSet.class).narrow(rangeJenaInterface.asJDefinedClass());
				JType setType_range = super.jCodeModel.ref(Set.class).narrow(rangeJenaInterface.asJDefinedClass());
				JVar kbSetVar = methodBody.decl(setType_range, "toAdd", JExpr._new(hashSetType_range));
				methodBody.add(kbSetVar.invoke("add").arg(JExpr._new(rangeJenaClass.asJDefinedClass()).arg(jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createResource").arg(iriRangeParam))));

				// add set to the individual
				JVar entityVar = methodBody.decl(ownerInterface.asJDefinedClass(), "_entity", ownerInterface.asJDefinedClass().staticInvoke("get").arg(idParam));
				methodBody.add(entityVar.invoke(methodName).arg(kbSetVar));

				// Respond OK
				JVar responseBuilderVar = methodBody.decl(super.jCodeModel._ref(ResponseBuilder.class), "_responseBuilder", super.jCodeModel.ref(Response.class).staticInvoke("ok"));
				methodBody._return(responseBuilderVar.invoke("build"));
			}
		}

	}

	public void createSetMethodForDatatypeProperty() {
		JType responseType = super.jCodeModel.ref(Response.class);
		String methodName = "set" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);

		JMethod tempSet = ((JDefinedClass) owner.asJDefinedClass()).getMethod(methodName, new JType[] { super.jCodeModel._ref(String.class), super.jCodeModel._ref(String.class) });

		if (tempSet == null) {
			jMethod = ((JDefinedClass) owner.asJDefinedClass()).method(JMod.PUBLIC, responseType, methodName);
			if (owner instanceof OntologyCodeClass) {

				// Create annotation SET method
				jMethod.annotate(POST.class);
				jMethod.annotate(Path.class).param("value", "/entity/set" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1));
				String operationId = "set_" + ((RestOntologyCodeClass) owner).getPath().substring(1) + "_" + entityName;
				jMethod.annotate(ApiOperation.class).param("value", "Set " + entityName).param("nickname", operationId);

				// Create SET parameter
				// IRI of the target individual where the property will be added
				JVar idParam = jMethod.param(String.class, "id");
				idParam.annotate(ApiParam.class).param("value", "id").param("required", true);
				idParam.annotate(QueryParam.class).param("value", "id");

				JVar rangeValueParam = jMethod.param(String.class, "value");
				rangeValueParam.annotate(ApiParam.class).param("value", "value").param("required", true);
				rangeValueParam.annotate(QueryParam.class).param("value", "value");

				/*
				 * Method Body
				 */
				JBlock methodBody = jMethod.body();

				// Getting the target individual where the property will be added
				AbstractOntologyCodeClass ownerInterface = ontologyModel.getOntologyClass(owner.getOntResource(), BeanOntologyCodeInterface.class);

				// Getting range class
				Class<?> rangeClass = null;

				logger.debug("OWNER " + owner.getEntityName() + " " + this.ontResource.getLocalName());

				if (range == null) {
					rangeClass = String.class;
				} else {
					logger.debug(range.getOntResource().getLocalName());
					rangeClass = TypeMapper.getInstance().getTypeByName(range.getOntResource().getURI()).getJavaClass();
				}

				// Creting set to be added
				JType hashSetType_range = super.jCodeModel.ref(HashSet.class).narrow(rangeClass);
				JType setType_range = super.jCodeModel.ref(Set.class).narrow(rangeClass);
				JVar kbSetVar = methodBody.decl(setType_range, "toAdd", JExpr._new(hashSetType_range));

				// Adding value to the set that will be added
				if (range == null || rangeClass.equals(String.class)) {
					methodBody.add(kbSetVar.invoke("add").arg(rangeValueParam));
				} else {
					JVar value = methodBody.decl(jCodeModel.ref(rangeClass), "datatype", JExpr.cast(jCodeModel.ref(rangeClass), jCodeModel.ref(TypeMapper.class).staticInvoke("getInstance").invoke("getTypeByClass").arg(jCodeModel.ref(rangeClass).dotclass()).invoke("parse").arg(rangeValueParam)));
					methodBody.add(kbSetVar.invoke("add").arg(value));
				}
				// Add set to the individual
				JVar entityVar = methodBody.decl(ownerInterface.asJDefinedClass(), "_entity", ownerInterface.asJDefinedClass().staticInvoke("get").arg(idParam));
				methodBody.add(entityVar.invoke(methodName).arg(kbSetVar));

				// Respond OK
				JVar responseBuilderVar = methodBody.decl(super.jCodeModel._ref(ResponseBuilder.class), "_responseBuilder", super.jCodeModel.ref(Response.class).staticInvoke("ok"));
				methodBody._return(responseBuilderVar.invoke("build"));
			}
		}

	}

	@Override
	public int hashCode() {
		return methodType.hashCode() + super.hashCode();
	}

}
