package it.cnr.istc.stlab.lizard.core.model;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.PrefixRegistry;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.inmemory.RestInterface;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;

public class RestOntologyCodeClass extends OntologyCodeClass {

	private static Logger logger = LoggerFactory.getLogger(RestOntologyCodeClass.class);

	private static final String SUBPACKAGE_NAME = "web";

	protected Set<OntClass> superClasses;
	private String path;

	protected RestOntologyCodeClass() {
		super();
	}

	RestOntologyCodeClass(OntResource resource, RestOntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
		super(resource, ontologyModel, codeModel);

		String artifactId = packageName + "." + SUBPACKAGE_NAME + ".";
		logger.debug(artifactId);

		String localName = Constants.getJavaName(resource.getLocalName());

		super.entityName = artifactId + localName;

		StringBuilder sb = new StringBuilder();
		char[] chars = localName.toCharArray();
		boolean start = true;
		for (char c : chars) {
			if (Character.isUpperCase(c) && !start)
				sb.append("_");
			sb.append(Character.toLowerCase(c));
			if (start)
				start = !start;
		}

		String namespace = ontResource.getNameSpace();
		String prefix = ontologyModel.asOntModel().getNsURIPrefix(namespace);
		if (prefix == null)
			prefix = PrefixRegistry.getInstance().getNsPrefix(namespace);
		if (prefix == null)
			prefix = PrefixRegistry.getInstance().createNsPrefix(namespace);

		if (prefix.isEmpty())
			this.path = "/" + sb.toString();
		else
			this.path = "/" + prefix + "_" + sb.toString();

		try {
			super.jClass = codeModel._class(entityName);
			((JDefinedClass) super.jClass)._implements(RestInterface.class);
			((JDefinedClass) super.jClass).constructor(JMod.PUBLIC);
			((JDefinedClass) super.jClass).annotate(Path.class).param("value", path);
			((JDefinedClass) super.jClass).annotate(Api.class).param("value", path);
			((JDefinedClass) super.jClass).annotate(Produces.class).param("value", jCodeModel.ref(MediaType.class).staticRef("APPLICATION_JSON"));

			// OSGi Annotation
			// ((JDefinedClass) super.jClass).annotate(Component.class).param("service", jCodeModel.ref(Object.class)).paramArray("property").param("javax.ws.rs=true");
		} catch (JClassAlreadyExistsException e) {
			super.jClass = codeModel._getClass(entityName);
		}

		addCreateMethod();
		addGetAllMethod();
		addGetByIdMethod();
	}

	private void addGetByIdMethod() {

		// Method declaration
		String localName = this.ontResource.getLocalName().substring(0, 1).toUpperCase() + this.ontResource.getLocalName().substring(1);
		JType responseType = super.jCodeModel.ref(Response.class);
		// String methodName = "get" + Constants.getJavaName(localName) + "ById";
		String methodName = "getById";

		JMethod tempSet = ((JDefinedClass) this.asJDefinedClass()).getMethod(methodName, new JType[] { jCodeModel.ref(String.class) });

		if (tempSet == null) {

			JMethod jMethod = ((JDefinedClass) this.asJDefinedClass()).method(JMod.PUBLIC, responseType, methodName);
			OntologyCodeInterface javaInterface = ontologyModel.getOntologyClass(ontResource, BeanOntologyCodeInterface.class);

			/*
			 * Method Body
			 */

			logger.debug(entityName + " " + path + " " + methodName + " " + this.ontResource.getLocalName());

			JBlock methodBody = jMethod.body();

			jMethod.annotate(GET.class);
			jMethod.annotate(Path.class).param("value", "/" + methodName);
			jMethod.annotate(ApiOperation.class).param("value", "Retrieve a " + Constants.getJavaName(localName) + " by id").param("nickname", "get" + Constants.getJavaName(localName) + "ById").param("response", javaInterface.asJDefinedClass().dotclass()).param("responseContainer", "List");

			// Create parameters
			JVar idParam = jMethod.param(String.class, "id");
			idParam.annotate(ApiParam.class).param("value", "id").param("required", true);
			idParam.annotate(QueryParam.class).param("value", "id");

			JVar entityVar = methodBody.decl(javaInterface.asJDefinedClass(), "entity", javaInterface.asJDefinedClass().staticInvoke("get").arg(idParam));

			JVar responseBuilderVar = methodBody.decl(jCodeModel._ref(ResponseBuilder.class), "_responseBuilder", JExpr._null());

			JConditional ifBlock = methodBody._if(entityVar.ne(JExpr._null()));
			// then
			JBlock then = ifBlock._then();
			// create set response
			JenaOntologyCodeClass jenaClass = ontologyModel.getOntologyClass(ontResource, JenaOntologyCodeClass.class);
			JExpression cast = JExpr.cast(jenaClass.asJDefinedClass(), entityVar);
			JType hashSetType_range_res = super.jCodeModel.ref(HashSet.class).narrow(javaInterface.asJDefinedClass());
			JType setType_range_res = super.jCodeModel.ref(Set.class).narrow(javaInterface.asJDefinedClass());
			JVar kbSetVar_res = then.decl(setType_range_res, "response", JExpr._new(hashSetType_range_res));
			then.add(kbSetVar_res.invoke("add").arg(cast.invoke("asMicroBean")));

			JExpression toList = kbSetVar_res.invoke("toArray").arg(JExpr.newArray(javaInterface.asJDefinedClass(), kbSetVar_res.invoke("size")));

			then.assign(responseBuilderVar, super.jCodeModel.ref(Response.class).staticInvoke("ok").arg(toList));

			// else
			JBlock entityIfElseBlock = ifBlock._else();
			entityIfElseBlock.assign(responseBuilderVar, jCodeModel.ref(Response.class).staticInvoke("status").arg(jCodeModel.ref(Status.class).staticRef("NOT_FOUND")));

			methodBody._return(responseBuilderVar.invoke("build"));

		}

	}

