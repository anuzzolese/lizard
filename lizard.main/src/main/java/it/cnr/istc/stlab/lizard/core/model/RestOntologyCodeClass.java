package it.cnr.istc.stlab.lizard.core.model;

import java.util.Set;

import javax.ws.rs.Path;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;

import io.swagger.annotations.Api;
import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.PrefixRegistry;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.inmemory.RestInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;

public class RestOntologyCodeClass extends OntologyCodeClass {

	private static final String SUBPACKAGE_NAME = "web";

	protected Set<OntClass> superClasses;
	private String path;

	protected RestOntologyCodeClass() {
		super();
	}

	RestOntologyCodeClass(OntResource resource, RestOntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
		super(resource, ontologyModel, codeModel);

		String artifactId = packageName + "." + SUBPACKAGE_NAME + ".";

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
	}
	
	public String getPath(){
		return path;
	}

}
