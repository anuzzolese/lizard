package it.cnr.istc.stlab.lizard.core.anonymous;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.annotations.ComplementOf;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;

public class ComplementClass extends BooleanAnonClass {

	ComplementClass(String id, OntResource ontClass, JCodeModel codeModel, AbstractOntologyCodeClass... members) {
		super(Constants.COMPLEMENT_CLASS_SUFFIX, ComplementOf.class, id, ontClass, codeModel, members);
	}

	public void addMember(AbstractOntologyCodeClass ontologyCodeClass) {
		if (annotationArray == null) {
			JAnnotationUse annotation = ((JDefinedClass) super.jClass).annotate(ComplementOf.class);
			annotationArray = annotation.paramArray("classes");
		}

		annotationArray.param(ontologyCodeClass.asJDefinedClass());
	}

}
