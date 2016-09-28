package it.cnr.istc.stlab.lizard.commons;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

public class ExtentionalLizardClassImpl<T extends LizardInterface> implements ExtentionalLizardClass<T> {

    protected Map<RDFNode, T> individuals;
    protected OntResource classResource;
    
    protected ExtentionalLizardClassImpl(OntResource classResource) {
    	this.classResource = classResource;
        individuals = new HashMap<RDFNode, T>();
    }
    
    public OntResource getClassResource() {
        return classResource;
    }
    
    public void setClassResource(OntResource classResource) {
        this.classResource = classResource;
    }
    
    public void addIndividual(T individual){
    	individuals.put(individual.getIndividual(), individual);
    }
    
    public T removeIndividual(T individual){
    	return (T) individuals.remove(individual.getIndividual());
    }
    
    public T getIndividual(String id){
    	return (T) individuals.get(ModelFactory.createDefaultModel().createResource(id));
    }
    
    public Set<T> getIndividuals(){
    	Set<T> inds = new HashSet<T>();
    	inds.addAll(individuals.values());
    	return inds;
    }
    
    public Set<T> getIndividualsByProperty(OntResource property){
    	Set<T> inds = new HashSet<T>();
    	for(T individual : individuals.values()){
    		if(individual.getPropertyMap().hasProperty(property)) inds.add(individual);
    	}
    	
    	return inds;
    }
    
}