	private void addGetAllMethod() {

		String localName = this.ontResource.getLocalName().substring(0, 1).toUpperCase() + this.ontResource.getLocalName().substring(1);
		JType responseType = super.jCodeModel.ref(Response.class);
		// String methodName = "getAll" + Constants.getJavaName(localName);
		String methodName = "getAll";

		JMethod tempSet = ((JDefinedClass) this.asJDefinedClass()).getMethod(methodName, new JType[] {});

		if (tempSet == null) {
			OntologyCodeInterface javaInterface = ontologyModel.getOntologyClass(ontResource, BeanOntologyCodeInterface.class);
			JMethod jMethod = ((JDefinedClass) this.asJDefinedClass()).method(JMod.PUBLIC, responseType, methodName);

			/*
			 * Method Body
			 */

			logger.debug(entityName + " " + path + " " + methodName + " " + this.ontResource.getLocalName());

			JBlock methodBody = jMethod.body();

			jMethod.annotate(GET.class);
			jMethod.annotate(Path.class).param("value", "/" + methodName);
			jMethod.annotate(ApiOperation.class).param("value", "Retrieve all " + Constants.getJavaName(localName)).param("nickname", "getAll" + Constants.getJavaName(localName)).param("response", javaInterface.asJDefinedClass().dotclass()).param("responseContainer", "List");

			JVar responseBuilderVar = methodBody.decl(jCodeModel._ref(ResponseBuilder.class), "_responseBuilder", JExpr._null());
			JType setType = jCodeModel.ref(Set.class).narrow(javaInterface.asJDefinedClass());
			JType hashSetType = jCodeModel.ref(HashSet.class).narrow(javaInterface.asJDefinedClass());

			JVar kbSetVar = methodBody.decl(setType, "_kbSet", javaInterface.asJDefinedClass().staticInvoke("getAll"));
			JVar retSetVar = methodBody.decl(setType, "_retSet", JExpr._new(hashSetType));

			JConditional ifBlock = methodBody._if(kbSetVar.ne(JExpr._null()));
			/*
			 * Then
			 */
			JBlock ifThenBlock = ifBlock._then();
			JForEach forEach = ifThenBlock.forEach(javaInterface.asJDefinedClass(), "_obj", kbSetVar);

			JBlock forEachBlock = forEach.body();
			JExpression castExpression = JExpr.cast(ontologyModel.getOntologyClass(ontResource, JenaOntologyCodeClass.class).asJDefinedClass(), forEach.var());
			forEachBlock.add(retSetVar.invoke("add").arg(castExpression.invoke("asMicroBean")));

			JExpression toList = retSetVar.invoke("toArray").arg(JExpr.newArray(javaInterface.asJDefinedClass(), retSetVar.invoke("size")));

			ifThenBlock.assign(responseBuilderVar, jCodeModel.ref(Response.class).staticInvoke("ok").arg(toList));

			/*
			 * Else
			 */
			JBlock ifElseBlock = ifBlock._else();
			ifElseBlock.assign(responseBuilderVar, jCodeModel.ref(Response.class).staticInvoke("status").arg(jCodeModel.ref(Status.class).staticRef("NOT_FOUND")));

			methodBody._return(responseBuilderVar.invoke("build"));

		}

	}

	public String getPath() {
		return path;
	}

	public void addCreateMethod() {

		String localName = this.ontResource.getLocalName().substring(0, 1).toUpperCase() + this.ontResource.getLocalName().substring(1);
		JType responseType = super.jCodeModel.ref(Response.class);
		// String methodName = "create" + Constants.getJavaName(localName);
		String methodName = "create";

		JMethod tempSet = ((JDefinedClass) this.asJDefinedClass()).getMethod(methodName, new JType[] { super.jCodeModel._ref(String.class) });

		if (tempSet == null) {
			JMethod jMethod = ((JDefinedClass) this.asJDefinedClass()).method(JMod.PUBLIC, responseType, methodName);

			// Create annotations
			jMethod.annotate(POST.class);
			jMethod.annotate(Path.class).param("value", "/" + methodName);
			String operationId = "create" + localName;
			jMethod.annotate(ApiOperation.class).param("value", "Create a new " + this.ontResource.getLocalName()).param("nickname", operationId);

			// Create parameters
			JVar idParam = jMethod.param(String.class, "id");
			idParam.annotate(ApiParam.class).param("value", "id").param("required", true);
			idParam.annotate(QueryParam.class).param("value", "id");

			/*
			 * Method Body
			 */

			logger.debug(entityName + " " + path + " " + methodName + " " + this.ontResource.getLocalName());

			JBlock methodBody = jMethod.body();

			// Getting the bean class of the individual
			AbstractOntologyCodeClass jenaClass = ontologyModel.getOntologyClass(this.getOntResource(), JenaOntologyCodeClass.class);
			logger.debug("JENA CLASS " + ((jenaClass == null) ? "null" : "not null") + " " + this.getOntResource().getURI());
			methodBody.add(JExpr._new(jenaClass.asJDefinedClass()).arg(jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createResource").arg(idParam)));

			// Respond OK
			JVar responseBuilderVar = methodBody.decl(super.jCodeModel._ref(ResponseBuilder.class), "_responseBuilder", super.jCodeModel.ref(Response.class).staticInvoke("ok"));
			methodBody._return(responseBuilderVar.invoke("build"));

		}
	}

}
