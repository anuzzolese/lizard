package it.cnr.istc.stlab.lizard.commons.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import it.cnr.istc.stlab.lizard.commons.LizardInterface;

@Retention(RetentionPolicy.RUNTIME)
public @interface ComplementOf {

	public Class<? extends LizardInterface>[] classes();
}
