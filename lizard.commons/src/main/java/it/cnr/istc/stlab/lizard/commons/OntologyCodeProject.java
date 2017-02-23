package it.cnr.istc.stlab.lizard.commons;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;

public class OntologyCodeProject {

	private OntologyCodeModel ontologyCodeModel;
	private String groupId;
	private String artifactId;
	private String srcFolder;
	private String testFolder;
	private String mainFolder;
	private OntologyWorkingSpace ontologyWorkingSpace;
	private Set<OntologyCodeProject> importedProjects = new HashSet<OntologyCodeProject>();

	public OntologyCodeProject(URI ontologyURI, OntologyCodeModel ontologyCodeModel, OntologyWorkingSpace ows) {
		this.ontologyCodeModel = ontologyCodeModel;
		this.ontologyWorkingSpace = ows;
		ontologyCodeModel.setOntologyCodeProject(this);
		groupId = PackageResolver.resolveGroupId(ontologyURI);
		artifactId = PackageResolver.resolveArtifactId(ontologyURI);
		this.setMainFolder(this.srcFolder + "/main");
		this.setTestFolder(this.srcFolder + "/test");
	}

	public OntologyCodeModel getOntologyCodeModel() {
		return ontologyCodeModel;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void write(String destinationFolder) throws IOException {
	}

	public String getTestFolder() {
		return testFolder;
	}

	public void setTestFolder(String testFolder) {
		this.testFolder = testFolder;
	}

	public String getMainFolder() {
		return mainFolder;
	}

	public void setMainFolder(String mainFolder) {
		this.mainFolder = mainFolder;
	}

	public void importProject(OntologyCodeProject otherProject) {
		importedProjects.add(otherProject);
	}
	
	public Set<OntologyCodeProject> getImportedProjects(){
		return importedProjects;
	}

	public OntologyWorkingSpace getOntologyWorkingSpace() {
		return ontologyWorkingSpace;
	}

	public void setOntologyWorkingSpace(OntologyWorkingSpace ontologyWorkingSpace) {
		this.ontologyWorkingSpace = ontologyWorkingSpace;
	}
}
