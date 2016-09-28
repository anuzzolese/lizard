package it.cnr.istc.stlab.lizard.inmemory.model;

import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.inmemory.InMemoryLizardClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;

public class InMemoryOntologyCodeClass extends OntologyCodeClass {
    
    protected Set<OntClass> superClasses;
    
    protected InMemoryOntologyCodeClass(){
    	super();
    }
    
    InMemoryOntologyCodeClass(OntResource resource, InMemoryOntologyCodeModel ontologyModel, JCodeModel codeModel) throws ClassAlreadyExistsException {
        super(resource, ontologyModel, codeModel);
        
        super.jClass._extends(InMemoryLizardClass.class);
        
        JExpression expression = jCodeModel.ref(ModelFactory.class).staticInvoke("createOntologyModel").invoke("createOntResource").arg(ontResource.getURI());
        
        JMethod constructor = jClass.getConstructor(new JType[]{jClass.owner()._ref(RDFNode.class)});
        JVar param = constructor.listParams()[0];
        constructor.body().invoke("super").arg(param).arg(expression);
        constructor.body().assign(JExpr._super().ref(param), param);
        
        constructor.body().invoke("addInstanceToExtentionalClass");
    }

}
