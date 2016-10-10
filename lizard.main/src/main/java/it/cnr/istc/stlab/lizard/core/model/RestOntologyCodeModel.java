package it.cnr.istc.stlab.lizard.core.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.BooleanClassDescription;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import it.cnr.istc.stlab.lizard.commons.AnonClassType;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.exception.NotAvailableOntologyCodeEntityException;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClassImpl;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.datatype.DatatypeCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;
import it.cnr.istc.stlab.lizard.core.anonymous.AnonymousClassBuilder;

public class RestOntologyCodeModel implements OntologyCodeModel {
	
	private OntologyCodeModel apiCodeModel;
    
	protected JCodeModel codeModel;
    protected OntModel ontModel;
    
    protected Map<OntResource,Set<AbstractOntologyCodeMethod>> methodMap;
    
    protected Map<Class<? extends AbstractOntologyCodeClass>, Map<OntResource,AbstractOntologyCodeClass>> entityMap;
    
	public RestOntologyCodeModel(OntModel ontModel) {
		this.codeModel = new JCodeModel();
        this.ontModel = ontModel;
        this.methodMap = new HashMap<OntResource,Set<AbstractOntologyCodeMethod>>();
    	this.entityMap = new HashMap<Class<? extends AbstractOntologyCodeClass>, Map<OntResource,AbstractOntologyCodeClass>>();
    	
    }
	
	public RestOntologyCodeModel(OntologyCodeModel apiCodeModel) {
		this.ontModel = apiCodeModel.asOntModel();
    	this.codeModel = apiCodeModel.asJCodeModel();
    	this.entityMap = apiCodeModel.getEntityMap();
    	this.methodMap = apiCodeModel.getMethodMap();
    }	
    
    public OntologyCodeModel getApiCodeModel() {
		return apiCodeModel;
	}
    
    @SuppressWarnings({"unchecked" })
	private <T extends AbstractOntologyCodeClass> T createBeanClass(OntResource resource) {
    	
    	OntologyCodeClass ontologyClass = null;
        try {
        	if(resource.isURIResource()){
	            ontologyClass = new BeanOntologyCodeClass(resource, this, codeModel);
        	}
        	else {
        		ontologyClass = createAnonClass((OntClass)resource);
        	}
        	
        	Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(BeanOntologyCodeClass.class);
        	
        	if(beanClasses == null){
        		beanClasses = new HashMap<OntResource, AbstractOntologyCodeClass>();
        		entityMap.put(BeanOntologyCodeClass.class, beanClasses);
        	}
        	beanClasses.put(resource, ontologyClass);
        	
            
        } catch (ClassAlreadyExistsException e) {
        	Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(BeanOntologyCodeClass.class);
        	if(beanClasses != null) ontologyClass = (OntologyCodeClass) beanClasses.get(resource);
        }
        return (T) ontologyClass;
    }
    
    @SuppressWarnings({"unchecked" })
	private <T extends OntologyCodeInterface> T createInterface(OntResource resource) {
    	
    	OntologyCodeInterface ontologyInterface = null;
        try {
        	if(resource.isURIResource()){
	            ontologyInterface = new BeanOntologyCodeInterface(resource, this, codeModel);
        	}
        	
        	Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(BeanOntologyCodeInterface.class);
        	if(beanClasses == null){
        		
        		beanClasses = new HashMap<OntResource, AbstractOntologyCodeClass>();
        		entityMap.put(BeanOntologyCodeInterface.class, beanClasses);
        	}
        	beanClasses.put(resource, ontologyInterface);
        	
        } catch (ClassAlreadyExistsException e) {
        	Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(BeanOntologyCodeInterface.class);
        	if(beanClasses != null) ontologyInterface = (OntologyCodeInterface) beanClasses.get(resource);
        }
        return (T) ontologyInterface;
    }
    
    @SuppressWarnings({"unchecked" })
	private <T extends AbstractOntologyCodeClass> T createJenaClass(OntResource resource) {
    	
    	OntologyCodeClass ontologyClass = null;
        try {
        	if(resource.isURIResource()){
	            ontologyClass = new JenaOntologyCodeClass(resource, this, codeModel);
        	}
        	
        	Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(JenaOntologyCodeClass.class);
        	if(beanClasses == null){
        		beanClasses = new HashMap<OntResource, AbstractOntologyCodeClass>();
        		entityMap.put(JenaOntologyCodeClass.class, beanClasses);
        	}
        	beanClasses.put(resource, ontologyClass);
        	
        } catch (ClassAlreadyExistsException e) {
        	Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(JenaOntologyCodeClass.class);
        	if(beanClasses != null) ontologyClass = (JenaOntologyCodeClass) beanClasses.get(resource);
        }
        return (T) ontologyClass;
    }
    
