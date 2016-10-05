package it.cnr.istc.stlab.lizard.core.anonymous;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.annotations.IntersectionOf;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;

public class IntersectionClass extends BooleanAnonClass {
    
    IntersectionClass(String id, OntResource ontClass, JCodeModel codeModel, AbstractOntologyCodeClass...members) {
    	super(Constants.INTERSECTION_CLASS_SUFFIX, IntersectionOf.class, id, ontClass, codeModel, members);
    }
    
    protected void addMember(AbstractOntologyCodeClass ontologyCodeClass){
    	if(annotationArray == null) {
    		JAnnotationUse annotation = ((JDefinedClass)super.jClass).annotate(IntersectionOf.class);
    		annotationArray = annotation.paramArray("classes");
    	}
    	
    	annotationArray.param(ontologyCodeClass.asJDefinedClass());
    }

    

    

}
