package it.cnr.istc.stlab.lizard.commons.model;

import it.cnr.istc.stlab.lizard.commons.PackageResolver;

import java.net.URISyntaxException;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;

public abstract class AbstractOntologyCodeEntity implements OntologyCodeEntity {

    protected JCodeModel jCodeModel;
    protected OntologyCodeModel ontologyModel;
    protected String packageName;
    protected OntResource ontResource;
    protected String entityName;
    
    protected AbstractOntologyCodeEntity(){
    	
    }
    
    protected AbstractOntologyCodeEntity(OntResource ontResource, OntologyCodeModel ontologyModel, JCodeModel jCodeModel) {
        this.ontResource = ontResource;
        this.ontologyModel = ontologyModel;
        this.jCodeModel = jCodeModel;
        try {
        	this.packageName = PackageResolver.resolve(ontResource);
        } catch (URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    
    @Override
    public String getPackageName() {
        return packageName;
    }
    
    @Override
    public JCodeModel getJCodeModel() {
        return jCodeModel;
    }
    
    @Override
    public OntResource getOntResource() {
        return ontResource;
    }
    
    @Override
    public String getEntityName() {
        return entityName;
    }
    
    @Override
    public OntologyCodeModel getOntologyModel() {
        return ontologyModel;
    }
    
    @Override
    public int hashCode() {
    	return ontResource.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof AbstractOntologyCodeEntity){
    		return ((AbstractOntologyCodeEntity) obj).getOntResource().equals(ontResource);
    	}
    	return false;
    }
    
    
}
