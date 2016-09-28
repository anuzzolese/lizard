package it.cnr.istc.stlab.lizard.commons.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JGenerifiable;
import com.sun.codemodel.JVar;

import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;

public abstract class AbstractOntologyCodeClass extends AbstractOntologyCodeEntity {
    
    protected OntologyCodeClassType ontologyClassType;
    protected Map<OntResource, Set<AbstractOntologyCodeMethod>> methodMap;
    protected AbstractOntologyCodeClass extendedClass;
    protected JDefinedClass jClass;

    AbstractOntologyCodeClass() {
    	super();
    	methodMap = new HashMap<OntResource,Set<AbstractOntologyCodeMethod>>();
	}
    
    AbstractOntologyCodeClass(OntResource ontResource, OntologyCodeModel ontologyModel, JCodeModel jCodeModel) {
        super(ontResource, ontologyModel, jCodeModel);
        methodMap = new HashMap<OntResource,Set<AbstractOntologyCodeMethod>>();
    }
    
    public Set<AbstractOntologyCodeMethod> getMethods(OntResource property){
        return methodMap.get(property);
    }
    
    abstract void extendsClasses(AbstractOntologyCodeClass oClass);
    
    public AbstractOntologyCodeClass getExtendedClass() {
        return extendedClass;
    }
    
    public void setExtendedClass(AbstractOntologyCodeClass extendedClass) {
        this.extendedClass = extendedClass;
    }
    
    public void addMethod(AbstractOntologyCodeMethod method){
        Set<AbstractOntologyCodeMethod> methodSet = methodMap.get(method.getOntResource());
        if(methodSet == null){
            methodSet = new HashSet<AbstractOntologyCodeMethod>();
            methodMap.put(method.getOntResource(), methodSet);
        }
        methodSet.add(method);
    }
    
    public Collection<AbstractOntologyCodeMethod> getMethods() {
        Collection<Set<AbstractOntologyCodeMethod>> methodSets = methodMap.values();
        
        Collection<AbstractOntologyCodeMethod> ontologyMethods = new ArrayList<AbstractOntologyCodeMethod>();
        for(Set<AbstractOntologyCodeMethod> methodSet : methodSets){
            ontologyMethods.addAll(methodSet);
        }
        
        
        return ontologyMethods;
    }
    
    public OntologyCodeClassType getOntologyClassType() {
        return ontologyClassType;
    }
    
    public JDefinedClass asJDefinedClass(){
        return jClass;
    }
    
    @Override
    public JGenerifiable getJCodeEntity() {
        return jClass;
    }
    
    public abstract Set<AbstractOntologyCodeClass> listSuperClasses();
    

}
