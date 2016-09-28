package it.cnr.istc.stlab.lizard.jena.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.BooleanClassDescription;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;

import it.cnr.istc.stlab.lizard.anonymous.AnonymousClassBuilder;
import it.cnr.istc.stlab.lizard.commons.AnonClassType;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.exception.NotAvailableOntologyCodeEntityException;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClassImpl;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.ExtentionalOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;
import it.cnr.istc.stlab.lizard.inmemory.model.InMemoryOntologyCodeModel;

public class JenaOntologyCodeModel extends InMemoryOntologyCodeModel {
    
    public JenaOntologyCodeModel(OntModel ontModel) {
    	super(ontModel);
    }
    
    @Override
	public ExtentionalOntologyCodeClass createExtentionalOntologyClass(OntologyCodeInterface ontologyCodeInterface) throws NotAvailableOntologyCodeEntityException {
    	ExtentionalOntologyCodeClass extentionalOntologyCodeClass;
        try {
        	extentionalOntologyCodeClass = new JenaExtentionalOntologyCodeClass(ontologyCodeInterface);
            
        	extentionalClassMap.put(ontologyCodeInterface.getOntResource(), extentionalOntologyCodeClass);
            
        } catch (ClassAlreadyExistsException e) {
        	extentionalOntologyCodeClass = extentionalClassMap.get(ontologyCodeInterface.getOntResource());
        }
        return extentionalOntologyCodeClass;
    }
    
    protected OntologyCodeInterface createInterface(OntResource resource) {
        OntologyCodeInterface ontologyInterface;
        try {
            ontologyInterface = new JenaOntologyCodeInterface(resource, this, codeModel);
            interfaceMap.put(resource, ontologyInterface);
        } catch (ClassAlreadyExistsException e) {
        	ontologyInterface = interfaceMap.get(resource);
        }
        return ontologyInterface;
    }
    
    public OntologyCodeClass createClass(OntResource resource) {
        OntologyCodeClass ontologyClass;
        try {
            ontologyClass = new JenaOntologyCodeClass(resource, this, codeModel);
            
            classMap.put(resource, ontologyClass);
            
        } catch (ClassAlreadyExistsException e) {
            ontologyClass = classMap.get(resource);
        }
        return ontologyClass;
    }
    
