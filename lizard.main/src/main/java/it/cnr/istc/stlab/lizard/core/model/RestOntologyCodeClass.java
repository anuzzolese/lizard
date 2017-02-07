package it.cnr.istc.stlab.lizard.core.model;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.PrefixRegistry;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.inmemory.RestInterface;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;

import java.util.Set;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

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

		// String packagePath = packageName.replaceAll("\\.", "_");

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
		} catch (JClassAlreadyExistsException e) {
			super.jClass = codeModel._getClass(entityName);
		}

		addCreateMethod();
	}

	public String getPath() {
		return path;
	}

	public void addCreateMethod() {
		String localName = this.ontResource.getLocalName().substring(0, 1).toUpperCase() + this.ontResource.getLocalName().substring(1);
		JType responseType = super.jCodeModel.ref(Response.class);
		String methodName = "create" + Constants.getJavaName(localName);

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
			
			logger.debug(entityName+" "+path+" "+methodName+" "+this.ontResource.getLocalName());

			JBlock methodBody = jMethod.body();

			// Getting the bean class of the individual
			AbstractOntologyCodeClass jenaClass = ontologyModel.getOntologyClass(this.getOntResource(), JenaOntologyCodeClass.class);
			logger.debug("JENA CLASS "+ ((jenaClass==null)?"null":"not null") + " "+this.getOntResource().getURI());
			methodBody.add(JExpr._new(jenaClass.asJDefinedClass()).arg(jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createResource").arg(idParam)));

			// Respond OK
			JVar responseBuilderVar = methodBody.decl(super.jCodeModel._ref(ResponseBuilder.class), "_responseBuilder", super.jCodeModel.ref(Response.class).staticInvoke("ok"));
			methodBody._return(responseBuilderVar.invoke("build"));

		}
	}

}
