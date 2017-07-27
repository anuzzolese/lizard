package it.cnr.istc.stlab.lizard.core.anonymous;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.annotations.UnionOf;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;

public class UnionClass extends BooleanAnonClass {

	UnionClass(String id, OntResource ontClass, JCodeModel codeModel, AbstractOntologyCodeClass... members) {
		super(Constants.UNION_CLASS_SUFFIX, UnionOf.class, id, ontClass, codeModel, members);
	}

	protected void addMember(AbstractOntologyCodeClass ontologyCodeClass) {

		if (annotationArray == null) {
			JAnnotationUse annotation = ((JDefinedClass) super.jClass).annotate(UnionOf.class);
			annotationArray = annotation.paramArray("classes");
		}

		annotationArray.param(ontologyCodeClass.asJDefinedClass());

	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UnionClass) {
			return hashCode() == obj.hashCode() ? true : false;
		} else
			return false;
	}

}
