package it.cnr.istc.stlab.lizard.jena.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.SourceVersion;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import it.cnr.istc.stlab.lizard.Constants;
import it.cnr.istc.stlab.lizard.commons.ExtentionalLizardClassImpl;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.ExtentionalOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

public class JenaExtentionalOntologyCodeClass extends ExtentionalOntologyCodeClass {
    
    protected OntologyCodeClassType ontologyClassType;
    
    JenaExtentionalOntologyCodeClass(OntologyCodeInterface ontologyInteface) throws ClassAlreadyExistsException {
        super(ontologyInteface);
        
        super.ontologyClassType = OntologyCodeClassType.ExtentionalClass;
        
        if(ontResource.isURIResource()){
        	String artifactId = packageName + "." + Constants.EXTENTIONAL_CLASS_PACKAGE + ".";
            
            String localName = ontResource.getLocalName() + Constants.EXTENTIONAL_CLASS_POSTFIX;
            
            if(!SourceVersion.isName(localName)) localName = "_" + localName;
            
            super.entityName = artifactId + localName;
            try {
                super.jClass = jCodeModel._class(entityName, ClassType.CLASS);
                JClass extentionalLizardClass = jCodeModel.ref(ExtentionalLizardClassImpl.class);
                extentionalLizardClass = extentionalLizardClass.narrow(ontologyInteface.asJDefinedClass());
                super.jClass._extends(extentionalLizardClass);
                
                /*
                 * Create the constructor that allows to instantiate individuals.
                 */
                JMethod constructor = super.jClass.constructor(1);
                JVar param = constructor.param(OntResource.class, "classResource");
                constructor.body().invoke("super").arg(param);
                
                
            } catch (JClassAlreadyExistsException e) {
                throw new ClassAlreadyExistsException(ontResource);
            }
        }
    
    }
    
    public AbstractOntologyCodeMethod createMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass range) {
        AbstractOntologyCodeMethod ontologyMethod = null;
        
        Set<AbstractOntologyCodeMethod> methods = getMethods(methodResource);
        if(methods != null){
            for(AbstractOntologyCodeMethod method : methods){
                if(method.getMethodType() == methodType){
                    ontologyMethod = method;
                    break;
                }
            }
        }
        if(ontologyMethod == null){
        
        	/*
             * Add the body to the method.
             */
            JType setClass = jCodeModel.ref(Set.class).narrow(range.asJDefinedClass());
            
            JMethod existingMethod = jClass.getMethod(methodResource.getLocalName(), new JType[]{});
            
            if(existingMethod != null) {
            	System.out.println("EM " + existingMethod.name());
            	if(ontResource.getURI().startsWith(ontologyModel.getBaseNamespace()))
            		System.out.println("YES"); 
            }
            
            Collection<AbstractOntologyCodeClass> classDomain = new ArrayList<AbstractOntologyCodeClass>();
            classDomain.add(this);
            
            ontologyMethod = new JenaOntologyCodeMethod(methodType, methodResource, this, classDomain, range, ontologyModel, jCodeModel);
            
            JMethod jMethod = ontologyMethod.asJMethod();
            
            
            
            String entityName = ontologyMethod.getEntityName();
            //if(methodType == OntologyCodeMethodType.Set) jMethod.param(setClass, entityName);
            
            Set<AbstractOntologyCodeMethod> ontologyMethods = methodMap.get(methodResource);
            if(ontologyMethods == null){
                ontologyMethods = new HashSet<AbstractOntologyCodeMethod>();
                methodMap.put(methodResource, ontologyMethods);
            }
            ontologyMethods.add(ontologyMethod);
            addMethod(ontologyMethod);
        }
        
        return ontologyMethod;
    }
    
    void extendsClasses(AbstractOntologyCodeClass oClass){
        if(oClass != null && oClass instanceof JenaExtentionalOntologyCodeClass)
            this.extendedClass = oClass;
    }
    
    public Set<AbstractOntologyCodeClass> listSuperClasses(){
        Set<AbstractOntologyCodeClass> superClasses = new HashSet<AbstractOntologyCodeClass>();
        superClasses.add(extendedClass);
        return superClasses;
    }

}
