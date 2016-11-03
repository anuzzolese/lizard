package it.cnr.istc.stlab.lizard.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.ontology.OntResource;

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

public class RestOntologyCodeMethod extends OntologyCodeMethod {

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

						jMethod.annotate(GET.class);
						jMethod.annotate(Path.class).param("value", "/" + entityName);
						String operationId = ((RestOntologyCodeClass) owner).getPath().substring(1) + "_" + entityName;
						jMethod.annotate(ApiOperation.class).param("value", "Get by " + entityName).param("nickname", operationId);
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
						JMethod meth = jdc.getMethod(getMethodName, new JType[] {});

						JType methRetType = meth.type();

						JType methRetNarrowedType = methRetType.boxify().getTypeParameters().get(0);

						JType entitySetType = methRetType;

						entityBeanSetType = methRetType;
						entityBeanHashSetType = codeModel.ref(HashSet.class).narrow(methRetNarrowedType);

						/*
						 * if(beanClass != null &&
						 * methodResource.isObjectProperty()){
						 * if(methodResource.isDatatypeProperty()){
						 * entityBeanSetType =
						 * codeModel.ref(Set.class).narrow(beanClass
						 * .asJDefinedClass()); entityBeanHashSetType =
						 * codeModel
						 * .ref(HashSet.class).narrow(beanClass.asJDefinedClass
						 * ()); } else{ entityBeanSetType =
						 * codeModel.ref(Set.class
						 * ).narrow(beanClass.asJDefinedClass());
						 * entityBeanHashSetType =
						 * codeModel.ref(HashSet.class).narrow
						 * (beanClass.asJDefinedClass()); } } else{
						 * entityBeanSetType =
						 * codeModel.ref(Set.class).narrow(rangeJClass);
						 * entityBeanHashSetType =
						 * codeModel.ref(HashSet.class).narrow(rangeJClass); }
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
				/*
				 * methodName = "setBy" +
				 * entityName.substring(0,1).toUpperCase() +
				 * entityName.substring(1);
				 * 
				 * JMethod tempSet =
				 * ((JDefinedClass)owner.asJDefinedClass()).getMethod
				 * (methodName, new JType[]{});
				 * 
				 * if(temp != null){ jMethod =
				 * ((JDefinedClass)owner.asJDefinedClass()).method(JMod.PUBLIC,
				 * responseType, methodName); if(owner instanceof
				 * AbstractOntologyCodeClassImpl){
				 * 
				 * jMethod.annotate(POST.class);
				 * jMethod.annotate(Path.class).param("value", "/" +
				 * entityName); jMethod.param(String.class,
				 * "constraint").annotate(FormParam.class).param("value",
				 * entityName);
				 * 
				 * jMethod.body()._return(JExpr._null()); }
				 */
				break;

			default:
				break;
			}
		}
	}

	@Override
	public int hashCode() {
		return methodType.hashCode() + super.hashCode();
	}

}
