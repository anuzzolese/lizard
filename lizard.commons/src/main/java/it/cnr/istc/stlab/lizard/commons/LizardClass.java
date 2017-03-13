package it.cnr.istc.stlab.lizard.commons;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "individual", "classResource","propertyMap","extentionalClasses" })
public class LizardClass implements LizardInterface {
	@JsonIgnore
	protected RDFNode individual;
	@JsonIgnore
	protected OntResource classResource;
	@JsonIgnore
	protected PropertyMap propertyMap;
	@JsonIgnore
	protected Set<ExtentionalLizardClassImpl<LizardInterface>> extentionalClasses;

	@JsonIgnore
	public LizardClass() {
		propertyMap = new PropertyMap();
		extentionalClasses = new HashSet<ExtentionalLizardClassImpl<LizardInterface>>();
	}

	@JsonIgnore
	protected LizardClass(RDFNode individual, OntResource classResource) {
		this();
		this.individual = individual;
		this.classResource = classResource;
	}

	@JsonIgnore
	protected LizardClass(RDFNode individual, OntResource classResource,
			PropertyMap propertyMap) {
		this();
		this.individual = individual;
		this.classResource = classResource;
		this.propertyMap = propertyMap;
	}

	@JsonIgnore
	public OntResource getClassResource() {
		return classResource;
	}

	@JsonIgnore
	public RDFNode getIndividual() {
		return individual;
	}

	@JsonIgnore
	public void setClassResource(OntResource classResource) {
		this.classResource = classResource;
	}

	@JsonIgnore
	public void setIndividual(RDFNode individual) {
		this.individual = individual;
	}

	@JsonIgnore
	public PropertyMap getPropertyMap() {
		return propertyMap;
	}

	@JsonIgnore
	public void setPropertyMap(PropertyMap propertyMap) {
		this.propertyMap = propertyMap;
	}

	@JsonIgnore
	public Object getPropertyValue(OntResource ontResource,
			Class<? extends Object> objectClass) {
		return propertyMap.get(ontResource, objectClass);
	}

	@JsonIgnore
	public void setPropertyValue(OntResource ontResource, Object object) {
		propertyMap.put(ontResource, object);
	}
	
//	public static void prova(){
//		
//	}


}
