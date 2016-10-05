package it.cnr.istc.stlab.lizard.commons;

import java.io.IOException;
import java.net.URI;

import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;

public class OntologyCodeProject {

    private OntologyCodeModel ontologyCodeModel;
    private String groupId;
    private String artifactId;
    //private String projectFolder;
    private String srcFolder;
    private String testFolder;
    private String mainFolder;
    
    //public OntologyCodeProject(URI ontologyURI, String projectFolder, OntologyCodeModel ontologyCodeModel) {
    public OntologyCodeProject(URI ontologyURI, OntologyCodeModel ontologyCodeModel) {
        this.ontologyCodeModel = ontologyCodeModel;
        groupId = PackageResolver.resolveGroupId(ontologyURI);
        artifactId = PackageResolver.resolveArtifactId(ontologyURI);
        //this.projectFolder = projectFolder;
        //this.srcFolder = this.projectFolder + "/src";
        this.mainFolder = this.srcFolder + "/main";
        this.testFolder = this.srcFolder + "/test";
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
    
    public void write(String destinationFolder) throws IOException{}
}