    @Override
    public AbstractOntologyCodeMethod createMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass owner, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range) {
        AbstractOntologyCodeMethod ontologyMethod = null;
        
        if(owner != null){
	        Set<AbstractOntologyCodeMethod> methods = owner.getMethods(methodResource);
	        if(methods != null){
	            for(AbstractOntologyCodeMethod method : methods){
	                if(method.getMethodType() == methodType){
	                    ontologyMethod = method;
	                    break;
	                }
	            }
	        }
        }
        
        
        if(ontologyMethod == null){
        
        	ontologyMethod = new JenaOntologyCodeMethod(methodType, methodResource, owner, domain, range, this, codeModel);
            
            Set<AbstractOntologyCodeMethod> ontologyMethods = methodMap.get(methodResource);
            if(ontologyMethods == null){
                ontologyMethods = new HashSet<>();
                methodMap.put(methodResource, ontologyMethods);
            }
            ontologyMethods.add(ontologyMethod);
            owner.addMethod(ontologyMethod);
        }
        
        return ontologyMethod;
    }
    
    public AbstractOntologyCodeClassImpl createClassImplements(AbstractOntologyCodeClassImpl ontologyClass, OntologyCodeInterface...ontologyInterfaces) {
        
        for(OntologyCodeInterface ontologyInterface : ontologyInterfaces){
            
            if(ontologyInterface.getOntologyClassType() == OntologyCodeClassType.Interface){
                ontologyClass.asJDefinedClass()._implements(ontologyInterface.asJDefinedClass());
                
                ontologyClass.implementsInterfaces(ontologyInterface);
                
                Collection<AbstractOntologyCodeMethod> methods = ontologyInterface.getMethods();
                
                for(AbstractOntologyCodeMethod method : methods){
                	createMethod(method.getMethodType(), method.getOntResource(), ontologyClass, method.getDomain(), method.getRange());
                }
            }
        }
        return ontologyClass;
    }
    
    public BooleanAnonClass createAnonClass(OntClass ontClass){
    	
    	BooleanAnonClass anonClass = null;
    	BooleanClassDescription booleanClassDescription = null;
    	if(ontClass.isUnionClass()) {
        	booleanClassDescription = ontClass.asUnionClass();
        	
        }
    	else if(ontClass.isIntersectionClass()) {
    		booleanClassDescription = ontClass.asIntersectionClass();
    	}
        else if(ontClass.isComplementClass()) {
        	booleanClassDescription = ontClass.asComplementClass();
        }
    	
    	if(booleanClassDescription != null){
    		
    		ExtendedIterator<? extends OntClass> members = booleanClassDescription.listOperands();
    		
    		Set<AbstractOntologyCodeClass> memberClasses = new HashSet<AbstractOntologyCodeClass>();
    		while(members.hasNext()){
    			OntClass member = members.next();
    			
    			AbstractOntologyCodeClass memberClass = null;
    			
    			if(member.isURIResource()) {
    				memberClass = createInterface(member);
    			}
                else memberClass = createAnonClass(member);
    			
    			if(memberClass != null){
            		memberClasses.add(memberClass);
            	}
    		}
    		
    		if(!memberClasses.isEmpty()){
    			
    			AbstractOntologyCodeClass[] membs = new AbstractOntologyCodeClass[memberClasses.size()];
    			memberClasses.toArray(membs);
    			
    			if(ontClass.isUnionClass()) {
    	        	anonClass = createAnonClass(AnonClassType.Union, ontClass, membs);
    	        }
    	    	else if(ontClass.isIntersectionClass()) {
    	    		anonClass = createAnonClass(AnonClassType.Intersection, ontClass, membs);
    	    		booleanClassDescription = ontClass.asIntersectionClass();
    	    	}
    	        else if(ontClass.isComplementClass()) {
    	        	anonClass = createAnonClass(AnonClassType.Complement, ontClass, membs);
    	        	booleanClassDescription = ontClass.asComplementClass();
    	        }
    			
    			if(anonClass != null)
    				classMap.put(ontClass, anonClass);
    			
    		}
    		
    	}
    	
    	return anonClass;
    }
    
    public void addExtentionalClasses(AbstractOntologyCodeClassImpl ontologyClass, ExtentionalOntologyCodeClass...extentionalOntologyCodeClasses) {
        
    	if(extentionalOntologyCodeClasses != null)
    		for(ExtentionalOntologyCodeClass extentionalOntologyCodeClass : extentionalOntologyCodeClasses)
    			ontologyClass.addExtentionalOntologyCodeClasses(extentionalOntologyCodeClass);
    	
    }
    
    @SuppressWarnings("unchecked")
	private <T extends AbstractOntologyCodeClass> T createAnonClass(AnonClassType anonClassType, OntResource anon, AbstractOntologyCodeClass...members){
    	return (T) AnonymousClassBuilder.build(anonClassType, anon, codeModel, members);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractOntologyCodeClass> T createOntologyClass(OntResource resource, Class<T> ontologyEntityClass) throws NotAvailableOntologyCodeEntityException {
        T ontologyClass = null;
        
        if(OntologyCodeClass.class.isAssignableFrom(ontologyEntityClass)) ontologyClass = (T) createClass(resource);
        /*else if(ontologyEntityClass == SparqlOntologyCodeClass.class) ontologyClass = (T) createSparqlClass(resource);*/
        else if(OntologyCodeInterface.class.isAssignableFrom(ontologyEntityClass)) ontologyClass = (T) createInterface(resource);
        else throw new NotAvailableOntologyCodeEntityException(ontologyEntityClass);
        
        return ontologyClass;
    }
    
}
