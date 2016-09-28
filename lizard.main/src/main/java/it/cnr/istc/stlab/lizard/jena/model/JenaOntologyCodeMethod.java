package it.cnr.istc.stlab.lizard.jena.model;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.SourceVersion;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JWhileLoop;

import it.cnr.istc.stlab.lizard.commons.LizardInterface;
import it.cnr.istc.stlab.lizard.commons.PrefixRegistry;
import it.cnr.istc.stlab.lizard.commons.annotations.ObjectPropertyAnnotation;
import it.cnr.istc.stlab.lizard.commons.jena.RuntimeJenaLizardContext;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClassImpl;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;
import it.cnr.istc.stlab.lizard.inmemory.model.OntologyAnnotation;

public class JenaOntologyCodeMethod extends OntologyCodeMethod {

    JenaOntologyCodeMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass owner, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range, OntologyCodeModel ontologyModel, JCodeModel codeModel) {
        super(methodType, methodResource, owner, domain, range, ontologyModel, codeModel);
        
        if(methodResource.isURIResource()){
            
        	String namespace = methodResource.getNameSpace();
        	
        	String prefix = ontologyModel.asOntModel().getNsURIPrefix(namespace);
        	// look-up on prefix.cc
        	if(prefix == null) prefix = PrefixRegistry.getInstance().getNsPrefix(namespace);
        	// if the prefix is again null, then we create it
        	if(prefix == null) prefix = PrefixRegistry.getInstance().createNsPrefix(namespace);
        	
        	String localName = methodResource.getLocalName();
            if(!SourceVersion.isName(localName)) localName = "_" + localName;
        	
        	if(prefix.isEmpty()) entityName = localName;
        	else entityName = prefix + "_" + localName;
            
        	JDefinedClass domainJClass = owner.asJDefinedClass();
            
            JType setClass = codeModel.ref(Set.class).narrow(range.asJDefinedClass());
            
            if(methodType == OntologyCodeMethodType.Get) {
                jMethod = owner.asJDefinedClass().method(1, setClass, entityName);
            }
            else {
                String methodName = entityName.substring(0,1).toUpperCase() + entityName.substring(1);
                jMethod = domainJClass.method(1, void.class, "set" + methodName);
                if(domain != null){
	                for(AbstractOntologyCodeClass domainClass : domain){
	                	String name = domainClass.getEntityName();
	                	name = name.substring(name.lastIndexOf(".")+1);
	                	name = name.substring(0, 1).toLowerCase() + name.substring(1);
	                	jMethod.param(setClass, name);
	                }
                }
                else jMethod.param(setClass, entityName);
            }
            
            
            
            char[] fieldNameChars = entityName.toCharArray();
        	StringBuilder sb = new StringBuilder();
        	
        	Character previous = null;
        	for(char fieldNameChar : fieldNameChars){
        		if(previous != null){
        			if(Character.isLowerCase(previous) && Character.isUpperCase(fieldNameChar))
        				sb.append("_");
        		}
        		sb.append(Character.toUpperCase(fieldNameChar));
        		previous = fieldNameChar;
        	}
            
            if(owner instanceof OntologyCodeInterface){
            	JFieldVar staticField = domainJClass.fields().get(sb.toString());
            	if(staticField == null){
            		staticField = domainJClass.field(JMod.PUBLIC|JMod.STATIC|JMod.FINAL, String.class, sb.toString(), JExpr.lit(ontResource.getURI()));
            		System.out.println("SF " + methodResource + " : " + owner.getEntityName());
            	}
	        		
            	
            	JAnnotationUse jAnnotationUse = jMethod.annotate(ObjectPropertyAnnotation.class);
                jAnnotationUse.param("uri", staticField);
                jAnnotationUse.param("method", methodType);
        	}
            else { 
            	jMethod.annotate(Override.class);
            	
            	if(this.methodType == OntologyCodeMethodType.Get){
            		addClassCentricStaticMethod(codeModel, sb.toString());
            		addClassCentricStaticMethodWithParam(codeModel, sb.toString());
            	}
            }
         
        }
        
        addMethodBody();
    }
    
    
    
    public void annotate(OntologyAnnotation annotation){
        jMethod.annotate(annotation.asJCode());
    }
    
    @Override
    public int hashCode() {
    	return methodType.hashCode() + super.hashCode();
    }
    
    
    private void addMethodBody(){
    	/*
         * Add the body to the method.
         */
        if(owner instanceof OntologyCodeClass){
            JClass setClass = jCodeModel.ref(Set.class).narrow(range.asJDefinedClass());
            JClass hashSetClass = jCodeModel.ref(HashSet.class).narrow(range.asJDefinedClass());
            
            String entityName = getEntityName();
            if(methodType == OntologyCodeMethodType.Get) {
                if(owner instanceof OntologyCodeClass){
                	
                	JBlock methodBody = jMethod.body();
                	
                	JVar returnVar = methodBody.decl(setClass, "retValue", JExpr._new(hashSetClass));
                	
                	JTryBlock methodTry = methodBody._try();
                	JCatchBlock catchBlock = methodTry._catch(jCodeModel.ref(SecurityException.class));
                	JVar exceptionVar = catchBlock.param("e");
                	catchBlock.body().add(exceptionVar.invoke("printStackTrace"));
                	
                	
                	JBlock methodTryBody = methodTry.body();
                	JDefinedClass objectAnonymous = jCodeModel.anonymousClass(Object.class);
                	
                	/*
                	 * Add the code to get information about the current method
                	 */
                	//JExpression methodExpression = JExpr._new(objectAnonymous).invoke("getClass").invoke("getEnclosingMethod");
                	//JVar methodVar = methodTryBody.decl(jCodeModel._ref(Method.class), "method", methodExpression);
                	
                	/*
                	 * Add the code to use the annotation ObjectPropertyAnnotation
                	 */
                	//JExpression getAnnotation = methodVar.invoke("getAnnotation").arg(JExpr.dotclass(jCodeModel.ref(ObjectPropertyAnnotation.class)));
                	//JVar objectPropertyAnnotationVar = methodTryBody.decl(jCodeModel._ref(ObjectPropertyAnnotation.class), "objectPropertyAnnotation", getAnnotation);
                	
                	
                	/*
                	 * Add the code to set a variable for the URI representing the property and the type of the method.
                	 */
                	//JExpression uriExpression = objectPropertyAnnotationVar.invoke("uri");
                	//JVar uriVar = methodTryBody.decl(jCodeModel._ref(String.class), "propertyUri", uriExpression);
                	//JVar ontPropertyVar = methodTryBody.decl(jCodeModel._ref(Property.class), "predicate", jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(uriVar));
                	
                	StringBuilder sb = new StringBuilder();
                	
                	Character previous = null;
                	for(char fieldNameChar : entityName.toCharArray()){
                		if(previous != null){
                			if(Character.isLowerCase(previous) && Character.isUpperCase(fieldNameChar))
                				sb.append("_");
                		}
                		sb.append(Character.toUpperCase(fieldNameChar));
                		previous = fieldNameChar;
                	}
                	
                	JVar ontPropertyVar = methodTryBody.decl(jCodeModel._ref(Property.class), "predicate", jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(ontResource.toString()));
                	
                	JVar jenaModelVar = methodTryBody.decl(jCodeModel._ref(Model.class), "model", jCodeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
                	JVar stmtIteratorVar = methodTryBody.decl(jCodeModel._ref(StmtIterator.class), "stmtIt", 
                			jenaModelVar.invoke("listStatements")
                				.arg(JExpr.cast(jCodeModel._ref(Resource.class), JExpr._super().ref("individual")))
                				.arg(ontPropertyVar)
                				.arg(JExpr.cast(jCodeModel._ref(RDFNode.class), JExpr._null())));
                	
                	JWhileLoop stmtItHasNextWhile = methodTryBody._while(stmtIteratorVar.invoke("hasNext"));
                	JBlock stmtItHasNextWhileBlock = stmtItHasNextWhile.body();
                	JVar stmtVar = stmtItHasNextWhileBlock.decl(jCodeModel._ref(Statement.class), "stmt", stmtIteratorVar.invoke("next"));
                	JVar stmtObjectVar = stmtItHasNextWhileBlock.decl(jCodeModel._ref(RDFNode.class), "object", stmtVar.invoke("getObject"));
                	
                	JDefinedClass rangeClass = range.asJDefinedClass();
                	
                	if(range.getOntResource() != null){
	                	AbstractOntologyCodeClass rangeConcreteClass = ontologyModel.getOntologyClass(range.getOntResource());
	                	
	                	if(rangeConcreteClass == null){
	                		rangeConcreteClass = ontologyModel.createClass(range.getOntResource());
	                		ontologyModel.createClassImplements((AbstractOntologyCodeClassImpl)rangeConcreteClass, ontologyModel.getOntologyInterface(range.getOntResource()));
	                		ontologyModel.getClassMap().put(range.getOntResource(), (OntologyCodeClass)rangeConcreteClass);
	                	}
	                	
	                	JVar retObj = stmtItHasNextWhileBlock.decl(rangeClass, "obj", JExpr._new(rangeConcreteClass.asJDefinedClass()).arg(stmtObjectVar));
	                	stmtItHasNextWhileBlock.add(returnVar.invoke("add").arg(retObj));
	                	
	                	methodBody._return(returnVar);
                	}
                }
            }
            else {
            	if(owner instanceof OntologyCodeClass){

                	JBlock methodBody = jMethod.body();
                	
                	JTryBlock methodTry = methodBody._try();
                	JCatchBlock catchBlock = methodTry._catch(jCodeModel.ref(SecurityException.class));
                	JVar exceptionVar = catchBlock.param("e");
                	catchBlock.body().add(exceptionVar.invoke("printStackTrace"));
                	
                	
                	JBlock methodTryBody = methodTry.body();
                	JDefinedClass objectAnonymous = jCodeModel.anonymousClass(Object.class);
                	
                	/*
                	 * Add the code to get information about the current method
                	 */
                	JExpression methodExpression = JExpr._new(objectAnonymous).invoke("getClass").invoke("getEnclosingMethod");
                	JVar methodVar = methodTryBody.decl(jCodeModel._ref(Method.class), "method", methodExpression);
                	
                	/*
                	 * Add the code to use the annotation ObjectPropertyAnnotation
                	 */
                	JExpression getAnnotation = methodVar.invoke("getAnnotation").arg(JExpr.dotclass(jCodeModel.ref(ObjectPropertyAnnotation.class)));
                	JVar objectPropertyAnnotationVar = methodTryBody.decl(jCodeModel._ref(ObjectPropertyAnnotation.class), "objectPropertyAnnotation", getAnnotation);
                	
                	
                	/*
                	 * Add the code to set a variable for the URI representing the property and the type of the method.
                	 */
                	JExpression uriExpression = objectPropertyAnnotationVar.invoke("uri");
                	JVar uriVar = methodTryBody.decl(jCodeModel._ref(String.class), "propertyUri", uriExpression);
                	JVar ontPropertyVar = methodTryBody.decl(jCodeModel._ref(Property.class), "predicate", jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(uriVar));
                	JVar jenaModelVar = methodTryBody.decl(jCodeModel._ref(Model.class), "model", jCodeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
                	
                	JForEach forEach = methodTryBody.forEach(range.asJDefinedClass(), "object", jMethod.params().get(0));
                	JBlock forEachBlock = forEach.body();
                	
                	forEachBlock.add(jenaModelVar.invoke("add")
	                	.arg(JExpr.cast(jCodeModel._ref(Resource.class), JExpr._super().ref("individual")))
        				.arg(ontPropertyVar)
        				.arg(forEach.var().invoke("getIndividual")));
                }
            }
        }
    }
    
    private void addClassCentricStaticMethod(JCodeModel codeModel, String fieldName){
    	if(this.methodType == OntologyCodeMethodType.Get){
    		OntologyCodeInterface ontInterface = ontologyModel.getOntologyInterface(owner.getOntResource());
    		if(ontInterface != null){
	        	JDefinedClass interfaceClass = ontInterface.asJDefinedClass();
	        	
	        	JFieldVar staticField = interfaceClass.fields().get(fieldName);
	        	
	        	if(staticField != null){
	            	String staticMethodName = fieldName;
	        		staticMethodName = "getBy" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
	        		
	        		
	        		JClass retType = codeModel.ref(Set.class).narrow(interfaceClass);
	        		JClass retTypeImpl = codeModel.ref(HashSet.class).narrow(interfaceClass);
	        		
	        		JMethod staticMethod = interfaceClass.method(JMod.PUBLIC|JMod.STATIC, retType, staticMethodName);
	        		JBlock staticMethodBlock = staticMethod.body();
	        		
	        		JVar retVar = staticMethodBlock.decl(retType, "ret", JExpr._new(retTypeImpl));
	        		
	        		JVar predicateVar = staticMethodBlock.decl(
	        				codeModel.ref(Property.class), 
	        				"predicate",
	        				codeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(staticField));
	        		
	        		JVar modelVar = staticMethodBlock.decl(
	        				codeModel.ref(Model.class), 
	        				"model",
	        				codeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
	        		
	        		JVar stmtItVar = staticMethodBlock.decl(
	        				codeModel.ref(StmtIterator.class), 
	        				"stmtIt",
	        				modelVar.invoke("listStatements").arg(JExpr._null()).arg(predicateVar).arg(JExpr.cast(codeModel._ref(RDFNode.class), JExpr._null())));
	        		
	        		/* 
	        		 * While loop to iterate StmtIterator statements
	        		 */
	        		JWhileLoop whileLoop = staticMethodBlock._while(stmtItVar.invoke("hasNext"));
	        		JBlock whileLoopBlock = whileLoop.body();
	        		JVar stmtVar = whileLoopBlock.decl(
	        				codeModel.ref(Statement.class), 
	        				"stmt",
	        				stmtItVar.invoke("next"));
	        		
	        		JVar subjVar = whileLoopBlock.decl(
	        				codeModel.ref(Resource.class), 
	        				"subj",
	        				stmtVar.invoke("getSubject"));
	        		
	        		AbstractOntologyCodeClassImpl concreteClass = ontologyModel.getOntologyClass(owner.getOntResource());
	        		
	        		System.out.println(concreteClass.asJDefinedClass());
	        		JVar indVar = whileLoopBlock.decl(
	        				owner.asJDefinedClass(), 
	        				"individual",
	        				JExpr._new(concreteClass.asJDefinedClass()).arg(subjVar));
	        		
	        		whileLoopBlock.add(retVar.invoke("add").arg(indVar));
	        		
	        		staticMethodBlock._return(retVar);
	        	}
    		}
    	}
    }
    
    private void addClassCentricStaticMethodWithParam(JCodeModel codeModel, String fieldName){
    	if(this.methodType == OntologyCodeMethodType.Get){
    		OntologyCodeInterface ontInterface = ontologyModel.getOntologyInterface(owner.getOntResource());
    		
    		if(ontInterface != null){
	        	JDefinedClass interfaceClass = ontInterface.asJDefinedClass();
	        	
	        	JFieldVar staticField = interfaceClass.fields().get(fieldName);
	        	
	        	if(staticField != null){
	            	String staticMethodName = fieldName;
	        		staticMethodName = "getBy" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
	        		
	        		
	        		JClass retType = codeModel.ref(Set.class).narrow(interfaceClass);
	        		JClass retTypeImpl = codeModel.ref(HashSet.class).narrow(interfaceClass);
	        		
	        		JMethod staticMethod = interfaceClass.method(JMod.PUBLIC|JMod.STATIC, retType, staticMethodName);
	        		JVar inputParam = staticMethod.param(LizardInterface.class, "value");
	        		
	        		JBlock staticMethodBlock = staticMethod.body();
	        		
	        		JVar retVar = staticMethodBlock.decl(retType, "ret", JExpr._new(retTypeImpl));
	        		
	        		JVar predicateVar = staticMethodBlock.decl(
	        				codeModel.ref(Property.class), 
	        				"predicate",
	        				codeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(staticField));
	        		
	        		JVar modelVar = staticMethodBlock.decl(
	        				codeModel.ref(Model.class), 
	        				"model",
	        				codeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
	        		
	        		JVar stmtItVar = staticMethodBlock.decl(
	        				codeModel.ref(StmtIterator.class), 
	        				"stmtIt",
	        				modelVar.invoke("listStatements").arg(JExpr._null()).arg(predicateVar).arg(inputParam.invoke("getIndividual")));
	        		/* 
	        		 * While loop to iterate StmtIterator statements
	        		 */
	        		JWhileLoop whileLoop = staticMethodBlock._while(stmtItVar.invoke("hasNext"));
	        		JBlock whileLoopBlock = whileLoop.body();
	        		JVar stmtVar = whileLoopBlock.decl(
	        				codeModel.ref(Statement.class), 
	        				"stmt",
	        				stmtItVar.invoke("next"));
	        		
	        		JVar subjVar = whileLoopBlock.decl(
	        				codeModel.ref(Resource.class), 
	        				"subj",
	        				stmtVar.invoke("getSubject"));
	        		
	        		AbstractOntologyCodeClassImpl concreteClass = ontologyModel.getOntologyClass(owner.getOntResource());
	        		
	        		System.out.println(concreteClass.asJDefinedClass());
	        		JVar indVar = whileLoopBlock.decl(
	        				owner.asJDefinedClass(), 
	        				"individual",
	        				JExpr._new(concreteClass.asJDefinedClass()).arg(subjVar));
	        		
	        		whileLoopBlock.add(retVar.invoke("add").arg(indVar));
	        		
	        		staticMethodBlock._return(retVar);
	        	}
	    	}
    	}
    }

}
