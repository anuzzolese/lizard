package it.cnr.istc.stlab.lizard.commons.recipe;

import it.cnr.istc.stlab.lizard.commons.OntologyCodeProject;

public interface OntologyCodeGenerationRecipe {

	OntologyCodeProject generate();

	public void generateSwaggerDescription(String swagger);

}
