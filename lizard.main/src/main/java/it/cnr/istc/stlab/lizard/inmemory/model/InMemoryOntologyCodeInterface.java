package it.cnr.istc.stlab.lizard.inmemory.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.SourceVersion;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

import it.cnr.istc.stlab.lizard.commons.LizardInterface;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.exception.NotAvailableOntologyCodeEntityException;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

public class InMemoryOntologyCodeInterface extends OntologyCodeInterface {
    
    protected OntologyCodeClassType ontologyClassType;
    
    InMemoryOntologyCodeInterface(OntResource resource, OntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
        super(resource, ontologyModel, codeModel);
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
            
            ontologyMethod = new InMemoryOntologyCodeMethod(methodType, methodResource, this, classDomain, range, ontologyModel, jCodeModel);
            
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
    
    @Override
    public void createMethods(){
        //OntClass ontClass = (OntClass)ontResource;
        OntClass ontClass = ontologyModel.asOntModel().getOntClass(ontResource.getURI());
        
        ExtendedIterator<OntProperty> propIt = ontClass.listDeclaredProperties();
        while(propIt.hasNext()){
            
            OntProperty ontProperty = propIt.next();
        
            OntResource range = ontProperty.getRange();
            
            if(range != null){
                if(range.isURIResource()){
                    if(range.isClass()){
                        OntClass rangeOntClass = ModelFactory.createOntologyModel().createClass(range.getURI());
                        OntologyCodeInterface rangeClass;
                        try {
                            rangeClass = ontologyModel.createOntologyClass(rangeOntClass, OntologyCodeInterface.class);
                            createMethod(OntologyCodeMethodType.Get, ontProperty, rangeClass);
                            createMethod(OntologyCodeMethodType.Set, ontProperty, rangeClass);
                        } catch (NotAvailableOntologyCodeEntityException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                else if(range.isClass()){
                	OntClass rangeOntClass = range.asClass();
                	BooleanAnonClass rangeClass = ontologyModel.createAnonClass(rangeOntClass);
                	
                	createMethod(OntologyCodeMethodType.Get, ontProperty, rangeClass);
                    createMethod(OntologyCodeMethodType.Set, ontProperty, rangeClass);
                		
                }
            }
            else{
                OntResource thing = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM).createOntResource(OWL2.Thing.getURI());
                createMethod(OntologyCodeMethodType.Get, ontProperty, ontologyModel.getOntologyInterface(thing));
                createMethod(OntologyCodeMethodType.Set, ontProperty, ontologyModel.getOntologyInterface(thing));
            }
            
        }
        
        ExtendedIterator<OntClass> superClassesIt = ontClass.listSuperClasses();
        while(superClassesIt.hasNext()){
        	OntClass superClass = superClassesIt.next();
        	if(superClass.isRestriction()){
        		Restriction restriction = superClass.asRestriction();
        		OntProperty onProperty = restriction.getOnProperty();
        		Resource onClass = null;
        		if(restriction.isSomeValuesFromRestriction()){
        			onClass = restriction.asSomeValuesFromRestriction().getSomeValuesFrom();
        		}
        		else if(restriction.isAllValuesFromRestriction()){
        			onClass = restriction.asAllValuesFromRestriction().getAllValuesFrom();
        		}
        		/*
        		else if(restriction.isCardinalityRestriction()){
        			onClass = restriction.asCardinalityRestriction().get
        		}
        		*/
        		if(onClass != null){
        			System.out.println("Resctriction " + ontClass);
        			System.out.println("  On class ont " + onClass);
        			
        			
        			try {
						InMemoryOntologyCodeInterface rangeClass = ontologyModel.createOntologyClass(ontologyModel.asOntModel().getOntResource(onClass), InMemoryOntologyCodeInterface.class);
						createMethod(OntologyCodeMethodType.Get, onProperty, rangeClass);
					} catch (NotAvailableOntologyCodeEntityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        				
        		}
        	}
        	
        }
        
    }
    
    void extendsClasses(AbstractOntologyCodeClass oClass){
        if(oClass != null && oClass instanceof InMemoryOntologyCodeInterface)
            this.extendedClass = oClass;
    }
    
    public Set<AbstractOntologyCodeClass> listSuperClasses(){
        Set<AbstractOntologyCodeClass> superClasses = new HashSet<AbstractOntologyCodeClass>();
        superClasses.add(extendedClass);
        return superClasses;
    }

}
