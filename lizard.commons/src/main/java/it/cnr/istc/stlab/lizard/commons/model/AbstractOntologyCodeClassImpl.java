package it.cnr.istc.stlab.lizard.commons.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import it.cnr.istc.stlab.lizard.commons.annotations.BelongsTo;

public abstract class AbstractOntologyCodeClassImpl extends AbstractOntologyCodeClass {

    protected Set<OntologyCodeInterface> implementedClasses;
    protected Set<ExtentionalOntologyCodeClass> extentionalOntologyCodeClasses;
    
    
    AbstractOntologyCodeClassImpl(){
    	super();
    	implementedClasses = new HashSet<OntologyCodeInterface>();
        extentionalOntologyCodeClasses = new HashSet<ExtentionalOntologyCodeClass>();
    }
    
    AbstractOntologyCodeClassImpl(OntResource ontResource, OntologyCodeModel ontologyModel, JCodeModel jCodeModel) {
        super(ontResource, ontologyModel, jCodeModel);
        implementedClasses = new HashSet<OntologyCodeInterface>();
        extentionalOntologyCodeClasses = new HashSet<ExtentionalOntologyCodeClass>();
    }
    
    public void implementsInterfaces(OntologyCodeInterface...ontologyInterfaces){
        if(ontologyInterfaces != null){
            for(OntologyCodeInterface ontologyInterface : ontologyInterfaces){
            	System.out.println("Interface " + ontologyInterface);
            	System.out.println("Implemented classes " + implementedClasses);
                implementedClasses.add(ontologyInterface);
            }
        }
    }
    
    public void extendsClasses(AbstractOntologyCodeClass oClass){
        if(oClass != null && oClass instanceof AbstractOntologyCodeClassImpl)
            this.extendedClass = oClass;
    }
    
    public Set<OntologyCodeInterface> getImplementedClasses() {
        return implementedClasses;
    }


    public Set<AbstractOntologyCodeClass> listSuperClasses(){
        Set<AbstractOntologyCodeClass> superClasses = new HashSet<AbstractOntologyCodeClass>();
        superClasses.addAll(implementedClasses);
        superClasses.add(extendedClass);
        return superClasses;
    }
    
    
    public void addExtentionalOntologyCodeClasses(ExtentionalOntologyCodeClass extentionalOntologyCodeClass) {
    	if(jClass instanceof JDefinedClass){
	    	JAnnotationUse annotation = ((JDefinedClass)jClass).annotate(jCodeModel.ref(BelongsTo.class));
	    	annotation.paramArray("extentionalClasses").param(extentionalOntologyCodeClass.asJDefinedClass());
    	}
	}    

}
