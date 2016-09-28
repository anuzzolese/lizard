package it.cnr.istc.stlab.lizard.rest.model;

import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.ws.rs.Path;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JMod;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.inmemory.InMemoryLizardClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;

public class RestOntologyCodeClass extends OntologyCodeClass {
	
	private static final String SUBPACKAGE_NAME = "web";
    
    protected Set<OntClass> superClasses;
    private String path;
    
    protected RestOntologyCodeClass(){
    	super();
    }
    
    RestOntologyCodeClass(OntResource resource, RestOntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
        super(resource, ontologyModel, codeModel);
        
        String artifactId = packageName + "." + SUBPACKAGE_NAME + ".";
        
        String localName = resource.getLocalName();
        if(!SourceVersion.isName(localName)) localName = "_" + localName;
        
        super.entityName = artifactId + localName;
        
        StringBuilder sb = new StringBuilder();
        char[] chars = localName.toCharArray();
        boolean start = true;
        for(char c : chars){
        	if(Character.isUpperCase(c) && !start) sb.append("_");
        	
        	sb.append(Character.toLowerCase(c));
        	
        	if(start) start = !start;
        }
        
        this.path = "/" + sb.toString();
        
        try {
			super.jClass = codeModel._class(entityName);
			super.jClass._extends(InMemoryLizardClass.class);
	        super.jClass.constructor(JMod.PUBLIC);
	        super.jClass.annotate(Path.class).param("value", path);
		} catch (JClassAlreadyExistsException e) {
			super.jClass = codeModel._getClass(entityName);
		}
        
    }

}
