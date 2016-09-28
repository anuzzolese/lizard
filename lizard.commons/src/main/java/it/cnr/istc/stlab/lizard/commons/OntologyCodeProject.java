package it.cnr.istc.stlab.lizard.commons;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.writer.FileCodeWriter;

import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;

public class OntologyCodeProject {

    private OntologyCodeModel ontologyCodeModel;
    private String groupId;
    private String artifactId;
    //private String projectFolder;
    private String srcFolder;
    private String testFolder;
    private String mainFolder;
    private String mainJavaFolder;
    private String testJavaFolder;
    private String mainResourcesFolder;
    
    //public OntologyCodeProject(URI ontologyURI, String projectFolder, OntologyCodeModel ontologyCodeModel) {
    public OntologyCodeProject(URI ontologyURI, OntologyCodeModel ontologyCodeModel) {
        this.ontologyCodeModel = ontologyCodeModel;
        groupId = PackageResolver.resolveGroupId(ontologyURI);
        artifactId = PackageResolver.resolveArtifactId(ontologyURI);
        //this.projectFolder = projectFolder;
        //this.srcFolder = this.projectFolder + "/src";
        this.mainFolder = this.srcFolder + "/main";
        this.testFolder = this.srcFolder + "/test";
        this.mainJavaFolder = this.mainFolder + "/java";
        this.mainResourcesFolder = this.mainFolder + "/resources";
        this.testJavaFolder = this.testFolder + "/java";
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
    
    public void write(String destinationFolder) throws IOException{
    	File testFolder = new File(destinationFolder);
    	if(testFolder.exists()) {
    		System.out.println("esists " + testFolder.getClass());
    		FileUtils.deleteDirectory(testFolder);
    	}
    	else System.out.println("not esists");
        File src = new File(mainJavaFolder);
        File resources = new File(mainResourcesFolder);
        File test = new File(testJavaFolder);
        if(!src.exists()) src.mkdirs();
        if(!resources.exists()) resources.mkdirs();
        if(!test.exists()) test.mkdirs();
        
        CodeWriter writer = new FileCodeWriter(src, "UTF-8");
        ontologyCodeModel.asJCodeModel().build(writer);
        
        /*
         * Add service declarations inside META-INF/services
         */
        
        String servicesFolderPath = mainResourcesFolder + "/META-INF/services";
        File servicesFolder = new File(servicesFolderPath);
        if(!servicesFolder.exists()) servicesFolder.mkdirs();
        
        Map<OntResource, OntologyCodeClass> classMap = ontologyCodeModel.getClassMap();
        Map<OntResource, OntologyCodeInterface> interfaceMap = ontologyCodeModel.getInterfaceMap();
        
        for(OntResource key : classMap.keySet()){
        	OntologyCodeInterface codeInterface = interfaceMap.get(key);
        	OntologyCodeClass codeClass = classMap.get(key);
        	
        	if(codeClass != null && codeInterface != null){
        		File serviceFile = new File(servicesFolder + "/" + codeInterface.asJDefinedClass().fullName());
        		Writer serviceFileWriter = new FileWriter(serviceFile);
        		serviceFileWriter.write(codeClass.asJDefinedClass().fullName());
        		serviceFileWriter.flush();
        		serviceFileWriter.close();
        	}
        }
        
        /*
         * Generate the POM descriptor file and build the project
         * as a Maven project.
         */
        File pom = new File("test_out/pom.xml");
        Writer pomWriter = new FileWriter(new File("test_out/pom.xml"));
        Map<String,String> dataModel = new HashMap<String,String>();
        dataModel.put("artifactId", artifactId);
        dataModel.put("groupId", groupId);
        MavenUtils.generatePOM(pomWriter, dataModel);
        MavenUtils.buildProject(pom);
    }
}
