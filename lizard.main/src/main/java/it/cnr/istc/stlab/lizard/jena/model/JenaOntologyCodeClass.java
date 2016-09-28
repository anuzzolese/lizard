package it.cnr.istc.stlab.lizard.jena.model;

import java.util.Set;

import javax.lang.model.SourceVersion;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.LizardClass;
import it.cnr.istc.stlab.lizard.commons.annotations.OntologyClass;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.inmemory.InMemoryLizardClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;

public class JenaOntologyCodeClass extends OntologyCodeClass {
    
    protected Set<OntClass> superClasses;
    
    protected JenaOntologyCodeClass(){
    	super();
    }
    
    JenaOntologyCodeClass(OntResource resource, JenaOntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
        super(resource, ontologyModel, codeModel);
        
        init(resource);
        
        super.jClass._extends(InMemoryLizardClass.class);
        
        JExpression expression = jCodeModel.ref(ModelFactory.class).staticInvoke("createOntologyModel").invoke("createOntResource").arg(ontResource.getURI());
        
        JMethod constructor = jClass.getConstructor(new JType[]{jClass.owner()._ref(RDFNode.class)});
        JVar param = constructor.listParams()[0];
        constructor.body().invoke("super").arg(param).arg(expression);
        constructor.body().assign(JExpr._super().ref(param), param);
        
        constructor.body().invoke("addInstanceToExtentionalClass");
    }
    
    private void init(OntResource resource) throws ClassAlreadyExistsException {
        super.ontologyClassType = OntologyCodeClassType.Class;
        
        if(resource.isURIResource()){
            String artifactId = packageName + "." + Constants.IMPLEMENTATION_PACKAGE + ".";
            
            String localName = resource.getLocalName();
            if(!SourceVersion.isName(localName)) localName = "_" + localName;
            
            super.entityName = artifactId + localName + Constants.IMPLEMENTATION_POSTFIX;
            try {
            	super.jClass = jCodeModel._class(entityName, ClassType.CLASS);
                
                /*
                 * Create the constructor that allows to instantiate individuals.
                 */
                JMethod constructor = super.jClass.constructor(1);
                constructor.param(RDFNode.class, "individual");
                
                super.jClass._extends(LizardClass.class);
                
                JAnnotationUse annotation = super.jClass.annotate(OntologyClass.class);
                annotation.param("uri", ontResource.getURI());
                
            } catch (JClassAlreadyExistsException e) {
                throw new ClassAlreadyExistsException(ontResource);
            }
        }
    }

}
