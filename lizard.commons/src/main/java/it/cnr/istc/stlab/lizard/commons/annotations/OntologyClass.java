package it.cnr.istc.stlab.lizard.commons.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OntologyClass {
	public String uri();
}
