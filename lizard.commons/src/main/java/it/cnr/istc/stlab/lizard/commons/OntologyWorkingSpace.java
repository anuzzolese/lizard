package it.cnr.istc.stlab.lizard.commons;

import java.util.HashSet;
import java.util.Set;

public class OntologyWorkingSpace {

	private Set<OntologyCodeProject> ontologyCodeProjects = new HashSet<>();

	public void addOntologyCodeProject(OntologyCodeProject ocp) {
		ontologyCodeProjects.add(ocp);
	}

	public OntologyCodeProject getOntologyCodeProject(String uri) {
		for (OntologyCodeProject ocp : ontologyCodeProjects) {
			if (uri.equals(ocp.getOntologyCodeModel().asOntModel().getNsPrefixURI(""))) {
				return ocp;
			}
		}
		return null;
	}

}
