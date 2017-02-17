package it.cnr.istc.stlab.lizard.commons.model.datatype;

import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;

import java.util.Collections;
import java.util.Set;

import javax.lang.model.SourceVersion;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;

public class DatatypeCodeInterface extends OntologyCodeInterface {

	public DatatypeCodeInterface(OntResource ontResource, OntologyCodeModel ontologyModel, JCodeModel jCodeModel) throws ClassAlreadyExistsException {
		super();

		super.ontResource = ontResource;
		super.ontologyModel = ontologyModel;
		super.jCodeModel = jCodeModel;

		String datatypeUri = ontResource.getURI();
		String localName = ontResource.getLocalName();
		if (!SourceVersion.isName(localName))
			localName = "_" + localName;
		super.entityName = localName;

		RDFDatatype datatype = TypeMapper.getInstance().getTypeByName(datatypeUri);
		if (datatype == null) {
			datatype = XSDDatatype.XSDstring;
		}

		super.jClass = jCodeModel.ref(datatype.getJavaClass());
	}

	@Override
	protected void extendsClasses(AbstractOntologyCodeClass oClass) {

	}

	@Override
	public Set<AbstractOntologyCodeClass> listSuperClasses() {
		return Collections.emptySet();
	}

}