    @SuppressWarnings({"unchecked" })
	private <T extends AbstractOntologyCodeClass> T createRestClass(OntResource resource) {
    	
    	OntologyCodeClass ontologyClass = null;
        try {
        	if(resource.isURIResource()){
	            ontologyClass = new RestOntologyCodeClass(resource, this, codeModel);
        	}
        	
        	Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(RestOntologyCodeClass.class);
        	if(beanClasses == null){
        		beanClasses = new HashMap<OntResource, AbstractOntologyCodeClass>();
        		entityMap.put(RestOntologyCodeClass.class, beanClasses);
        	}
        	beanClasses.put(resource, ontologyClass);
        	
        } catch (ClassAlreadyExistsException e) {
        	Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(RestOntologyCodeClass.class);
        	if(beanClasses != null) ontologyClass = (RestOntologyCodeClass) beanClasses.get(resource);
        }
        return (T) ontologyClass;
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
        	if(BeanOntologyCodeClass.class.isAssignableFrom(owner.getClass()) || BeanOntologyCodeInterface.class.isAssignableFrom(owner.getClass())) 
        		ontologyMethod = new BeanOntologyCodeMethod(methodType, methodResource, owner, domain, range, this, codeModel);
	        else if(JenaOntologyCodeClass.class.isAssignableFrom(owner.getClass())) 
	        	ontologyMethod = new JenaOntologyCodeMethod(methodType, methodResource, owner, domain, range, this, codeModel);
	        else if(RestOntologyCodeClass.class.isAssignableFrom(owner.getClass())) 
	        	ontologyMethod = new RestOntologyCodeMethod(methodType, methodResource, owner, domain, range, this, codeModel);
	        
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
            	((JDefinedClass)ontologyClass.asJDefinedClass())._implements(ontologyInterface.asJDefinedClass());
                
                ontologyClass.implementsInterfaces(ontologyInterface);
                
                Collection<AbstractOntologyCodeMethod> methods = ontologyInterface.getMethods();
                
                for(AbstractOntologyCodeMethod method : methods)
                	createMethod(method.getMethodType(), method.getOntResource(), ontologyClass, method.getDomain(), method.getRange());
            }
        }
        return ontologyClass;
    }
    
    public BooleanAnonClass createAnonClass(OntResource ontResource){
    	
    	BooleanAnonClass anonClass = null;
    	if(ontResource.isClass()){
	    	OntClass ontClass = (OntClass) ontResource;
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
	    			
	    			if(anonClass != null){
	    				Map<OntResource, AbstractOntologyCodeClass> anonClasses = entityMap.get(BooleanAnonClass.class);
	    				if(anonClasses == null){
	    					anonClasses = new HashMap<OntResource, AbstractOntologyCodeClass>();
	    					entityMap.put(BooleanAnonClass.class, anonClasses);
	    				}
	    				anonClasses.put(ontClass, anonClass);
	    			}
	    			
	    		}
	    		
	    	}
    	}
    	return anonClass;
    }
    
    @SuppressWarnings("unchecked")
	private <T extends AbstractOntologyCodeClass> T createAnonClass(AnonClassType anonClassType, OntResource anon, AbstractOntologyCodeClass...members){
    	return (T) AnonymousClassBuilder.build(anonClassType, anon, codeModel, members);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractOntologyCodeClass> T createOntologyClass(OntResource resource, Class<T> ontologyEntityClass) throws NotAvailableOntologyCodeEntityException {
        T ontologyClass = null;
        if(resource.isAnon()) 
        	ontologyClass = (T) createAnonClass(resource);
        else{
        	if(DatatypeCodeInterface.class.isAssignableFrom(ontologyEntityClass))
    			try {
    				ontologyClass = (T) new DatatypeCodeInterface(resource, this, this.codeModel);
    			} catch (ClassAlreadyExistsException e) {
    				ontologyClass = (T) getOntologyClass(resource, BeanOntologyCodeInterface.class);
    				e.printStackTrace();
    			}
        	else if(BeanOntologyCodeClass.class.isAssignableFrom(ontologyEntityClass)) ontologyClass = (T) createBeanClass(resource);
	        else if(BeanOntologyCodeInterface.class.isAssignableFrom(ontologyEntityClass)) ontologyClass = (T) createInterface(resource);
	        else if(JenaOntologyCodeClass.class.isAssignableFrom(ontologyEntityClass)) ontologyClass = (T) createJenaClass(resource);
	        else if(RestOntologyCodeClass.class.isAssignableFrom(ontologyEntityClass)) {
	        	ontologyClass = (T) createRestClass(resource);
	        }
	        else throw new NotAvailableOntologyCodeEntityException(ontologyEntityClass);
        }
        
        return ontologyClass;
    }

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractOntologyCodeClass> T getOntologyClass(OntResource ontResource, Class<T> ontologyClass) {
    	return (T)entityMap.get(ontologyClass).get(ontResource);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractOntologyCodeClass> Map<OntResource, T> getOntologyClasses(Class<T> ontologyEntityClass) {
		return (Map<OntResource, T>) entityMap.get(ontologyEntityClass);
	}

	@Override
	public BooleanAnonClass createAnonClass(OntClass ontClass) {
    	
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
    				//memberClass = createInterface(member);
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
    			
    		}
    		
    	}
    	
    	return anonClass;
    }
	
	
	
	public JCodeModel asJCodeModel(){
        return codeModel;
    }
    
    public OntModel asOntModel(){
        return ontModel;
    }

	@Override
	public String getBaseNamespace() {
		return ontModel.getNsPrefixURI("");
	}
	
	@Override
	public Map<Class<? extends AbstractOntologyCodeClass>, Map<OntResource, AbstractOntologyCodeClass>> getEntityMap() {
		return entityMap;
	}
	
	@Override
	public Map<OntResource, Set<AbstractOntologyCodeMethod>> getMethodMap() {
		return methodMap;
	}
    
}
