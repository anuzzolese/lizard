package it.cnr.istc.stlab.lizard.inmemory.model;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.BooleanClassDescription;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JMods;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JVar;

import it.cnr.istc.stlab.lizard.anonymous.AnonymousClassBuilder;
import it.cnr.istc.stlab.lizard.commons.AnonClassType;
import it.cnr.istc.stlab.lizard.commons.annotations.ObjectPropertyAnnotation;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.exception.NotAvailableOntologyCodeEntityException;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClassImpl;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.ExtentionalOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

public class InMemoryOntologyCodeModel implements OntologyCodeModel {
    
	protected JCodeModel codeModel;
    protected OntModel ontModel;
    
    protected Map<OntResource,ExtentionalOntologyCodeClass> extentionalClassMap;
    protected Map<OntResource,OntologyCodeClass> classMap;
    protected Map<OntResource,OntologyCodeInterface> interfaceMap;
    protected Map<OntResource,Set<AbstractOntologyCodeMethod>> methodMap;
    
    protected Map<OntResource,OntologyCodeClass> anonClassMap;
    
    public InMemoryOntologyCodeModel(OntModel ontModel) {
        this.codeModel = new JCodeModel();
        this.ontModel = ontModel;
        this.anonClassMap = new HashMap<OntResource,OntologyCodeClass>();
        this.extentionalClassMap = new HashMap<OntResource,ExtentionalOntologyCodeClass>();
        this.classMap = new HashMap<OntResource,OntologyCodeClass>();
        this.interfaceMap = new HashMap<OntResource,OntologyCodeInterface>();
        this.methodMap = new HashMap<OntResource,Set<AbstractOntologyCodeMethod>>();
    }
    
    /*
    @SuppressWarnings("unchecked")
    public <T extends AbstractOntologyCodeClass> T createOntologyClass(OntResource resource, OntologyCodeClassType ontologyClassType){
        T ontologyClass = null;
        switch (ontologyClassType) {
            case Class:
                ontologyClass = (T) createClass(resource);
                break;
            case SparqlClass:
                ontologyClass = (T) createSparqlClass(resource);
                break;
            default:
                ontologyClass = (T) createInterface(resource);
                break;
        }
        return ontologyClass;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends AbstractOntologyCodeClass> T createOntologyClass(OntResource resource, OntologyCodeClassType ontologyClassType, OntologyCodeInterface...ontologyInterfaces){
        T ontologyClass = null;
        switch (ontologyClassType) {
            case Class:
                ontologyClass = (T) createClass(resource);
                break;
            case SparqlClass:
                ontologyClass = (T) createSparqlClass(resource);
                break;
            default:
                ontologyClass = (T) createInterface(resource);
                break;
        }
        return ontologyClass;
    }
    */
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractOntologyCodeClass> T createOntologyClass(OntResource resource, Class<T> ontologyEntityClass) throws NotAvailableOntologyCodeEntityException {
        T ontologyClass = null;
        
        if(OntologyCodeClass.class.isAssignableFrom(ontologyEntityClass)) ontologyClass = (T) createClass(resource);
        /*else if(ontologyEntityClass == SparqlOntologyCodeClass.class) ontologyClass = (T) createSparqlClass(resource);*/
        else if(OntologyCodeInterface.class.isAssignableFrom(ontologyEntityClass)) ontologyClass = (T) createInterface(resource);
        else throw new NotAvailableOntologyCodeEntityException(ontologyEntityClass);
        
        System.out.println("Ontology class " + ontologyClass);
        return ontologyClass;
    }
    
    @Override
    public <T extends AbstractOntologyCodeClass> T createOntologyClass(OntResource resource, Class<T> ontologyEntityClass, OntologyCodeInterface...ontologyInterfaces) throws NotAvailableOntologyCodeEntityException {
        T ontologyClass = createOntologyClass(resource, ontologyEntityClass);
        return ontologyClass;
    }
    
    @Override
	public ExtentionalOntologyCodeClass createExtentionalOntologyClass(OntologyCodeInterface ontologyCodeInterface) throws NotAvailableOntologyCodeEntityException {
    	ExtentionalOntologyCodeClass extentionalOntologyCodeClass;
        try {
        	extentionalOntologyCodeClass = new InMemoryExtentionalOntologyCodeClass(ontologyCodeInterface);
            
        	extentionalClassMap.put(ontologyCodeInterface.getOntResource(), extentionalOntologyCodeClass);
            
        } catch (ClassAlreadyExistsException e) {
        	extentionalOntologyCodeClass = extentionalClassMap.get(ontologyCodeInterface.getOntResource());
        }
        return extentionalOntologyCodeClass;
    }
    
    public OntologyCodeClass createClass(OntResource resource) {
        OntologyCodeClass ontologyClass;
        try {
            ontologyClass = new InMemoryOntologyCodeClass(resource, this, codeModel);
            
            classMap.put(resource, ontologyClass);
            
        } catch (ClassAlreadyExistsException e) {
            ontologyClass = classMap.get(resource);
        }
        return ontologyClass;
    }
    
