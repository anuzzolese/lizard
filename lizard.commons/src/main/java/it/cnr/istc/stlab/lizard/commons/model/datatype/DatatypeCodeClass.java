package it.cnr.istc.stlab.lizard.commons.model.datatype;

import java.util.Collections;
import java.util.Set;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;


public class DatatypeCodeClass extends AbstractOntologyCodeClass {
	
	public DatatypeCodeClass() {
		super();
	}
	
	public DatatypeCodeClass(OntResource ontResource, OntologyCodeModel ontologyModel, JCodeModel jCodeModel) {
		super(ontResource, ontologyModel, jCodeModel);
		
		super.ontologyClassType = OntologyCodeClassType.Class;
		
		super.entityName = ontResource.getURI();
		
		RDFDatatype datatype = TypeMapper.getInstance().getTypeByName(entityName);
        
        super.jClass = (JDefinedClass) jCodeModel.ref(datatype.getJavaClass());
            
	}

	@Override
	protected void extendsClasses(AbstractOntologyCodeClass oClass) {
		
		
	}

	@Override
	public Set<AbstractOntologyCodeClass> listSuperClasses() {
		return Collections.emptySet();
	}

}
