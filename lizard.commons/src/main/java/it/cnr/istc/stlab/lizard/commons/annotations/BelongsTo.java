package it.cnr.istc.stlab.lizard.commons.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import it.cnr.istc.stlab.lizard.commons.ExtentionalLizardClass;
import it.cnr.istc.stlab.lizard.commons.LizardInterface;

@Retention(RetentionPolicy.RUNTIME)
public @interface BelongsTo {

	public Class<? extends ExtentionalLizardClass<? extends LizardInterface>>[] extentionalClasses();
}
