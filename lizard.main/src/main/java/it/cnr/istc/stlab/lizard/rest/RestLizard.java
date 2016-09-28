package it.cnr.istc.stlab.lizard.rest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.CardinalityRestriction;
import org.apache.jena.ontology.MaxCardinalityRestriction;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.OntTools;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import com.sun.codemodel.writer.FileCodeWriter;

import it.cnr.istc.stlab.lizard.commons.InsufficientArgumentsForInstantiationException;
import it.cnr.istc.stlab.lizard.commons.MavenUtils;
import it.cnr.istc.stlab.lizard.commons.OntologyCodeProject;
import it.cnr.istc.stlab.lizard.commons.exception.NotAvailableOntologyCodeEntityException;
import it.cnr.istc.stlab.lizard.commons.jena.RuntimeJenaLizardContext;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClassImpl;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;
import it.cnr.istc.stlab.lizard.commons.recipe.OntologyCodeGenerationRecipe;
import it.cnr.istc.stlab.lizard.rest.model.RestOntologyCodeModel;

public class RestLizard implements OntologyCodeGenerationRecipe {

	private URI ontologyURI;
	private RestOntologyCodeModel ontologyModel;
	
	public RestLizard(URI ontologyURI) {
		this.ontologyURI = ontologyURI;
		
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        ontModel.read(ontologyURI.toString());
        
        this.ontologyModel = new RestOntologyCodeModel(ontModel);
    }
    
	@Override
    public OntologyCodeProject generate(){
        
    	OntModel ontModel = ontologyModel.asOntModel();
    	
    	String baseURI = ontModel.getNsPrefixURI("");
        if(baseURI == null){
        	ExtendedIterator<Ontology> ontologyIt = ontModel.listOntologies();
        	while(ontologyIt.hasNext()) baseURI = ontologyIt.next().getURI();
        	if(baseURI == null) ontModel.setNsPrefix("", ontologyURI.toString());
        	else ontModel.setNsPrefix("", baseURI);
        }
        
        URI ontologyBaseURI;
		try {
			ontologyBaseURI = new URI(baseURI);
		} catch (URISyntaxException e) {
			ontologyBaseURI = ontologyURI;
		}
        
        
        List<OntClass> roots = OntTools.namedHierarchyRoots(ontModel);
        
        
        for(OntClass root : roots){
            visitHierarchyTree(root, ontologyModel, null);
        }
        
        
        
         
                
        /*
        CodeWriter writer = new SingleStreamCodeWriter(System.out);
        
        
        try {
            codeModel.build(writer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
        
        return new OntologyCodeProject(ontologyBaseURI, ontologyModel);
    }
    
    private BooleanAnonClass manageAnonClasses(OntClass ontClass, OntologyCodeModel ontologyModel, Set<AbstractOntologyCodeClass> ontologyClasses){
    	return ontologyModel.createAnonClass(ontClass);
    }
    
    private void visitHierarchyTree(OntClass ontClass, OntologyCodeModel ontologyModel, Set<AbstractOntologyCodeClass> ontologyClasses){
        
    	
    	
    	OntologyCodeClass ontologyClass = addClass(ontClass, ontologyModel);
    	createMethods(ontologyClass);
        
        //SparqlOntologyCodeClass sparqlOntologyClass = addSparqlClass(ontClass, ontologyModel);
        /**
         * FIXME
         * ontologyModel.createClassImplements(ontologyClass, ontologyInterface);
         */
        
        if(ontologyClasses == null) ontologyClasses = new HashSet<AbstractOntologyCodeClass>();
        ontologyClasses.add(ontologyClass);
        
        ExtendedIterator<OntClass> subClasses = ontClass.listSubClasses();
        while(subClasses.hasNext()){
            OntClass subClass = subClasses.next();
            
            if(subClass.isURIResource()) visitHierarchyTree(subClass, ontologyModel, ontologyClasses);
            else manageAnonClasses(subClass, ontologyModel, ontologyClasses);
        }
        
    }
    
