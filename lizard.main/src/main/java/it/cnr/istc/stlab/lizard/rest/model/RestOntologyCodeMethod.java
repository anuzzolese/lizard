package it.cnr.istc.stlab.lizard.rest.model;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JWhileLoop;

import it.cnr.istc.stlab.lizard.commons.PrefixRegistry;
import it.cnr.istc.stlab.lizard.commons.annotations.ObjectPropertyAnnotation;
import it.cnr.istc.stlab.lizard.commons.jena.RuntimeJenaLizardContext;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClassImpl;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;
import it.cnr.istc.stlab.lizard.inmemory.model.OntologyAnnotation;

public class RestOntologyCodeMethod extends OntologyCodeMethod {

	
	RestOntologyCodeMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass owner, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range, OntologyCodeModel ontologyModel, JCodeModel codeModel) {
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
        	
        	String methodName;
        	JType responseType = codeModel.ref(Response.class);
        	switch (methodType) {
			case Get:
				methodName = "getBy" + entityName.substring(0,1).toUpperCase() + entityName.substring(1);
				
				jMethod = owner.asJDefinedClass().method(JMod.PUBLIC, responseType, methodName);
				
				if(owner instanceof OntologyCodeClass){
					
					jMethod.annotate(GET.class);
					jMethod.annotate(Path.class).param("value", "/" + entityName);
					jMethod.param(String.class, "constraint").annotate(QueryParam.class).param("value", entityName);
					
					jMethod.body()._return(JExpr._null());
				}
				break;
				
			case Set:
				methodName = "setBy" + entityName.substring(0,1).toUpperCase() + entityName.substring(1);
				
				jMethod = owner.asJDefinedClass().method(JMod.PUBLIC, responseType, methodName);
				if(owner instanceof AbstractOntologyCodeClassImpl){
					
					jMethod.annotate(POST.class);
					jMethod.annotate(Path.class).param("value", "/" + entityName);
					jMethod.param(String.class, "constraint").annotate(FormParam.class).param("value", entityName);
					
					jMethod.body()._return(JExpr._null());
				}
				break;

			default:
				break;
			}
        }
    }
    
    
    
    public void annotate(OntologyAnnotation annotation){
        jMethod.annotate(annotation.asJCode());
    }
    
    @Override
    public int hashCode() {
    	return methodType.hashCode() + super.hashCode();
    }
    
    private void addMethodBodyNew(){
    	JType modelType = jCodeModel.ref(Model.class);
    	if(ontResource == null){
	    	switch (methodType) {
			case Get:
				jMethod = owner.asJDefinedClass().method(1, modelType, entityName);
				
				break;
	
			default:
				break;
			}
    	}
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

}
