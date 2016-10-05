package it.cnr.istc.stlab.lizard.commons.model;

import java.util.Collection;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JMethod;

import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

public abstract class AbstractOntologyCodeMethod extends AbstractOntologyCodeEntity {

    protected AbstractOntologyCodeClass owner;
    protected Collection<AbstractOntologyCodeClass> domain;
    protected AbstractOntologyCodeClass range;
    protected JMethod jMethod;
    protected OntologyCodeMethodType methodType;
    
    protected AbstractOntologyCodeMethod(){
    	super();
    }
    
    protected AbstractOntologyCodeMethod(OntologyCodeMethodType methodType, OntResource methodResource, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range, OntologyCodeModel ontologyModel, JCodeModel codeModel) {

        this(methodType, methodResource, null, domain, range, ontologyModel, codeModel);
        
    }
    
    protected AbstractOntologyCodeMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass owner, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range, OntologyCodeModel ontologyModel, JCodeModel codeModel) {

        super(methodResource, ontologyModel, codeModel);
        this.owner = owner;
        this.domain = domain;
        this.range = range;
        this.methodType = methodType;
        
        
    }
    
    public OntologyCodeMethodType getMethodType() {
        return methodType;
    }
    
    public JMethod asJMethod(){
        return jMethod;
    }
    
    public void setOwner(AbstractOntologyCodeClass owner){
    	this.owner = owner;
    }
    
    public AbstractOntologyCodeClass getOwner() {
        return owner;
    }
    
    public Collection<AbstractOntologyCodeClass> getDomain() {
        return domain;
    }
    
    public AbstractOntologyCodeClass getRange() {
        return range;
    }
    
    
    

}