    private void addImplementations(Set<AbstractOntologyCodeClassImpl> ontologyClasses){
        
    	
    	
        for(AbstractOntologyCodeClassImpl ontologyClass : ontologyClasses){
        	OntologyCodeInterface ontologyInterface = ontologyModel.getInterfaceMap().get(ontologyClass.getOntResource());
        	
        	OntResource ontRes = ontologyClass.getOntResource();
        	OntClass ontClass = null;
        	if(ontRes.isURIResource())
        		ontClass = ontologyModel.asOntModel().getOntClass(ontologyClass.getOntResource().getURI());
        	else ontClass = (OntClass)ontologyModel.asOntModel().getOntResource(ontologyClass.getOntResource());
        	
            ExtendedIterator<OntClass> superClassIt = ontClass.listSuperClasses(false);
            List<OntologyCodeInterface> ontologySuperInterfaces = new ArrayList<OntologyCodeInterface>();
            ontologySuperInterfaces.add(ontologyModel.getOntologyInterface(ModelFactory.createOntologyModel().createOntResource(OWL2.Thing.getURI())));
            
            if(ontologyInterface != null)
            	ontologySuperInterfaces.add(ontologyInterface);
            
            while(superClassIt.hasNext()){
                OntClass superClass = superClassIt.next();
                OntologyCodeInterface ontologySuperInterface = ontologyModel.getOntologyInterface(superClass);
                if(ontologySuperInterface != null)
                    ontologySuperInterfaces.add(ontologySuperInterface);
            }
            OntologyCodeInterface[] classArray = new OntologyCodeInterface[ontologySuperInterfaces.size()];
            
            ontologyModel.createClassImplements(ontologyClass, ontologySuperInterfaces.toArray(classArray));
            
            /*
             * Create class constructor
             */
            JDefinedClass jClass = ontologyClass.asJDefinedClass();
            /*
            JMethod constructor = jClass.getConstructor(new JType[]{jClass.owner()._ref(RDFNode.class)});
            if(constructor != null)
            	if(constructor.body().pos() > 1)
            		constructor.body().pos(2);
            */
            JMethod constructor = null;
            
            
            JCodeModel codeModel = jClass.owner();
            
            ExtendedIterator<OntClass> subClasses = ontClass.listSuperClasses();
            
            JVar ontPropertyVar = null, jenaModelVar = null;
            boolean isOntPropertyVarNullable = true;
            
            while(subClasses.hasNext()){
            	
            	OntClass subClass = subClasses.next();
            	if(subClass.isRestriction()){
            		Restriction restriction = subClass.asRestriction();
            		OntProperty property = restriction.getOnProperty();
            		if(restriction.isSomeValuesFromRestriction()){
            			
            			Set<AbstractOntologyCodeMethod> methods = ontologyClass.getMethods(property);
            			if(methods != null && !methods.isEmpty()){
	            			for(AbstractOntologyCodeMethod method : methods){
	            				if(method.getMethodType() == OntologyCodeMethodType.Get){
	            					
	            					if(constructor == null){
	            						constructor = jClass.constructor(JMod.PUBLIC);
	            						JVar indVar = constructor.param(codeModel.ref(RDFNode.class), "individual");
	            						constructor.body().invoke("this").arg(indVar);
	            					}
	            					JClass jc = codeModel.ref(Set.class).narrow(method.getRange().asJDefinedClass());
	            					JVar param = constructor.param(jc, method.getEntityName());
	            					
	            					if(ontPropertyVar == null) {
	            						ontPropertyVar = constructor.body().decl(codeModel._ref(Property.class), "ontResource", codeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(method.getOntResource().getURI()));
	            						isOntPropertyVarNullable = false;
	            					}
	            					else constructor.body().assign(ontPropertyVar, codeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(method.getOntResource().getURI()));
	            					
	            					if(jenaModelVar == null)
	            						jenaModelVar = constructor.body().decl(codeModel._ref(Model.class), "model", codeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
	            					else constructor.body().assign(jenaModelVar, codeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
	                            	
	            					JForEach forEach = constructor.body().forEach(method.getRange().asJDefinedClass(), "object", param);
	                            	JBlock forEachBlock = forEach.body();
	                            	
	                            	forEachBlock.add(jenaModelVar.invoke("add")
	            	                	.arg(JExpr.cast(codeModel._ref(Resource.class), JExpr._super().ref("individual")))
	                    				.arg(ontPropertyVar)
	                    				.arg(forEach.var().invoke("getIndividual")));
	            					
	            				}
	            			}
            			}
            		}
            		else if(restriction.isCardinalityRestriction()){
            			CardinalityRestriction cardinalityRestriction = restriction.asCardinalityRestriction();
            			int cardinality = cardinalityRestriction.getCardinality();
            			RDFNode onClass = cardinalityRestriction.getPropertyValue(OWL2.onClass);
            			Set<AbstractOntologyCodeMethod> methods = ontologyClass.getMethods(property);
            			if(methods != null && !methods.isEmpty()){
	            			for(AbstractOntologyCodeMethod method : methods){
	            				if(method.getMethodType() == OntologyCodeMethodType.Get){
	            					
	            					if(constructor == null){
	            						constructor = jClass.constructor(JMod.PUBLIC);
	            						JVar indVar = constructor.param(codeModel.ref(RDFNode.class), "individual");
	            						constructor.body().invoke("this").arg(indVar);
	            					}
	            					
	            					JClass jc = codeModel.ref(Set.class).narrow(method.getRange().asJDefinedClass());
	            					JVar param = constructor.param(jc, method.getEntityName());
	            					
	            					constructor._throws(InsufficientArgumentsForInstantiationException.class);
	            					
	            					JBlock constructorBody = constructor.body();
	            					
	            					JConditional ifBlock = constructorBody._if(JExpr.ref(method.getEntityName()).invoke("size").eq(JExpr.lit(cardinality)));
	            					
	            					
	            					JBlock ifThenBlock = ifBlock._then();
	            					
	            					if(ontPropertyVar == null) ontPropertyVar = constructorBody.decl(codeModel._ref(Property.class), "ontResource", codeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(method.getOntResource().getURI()));
	            					else ifThenBlock.assign(ontPropertyVar, codeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(method.getOntResource().getURI()));
	            					
	            					//ifThenBlock.add(JExpr._super().invoke("setPropertyValue").arg(ontPropertyVar).arg(method.getRange().asJDefinedClass().dotclass()));
	            					if(jenaModelVar == null)
	            						jenaModelVar = constructor.body().decl(codeModel._ref(Model.class), "model", codeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
	            					else constructor.body().assign(jenaModelVar, codeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
	                            	
	            					JForEach forEach = constructor.body().forEach(method.getRange().asJDefinedClass(), "object", param);
	                            	JBlock forEachBlock = forEach.body();
	                            	
	                            	forEachBlock.add(jenaModelVar.invoke("add")
	            	                	.arg(JExpr.cast(codeModel._ref(Resource.class), JExpr._super().ref("individual")))
	                    				.arg(ontPropertyVar)
	                    				.arg(forEach.var().invoke("getIndividual")));
	            					
	            					
	            					
	            					JBlock elseBlock = ifBlock._else();
	            					
	            					
	            					JClass classJClass = jClass.owner().ref(Class[].class);
	            					
	            					List<JClass> jClasses = jc.getTypeParameters();
	            					
	            					JClass narrowedClass = jClasses.get(0);
	            					//classJClass = classJClass.narrow(narrowedClass);
	            					
	            					elseBlock._throw(JExpr._new(jClass.owner()._ref(InsufficientArgumentsForInstantiationException.class)).arg(JExpr._super().ref("individual")).arg(JExpr._new(classJClass).arg(narrowedClass.dotclass())));
	            					//constructorBody.invoke("addInstanceToExtentionalClass");
	            				}
	            			}
            			}
            		}
            		else if(restriction.isMinCardinalityRestriction()){
            			
            			int cardinality = restriction.asMinCardinalityRestriction().getMinCardinality();
            			RDFNode onClass = restriction.asMinCardinalityRestriction().getPropertyValue(OWL2.onClass);
            			Set<AbstractOntologyCodeMethod> methods = ontologyClass.getMethods(property);
            			if(methods != null && !methods.isEmpty()){
	            			for(AbstractOntologyCodeMethod method : methods){
	            				if(method.getMethodType() == OntologyCodeMethodType.Get){
	            					
	            					if(constructor == null){
	            						constructor = jClass.constructor(JMod.PUBLIC);
	            						JVar indVar = constructor.param(codeModel.ref(RDFNode.class), "individual");
	            						constructor.body().invoke("this").arg(indVar);
	            					}
	            					
	            					JClass jc = codeModel.ref(Set.class).narrow(method.getRange().asJDefinedClass());
	            					JVar param = constructor.param(jc, method.getEntityName());
	            					
	            					constructor._throws(InsufficientArgumentsForInstantiationException.class);
	            					
	            					JBlock constructorBody = constructor.body();
	            					
	            					JConditional ifBlock = constructorBody._if(JExpr.ref(method.getEntityName()).invoke("size").gte(JExpr.lit(cardinality)));
	            					
	            					JBlock ifThenBlock = ifBlock._then();
	            					
	            					if(ontPropertyVar == null) ontPropertyVar = constructorBody.decl(codeModel._ref(Property.class), "ontResource", codeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(method.getOntResource().getURI()));
	            					else ifThenBlock.assign(ontPropertyVar, codeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(method.getOntResource().getURI()));
	            					
	            					//ifThenBlock.add(JExpr._super().invoke("setPropertyValue").arg(ontPropertyVar).arg(method.getRange().asJDefinedClass().dotclass()));
	            					
	            					if(jenaModelVar == null)
	            						jenaModelVar = constructor.body().decl(codeModel._ref(Model.class), "model", codeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
	            					else constructor.body().assign(jenaModelVar, codeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
	                            	
	            					JForEach forEach = constructor.body().forEach(method.getRange().asJDefinedClass(), "object", param);
	                            	JBlock forEachBlock = forEach.body();
	                            	
	                            	forEachBlock.add(jenaModelVar.invoke("add")
	            	                	.arg(JExpr.cast(codeModel._ref(Resource.class), JExpr._super().ref("individual")))
	                    				.arg(ontPropertyVar)
	                    				.arg(forEach.var().invoke("getIndividual")));
	            					
	            					if(isOntPropertyVarNullable) ontPropertyVar = null;
	            					
	            					JBlock elseBlock = ifBlock._else();
	            					
	            					
	            					JClass classJClass = jClass.owner().ref(Class[].class);
	            					
	            					List<JClass> jClasses = jc.getTypeParameters();
	            					
	            					JClass narrowedClass = jClasses.get(0);
	            					//classJClass = classJClass.narrow(narrowedClass);
	            					
	            					elseBlock._throw(JExpr._new(jClass.owner()._ref(InsufficientArgumentsForInstantiationException.class)).arg(JExpr._super().ref("individual")).arg(JExpr._new(classJClass).arg(narrowedClass.dotclass())));
	            					
	            					//constructorBody.invoke("addInstanceToExtentionalClass");
	            				}
	            			}
            			}
            		}
            		
            		else if(restriction.isMaxCardinalityRestriction()){
            			
            			MaxCardinalityRestriction maxCardinalityRestriction = restriction.asMaxCardinalityRestriction();
            			
            			int cardinality = maxCardinalityRestriction.getMaxCardinality();
            			RDFNode onClass = maxCardinalityRestriction.getPropertyValue(OWL2.onClass);
            			Set<AbstractOntologyCodeMethod> methods = ontologyClass.getMethods(property);
            			if(methods != null && !methods.isEmpty()){
	            			for(AbstractOntologyCodeMethod method : methods){
	            				if(method.getMethodType() == OntologyCodeMethodType.Get){
	            					
	            					if(constructor == null){
	            						constructor = jClass.constructor(JMod.PUBLIC);
	            						JVar indVar = constructor.param(codeModel.ref(RDFNode.class), "individual");
	            						constructor.body().invoke("this").arg(indVar);
	            					}
	            					
	            					JClass jc = codeModel.ref(Set.class).narrow(method.getRange().asJDefinedClass());
	            					JVar param = constructor.param(jc, method.getEntityName());
	            					
	            					constructor._throws(InsufficientArgumentsForInstantiationException.class);
	            					constructor._throws(InsufficientArgumentsForInstantiationException.class);
	            					
	            					JBlock constructorBody = constructor.body();
	            					
	            					JConditional ifNotNullBlock = constructorBody._if(JExpr.ref(method.getEntityName()).ne(JExpr._null()));
	            					JConditional ifBlock = ifNotNullBlock._then()._if(JExpr.ref(method.getEntityName()).invoke("size").lte(JExpr.lit(cardinality)));
	            					
	            					JBlock ifThenBlock = ifBlock._then();
	            					
	            					if(ontPropertyVar == null) ontPropertyVar = constructorBody.decl(codeModel._ref(Property.class), "ontResource", codeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(method.getOntResource().getURI()));
	            					else ifThenBlock.assign(ontPropertyVar, codeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(method.getOntResource().getURI()));
	            					
	            					//ifThenBlock.add(JExpr._super().invoke("setPropertyValue").arg(ontPropertyVar).arg(method.getRange().asJDefinedClass().dotclass()));
	            					
	            					if(jenaModelVar == null)
	            						jenaModelVar = constructor.body().decl(codeModel._ref(Model.class), "model", codeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
	            					else constructor.body().assign(jenaModelVar, codeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
	                            	
	            					JForEach forEach = constructor.body().forEach(method.getRange().asJDefinedClass(), "object", param);
	                            	JBlock forEachBlock = forEach.body();
	                            	
	                            	forEachBlock.add(jenaModelVar.invoke("add")
	            	                	.arg(JExpr.cast(codeModel._ref(Resource.class), JExpr._super().ref("individual")))
	                    				.arg(ontPropertyVar)
	                    				.arg(forEach.var().invoke("getIndividual")));
	            					
	            					//ifThenBlock.add(JExpr._super().invoke("setPropertyValue").arg(ontPropertyVar).arg(method.getRange().asJDefinedClass().dotclass()));
	            					
	            					if(isOntPropertyVarNullable) ontPropertyVar = null;
	            					
	            					
	            					JBlock elseBlock = ifBlock._else();
	            					
	            					
	            					JClass classJClass = jClass.owner().ref(Class[].class);
	            					
	            					List<JClass> jClasses = jc.getTypeParameters();
	            					
	            					JClass narrowedClass = jClasses.get(0);
	            					//classJClass = classJClass.narrow(narrowedClass);
	            					
	            					elseBlock._throw(JExpr._new(jClass.owner()._ref(InsufficientArgumentsForInstantiationException.class)).arg(JExpr._super().ref("individual")).arg(JExpr._new(classJClass).arg(narrowedClass.dotclass())));
	            					//constructorBody.invoke("addInstanceToExtentionalClass");
	            				}
	            			}
            			}
            		}
            		
            	}
            }
            
            /*
            if(constructor.hasSignature(new JType[]{codeModel._ref(Resource.class)})) 
            	constructor.body().invoke("addInstanceToExtentionalClass");
            */
            
        }
        
    }
    
    private OntologyCodeClass addClass(OntClass ontClass, OntologyCodeModel ontologyModel){
        OntologyCodeClass ontologyClass = null;
        try {
            ontologyClass = ontologyModel.createOntologyClass(ontClass, OntologyCodeClass.class);
        } catch (NotAvailableOntologyCodeEntityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ontologyClass;
    }
    
    /*
    private SparqlOntologyCodeClass addSparqlClass(OntClass ontClass, OntologyCodeModel ontologyModel){
        SparqlOntologyCodeClass sparqlOntologyClass = ontologyModel.getSparqlOntologyClass(ontClass);
        return sparqlOntologyClass;
    }
    */
    
    private void createMethods(AbstractOntologyCodeClass owner){

        OntClass ontClass = ontologyModel.asOntModel().getOntClass(owner.getOntResource().getURI());
        
        ExtendedIterator<OntProperty> propIt = ontClass.listDeclaredProperties();
        while(propIt.hasNext()){
            
            OntProperty ontProperty = propIt.next();
        
            OntResource range = ontProperty.getRange();
            
            if(range != null){
            	if(range.isURIResource()){
                    if(range.isClass()){
                        OntClass rangeOntClass = ModelFactory.createOntologyModel().createClass(range.getURI());
                        OntologyCodeClass rangeClass;
                        try {
                            rangeClass = ontologyModel.createOntologyClass(rangeOntClass, OntologyCodeClass.class);
                            
                            Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
                            domain.add(rangeClass);
                            ontologyModel.createMethod(OntologyCodeMethodType.Get, ontProperty, owner, domain, rangeClass);
                            ontologyModel.createMethod(OntologyCodeMethodType.Set, ontProperty, owner, domain, rangeClass);
                            
                        } catch (NotAvailableOntologyCodeEntityException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
            else{
                OntResource thing = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM).createOntResource(OWL2.Thing.getURI());
                
                Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
                domain.add(ontologyModel.getOntologyInterface(thing));
                
                ontologyModel.createMethod(OntologyCodeMethodType.Get, ontProperty, owner, domain, ontologyModel.getOntologyClass(thing));
                ontologyModel.createMethod(OntologyCodeMethodType.Set, ontProperty, owner, domain, ontologyModel.getOntologyClass(thing));
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
        			
        			
        			try {
						OntologyCodeClass rangeClass = ontologyModel.createOntologyClass(ontologyModel.asOntModel().getOntResource(onClass), OntologyCodeClass.class);
						
						Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
                        domain.add(rangeClass);
						ontologyModel.createMethod(OntologyCodeMethodType.Get, onProperty, owner, null, rangeClass);
						ontologyModel.createMethod(OntologyCodeMethodType.Set, onProperty, owner, null, rangeClass);
					} catch (NotAvailableOntologyCodeEntityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        				
        		}
        	}
        	
        }
        
    
    	
    }
    
    public static void main(String[] args) {
        System.setProperty("M2_HOME", "/usr/local/apache-maven-3.1.1");
        System.setProperty("JAVA_HOME", "/Library/Java/JavaVirtualMachines/jdk1.8.0_66.jdk/Contents/Home");
        //codegen.generate();
        URI uri = null;
        try {
            //uri = new URI("http://www.ontologydesignpatterns.org/cp/owl/timeindexedsituation.owl");
            uri = new URI("http://stlab.istc.cnr.it/documents/mibact/cultural-ON_xml.owl");
        	//uri = new URI("vocabs/foaf.rdf");
            
            OntologyCodeGenerationRecipe codegen = new RestLizard(uri);
            OntologyCodeProject ontologyCodeProject = codegen.generate();
            
            try {
            	File testFolder = new File("test_out");
            	if(testFolder.exists()) {
            		System.out.println("esists " + testFolder.getClass());
            		FileUtils.deleteDirectory(testFolder);
            	}
            	else System.out.println("not esists");
                File src = new File("test_out/src/main/java");
                File resources = new File("test_out/src/main/resources");
                File test = new File("test_out/src/test/java");
                if(!src.exists()) src.mkdirs();
                if(!resources.exists()) resources.mkdirs();
                if(!test.exists()) test.mkdirs();
                
                CodeWriter writer = new FileCodeWriter(src, "UTF-8");
                ontologyCodeProject.getOntologyCodeModel().asJCodeModel().build(writer);
                
                /*
                 * Generate the POM descriptor file and build the project
                 * as a Maven project.
                 */
                File pom = new File("test_out/pom.xml");
                Writer pomWriter = new FileWriter(new File("test_out/pom.xml"));
                Map<String,String> dataModel = new HashMap<String,String>();
                dataModel.put("artifactId", ontologyCodeProject.getArtifactId());
                dataModel.put("groupId", ontologyCodeProject.getGroupId());
                MavenUtils.generatePOM(pomWriter, dataModel);
                MavenUtils.buildProject(pom);
                
                
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
}
