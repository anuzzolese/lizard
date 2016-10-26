package it.cnr.istc.stlab.lizard.core.model;

import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.LizardClass;
import it.cnr.istc.stlab.lizard.commons.annotations.OntologyClass;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.inmemory.InMemoryLizardClass;
import it.cnr.istc.stlab.lizard.commons.jena.RuntimeJenaLizardContext;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;

public class JenaOntologyCodeClass extends OntologyCodeClass {
    
    protected Set<OntClass> superClasses;
    
    protected JenaOntologyCodeClass(){
    	super();
    }
    
    JenaOntologyCodeClass(OntResource resource, OntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
        super(resource, ontologyModel, codeModel);
        
        init(resource);
        addStaticReferencerMethodInInterface();
        
        ((JDefinedClass)super.jClass)._extends(InMemoryLizardClass.class);
        
        JExpression expression = jCodeModel.ref(ModelFactory.class).staticInvoke("createOntologyModel").invoke("createOntResource").arg(ontResource.getURI());
        
        JMethod constructor = ((JDefinedClass)super.jClass).getConstructor(new JType[]{jClass.owner()._ref(RDFNode.class)});
        JVar param = constructor.listParams()[0];
        JBlock constructorBody = constructor.body();
        constructorBody.invoke("super").arg(param).arg(expression);
        
        JVar jenaModelVar = constructorBody.decl(jCodeModel._ref(Model.class), "model", jCodeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
        JBlock ifThenBlock = constructorBody._if(jenaModelVar.invoke("contains")
        				.arg(JExpr.cast(jCodeModel._ref(Resource.class), JExpr._super().ref("individual")))
        				.arg(jCodeModel.ref(RDF.class).staticRef("type"))
        				.arg(expression).not())._then();
        
        JInvocation invocation = jenaModelVar.invoke("add")
        				.arg(JExpr.cast(jCodeModel._ref(Resource.class), JExpr._super().ref("individual")))
        				.arg(jCodeModel.ref(RDF.class).staticRef("type"))
        				.arg(expression);
        ifThenBlock.add(invocation);
        
        
        
        
        
        //constructorBody.add(invocation);
        
        
    }
    
    private void init(OntResource resource) throws ClassAlreadyExistsException {
        super.ontologyClassType = OntologyCodeClassType.Class;
        
        if(resource.isURIResource()){
            String artifactId = packageName + "." + Constants.JENA_PACKAGE + ".";
            
            String localName = Constants.getJavaName(resource.getLocalName());
            
            super.entityName = artifactId + localName + Constants.JENA_POSTFIX;
            try {
            	super.jClass = jCodeModel._class(entityName, ClassType.CLASS);
                
                /*
                 * Create the constructor that allows to instantiate individuals.
                 */
            	JMethod constructor = ((JDefinedClass)super.jClass).constructor(1);
                constructor.param(RDFNode.class, "individual");
                
                ((JDefinedClass)super.jClass)._extends(LizardClass.class);
                
                
                JAnnotationUse annotation = ((JDefinedClass)super.jClass).annotate(OntologyClass.class);
                annotation.param("uri", ontResource.getURI());
                
                JMethod setIdMethod = ((JDefinedClass)super.jClass).method(JMod.PUBLIC, jCodeModel.VOID , "setId");
                setIdMethod.param(String.class, "id");
                setIdMethod.body().directStatement("throw new UnsupportedOperationException(\"Unsupported Operation!\");");
                
                JMethod getIdMethod = ((JDefinedClass)super.jClass).method(JMod.PUBLIC, String.class, "getId");
                getIdMethod.body().directStatement("return super.individual.asResource().getURI();");
                
                JMethod setIsCompletedMethod =  ((JDefinedClass)super.jClass).method(JMod.PUBLIC, jCodeModel.VOID, "setIsCompleted");
                setIsCompletedMethod.param(Boolean.class, "isCompletedMethod");
                setIsCompletedMethod.body().directStatement("throw new UnsupportedOperationException(\"Unsupported Operation!\");");
                
                JMethod getIsCompletedMethod = ((JDefinedClass)super.jClass).method(JMod.PUBLIC, Boolean.class, "getIsCompleted");
                getIsCompletedMethod.body().directStatement("return true;");
                
            } catch (JClassAlreadyExistsException e) {
                throw new ClassAlreadyExistsException(ontResource);
            }
        }
    }
    
    private void addStaticReferencerMethodInInterface(){
    	AbstractOntologyCodeClass interfaceClass = ontologyModel.getOntologyClass(ontResource, BeanOntologyCodeInterface.class);
    	JMethod getMethod = ((JDefinedClass)interfaceClass.asJDefinedClass()).method(JMod.PUBLIC|JMod.STATIC, interfaceClass.asJDefinedClass(), "get");
        JVar param = getMethod.param(String.class, "entityURI");
        JBlock methodBlock = getMethod.body();
        
        JVar retEntity = methodBlock.decl(interfaceClass.asJDefinedClass(), "_entity", JExpr._null());
        JExpression resourceExpr = jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createResource").arg(param);
        JExpression rdfTypeExpr = jCodeModel.ref(RDF.class).staticRef("type");
        JExpression owlTypeExpr = jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createResource").arg(ontResource.toString());
        
        JExpression modelExpr = jCodeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel");
        JVar modelVar = methodBlock.decl(jCodeModel._ref(Model.class), "model", modelExpr);
        
        JConditional ifBlock = methodBlock._if(modelVar.invoke("contains").arg(resourceExpr).arg(rdfTypeExpr).arg(owlTypeExpr));
        JBlock ifThenBlock = ifBlock._then();
        ifThenBlock.assign(retEntity, JExpr._new(super.jClass).arg(resourceExpr));
        
        methodBlock._return(retEntity);
    }

}
