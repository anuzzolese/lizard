package it.cnr.istc.stlab.lizard.commons;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.XSDDuration;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

public class LizardClass implements LizardInterface {

    protected RDFNode individual;
    protected OntResource classResource;
    protected PropertyMap propertyMap;
    protected Set<ExtentionalLizardClassImpl<LizardInterface>> extentionalClasses;
    
    public LizardClass() {
    	propertyMap = new PropertyMap();
    	extentionalClasses = new HashSet<ExtentionalLizardClassImpl<LizardInterface>>();
    }
    
    protected LizardClass(RDFNode individual, OntResource classResource) {
    	this();
        this.individual = individual;
        this.classResource = classResource;
    }
    
    protected LizardClass(RDFNode individual, OntResource classResource, PropertyMap propertyMap) {
    	this();
        this.individual = individual;
        this.classResource = classResource;
        this.propertyMap = propertyMap;
    }
    
    public OntResource getClassResource() {
        return classResource;
    }
    
    public RDFNode getIndividual() {
        return individual;
    }
    
    public void setClassResource(OntResource classResource) {
        this.classResource = classResource;
    }
    
    public void setIndividual(RDFNode individual) {
        this.individual = individual;
    }
    
    public PropertyMap getPropertyMap() {
		return propertyMap;
	}
    
    public void setPropertyMap(PropertyMap propertyMap) {
		this.propertyMap = propertyMap;
	}
    
    public Object getPropertyValue(OntResource ontResource, Class<? extends Object> objectClass){
    	return propertyMap.get(ontResource, objectClass);
    }
    
    public void setPropertyValue(OntResource ontResource, Object object){
    	propertyMap.put(ontResource, object);
    }
     
    
}