    protected OntologyCodeInterface createInterface(OntResource resource) {
        OntologyCodeInterface ontologyInterface;
        try {
            ontologyInterface = new InMemoryOntologyCodeInterface(resource, this, codeModel);
            interfaceMap.put(resource, ontologyInterface);
        } catch (ClassAlreadyExistsException e) {
        	ontologyInterface = interfaceMap.get(resource);
        }
        return ontologyInterface;
    }
    
    /*
    private SparqlOntologyCodeClass createSparqlClass(OntResource resource) {
        SparqlOntologyCodeClass sparqlOntologyClass;
        try {
            sparqlOntologyClass = new SparqlOntologyCodeClass(resource, this, codeModel);
            sparqlClassMap.put(resource, sparqlOntologyClass);
            
        } catch (ClassAlreadyExistsException e) {
            sparqlOntologyClass = sparqlClassMap.get(resource);
        }
        return sparqlOntologyClass;
    }
    */
    
    @Override
    public OntologyCodeInterface getOntologyInterface(OntResource ontResource){
        return interfaceMap.get(ontResource);
    }
    
    @Override
    public OntologyCodeClass getOntologyClass(OntResource ontResource){
        return classMap.get(ontResource);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbstractOntologyCodeClassImpl> Map<OntResource,T> getClassMap() {
        return (Map<OntResource,T>) classMap;
    }
    
    @Override
    public Map<OntResource,OntologyCodeInterface> getInterfaceMap() {
        return interfaceMap;
    }
    
    @Override
    public AbstractOntologyCodeMethod createMethod(OntologyCodeMethodType methodType, OntResource methodResource, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range) {
    	return createMethod(methodType, methodResource, null, domain, range);
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
        
        	ontologyMethod = new InMemoryOntologyCodeMethod(methodType, methodResource, owner, domain, range, this, codeModel);
            
            JMethod jMethod = ontologyMethod.asJMethod();
            
            /*
             * Add the body to the method.
             */
            if(owner instanceof OntologyCodeClass){
	            JClass setClass = codeModel.ref(Set.class).narrow(range.asJDefinedClass());
	            
	            String entityName = ontologyMethod.getEntityName();
	            if(methodType == OntologyCodeMethodType.Get) {
	                if(owner instanceof OntologyCodeClass){
	                	
	                	JBlock methodBody = jMethod.body();
	                	
	                	JVar returnVar = methodBody.decl(setClass, "retValue", JExpr._null());
	                	
	                	JTryBlock methodTry = methodBody._try();
	                	JCatchBlock catchBlock = methodTry._catch(codeModel.ref(SecurityException.class));
	                	JVar exceptionVar = catchBlock.param("e");
	                	catchBlock.body().add(exceptionVar.invoke("printStackTrace"));
	                	
	                	
	                	JBlock methodTryBody = methodTry.body();
	                	JDefinedClass objectAnonymous = codeModel.anonymousClass(Object.class);
	                	
	                	/*
	                	 * Add the code to get information about the current method
	                	 */
	                	JExpression methodExpression = JExpr._new(objectAnonymous).invoke("getClass").invoke("getEnclosingMethod");
	                	JVar methodVar = methodTryBody.decl(codeModel._ref(Method.class), "method", methodExpression);
	                	
	                	/*
	                	 * Add the code to use the annotation ObjectPropertyAnnotation
	                	 */
	                	JExpression getAnnotation = methodVar.invoke("getAnnotation").arg(JExpr.dotclass(codeModel.ref(ObjectPropertyAnnotation.class)));
	                	JVar objectPropertyAnnotationVar = methodTryBody.decl(codeModel._ref(ObjectPropertyAnnotation.class), "objectPropertyAnnotation", getAnnotation);
	                	
	                	
	                	/*
	                	 * Add the code to set a variable for the URI representing the property and the type of the method.
	                	 */
	                	JExpression uriExpression = objectPropertyAnnotationVar.invoke("uri");
	                	JVar uriVar = methodTryBody.decl(codeModel._ref(String.class), "propertyUri", uriExpression);
	                	JVar ontPropertyVar = methodTryBody.decl(codeModel._ref(OntResource.class), "ontResource", codeModel.ref(ModelFactory.class).staticInvoke("createOntologyModel").invoke("createOntResource").arg(uriVar));
	                	
	                	
	                	//System.out.println("Range " + owner.getOntResource() + " - " + range + " : " + range.getOntResource().isAnon());
	                	methodTryBody.assign(returnVar, JExpr.cast(setClass, JExpr._super().ref("propertyMap").invoke("get").arg(ontPropertyVar).arg(range.asJDefinedClass().dotclass())));
	                	
	                	methodBody._return(returnVar);
	                	
	
	                	/*
	                    if(!owner.asJDefinedClass().fields().containsKey(entityName)){
	                        JFieldVar field = owner.asJDefinedClass().field(JMod.PRIVATE, setClass, entityName);
	                        jMethod.body()._return(field);
	                    }
	                    */
	                }
	                /*
	                else if(owner instanceof SparqlOntologyCodeClass){
	                    JType hashSetClass = codeModel.ref(HashSet.class).narrow(range.asJDefinedClass());
	                    
	                    String sparql = "SELECT ?object " +
	                    		        "WHERE{<\" + uri + \"> <" + methodResource.getURI() + "> ?object}";
	                    JVar sparqlJVar = jMethod.body().decl(codeModel._ref(String.class), "sparql", JExpr.lit(sparql));
	                    jMethod.body().assign(sparqlJVar, codeModel.ref(URLEncoder.class).staticInvoke("encode").arg(sparqlJVar).arg("UTF-8"));
	                    
	                    JVar connectionJVar = jMethod.body().decl(codeModel._ref(URLConnection.class), "connection", JExpr._new(codeModel._ref(URL.class)).arg("endpoint").invoke("openConnection"));
	                    connectionJVar.invoke("addRequestProperty").arg("Accept").arg("application/xml");
	                    
	                    JVar inputStreamJVar = jMethod.body().decl(codeModel._ref(InputStream.class), "inputStream", connectionJVar.invoke("getInputStream"));
	                    JVar resultSetJVar = jMethod.body().decl(codeModel._ref(ResultSet.class), "resultSet", codeModel.ref(ResultSetFactory.class).staticInvoke("fromXML").arg(inputStreamJVar));
	                    
	                    JVar objectSetJVar = jMethod.body().decl(setClass, "objects", JExpr._new(hashSetClass));
	                    
	                    JWhileLoop whileLoop = jMethod.body()._while(resultSetJVar.invoke("hasNext"));
	                    JVar querySolution = whileLoop.body().decl(codeModel._ref(QuerySolution.class), "querySolution", resultSetJVar.invoke("next"));
	                    JVar objectResourceJVar = whileLoop.body().decl(codeModel._ref(Resource.class), "objectResource", querySolution.invoke("getResource").arg("object"));
	                    
	                    SparqlOntologyCodeClass sparqlRange = sparqlClassMap.get(range.getOntResource());
	                    
	                    
	                    whileLoop.body().decl(sparqlRange.asJDefinedClass(), "object", JExpr._new(sparqlRange.asJDefinedClass()).arg(objectResourceJVar.invoke("getURI")));
	                    
	                    whileLoop.body().add(objectSetJVar.invoke("add").arg(objectResourceJVar));
	                    
	                    jMethod.body()._return(objectSetJVar);
	                    
	                    
	                }
	                */
	            }
	            else {
	            	//JVar methodArgument = jMethod.param(setClass, entityName);
	                if(owner instanceof OntologyCodeClass){
	
	                	JBlock methodBody = jMethod.body();
	                	
	                	JTryBlock methodTry = methodBody._try();
	                	JCatchBlock catchBlock = methodTry._catch(codeModel.ref(SecurityException.class));
	                	JVar exceptionVar = catchBlock.param("e");
	                	catchBlock.body().add(exceptionVar.invoke("printStackTrace"));
	                	
	                	
	                	JBlock methodTryBody = methodTry.body();
	                	JDefinedClass objectAnonymous = codeModel.anonymousClass(Object.class);
	                	
	                	/*
	                	 * Add the code to get information about the current method
	                	 */
	                	JExpression methodExpression = JExpr._new(objectAnonymous).invoke("getClass").invoke("getEnclosingMethod");
	                	JVar methodVar = methodTryBody.decl(codeModel._ref(Method.class), "method", methodExpression);
	                	
	                	/*
	                	 * Add the code to use the annotation ObjectPropertyAnnotation
	                	 */
	                	JExpression getAnnotation = methodVar.invoke("getAnnotation").arg(JExpr.dotclass(codeModel.ref(ObjectPropertyAnnotation.class)));
	                	JVar objectPropertyAnnotationVar = methodTryBody.decl(codeModel._ref(ObjectPropertyAnnotation.class), "objectPropertyAnnotation", getAnnotation);
	                	
	                	
	                	/*
	                	 * Add the code to set a variable for the URI representing the property and the type of the method.
	                	 */
	                	JExpression uriExpression = objectPropertyAnnotationVar.invoke("uri");
	                	JVar uriVar = methodTryBody.decl(codeModel._ref(String.class), "propertyUri", uriExpression);
	                	JVar ontPropertyVar = methodTryBody.decl(codeModel._ref(OntResource.class), "ontResource", codeModel.ref(ModelFactory.class).staticInvoke("createOntologyModel").invoke("createOntResource").arg(uriVar));
	                	
	                	
	                	methodTryBody.add(JExpr._super().ref("propertyMap").invoke("put").arg(ontPropertyVar).arg(jMethod.params().get(0)));
	                	
	                
	                }
	            }
            }
            
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
    
}
