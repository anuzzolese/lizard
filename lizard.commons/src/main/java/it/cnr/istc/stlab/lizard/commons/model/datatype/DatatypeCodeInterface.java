package it.cnr.istc.stlab.lizard.commons.model.datatype;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.lang.model.SourceVersion;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;

import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;

public class DatatypeCodeInterface extends OntologyCodeInterface {

	private static Logger logger = LoggerFactory.getLogger(DatatypeCodeInterface.class);

	public DatatypeCodeInterface(OntResource ontResource, OntologyCodeModel ontologyModel, JCodeModel jCodeModel)
			throws ClassAlreadyExistsException {
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
		if (datatype == null || datatype.getJavaClass() == null) {
			logger.trace("Assigning default datatype xsd:string");
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

	private static boolean hasTypeMapper(String uri) {
		Iterator<RDFDatatype> it = TypeMapper.getInstance().listTypes();
		while (it.hasNext()) {
			RDFDatatype rdfDatatype = (RDFDatatype) it.next();
			if (rdfDatatype.getURI().equals(uri) && rdfDatatype.getJavaClass() != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public JClass asJDefinedClass() {
		if (hasTypeMapper(ontResource.getURI())) {
			return jCodeModel.ref(TypeMapper.getInstance().getSafeTypeByName(ontResource.getURI()).getJavaClass());
		}
		return super.asJDefinedClass();
	}

}
