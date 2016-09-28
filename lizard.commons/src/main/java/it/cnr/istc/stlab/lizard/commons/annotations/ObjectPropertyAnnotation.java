package it.cnr.istc.stlab.lizard.commons.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectPropertyAnnotation {

	public String uri();
	public OntologyCodeMethodType method();
}
