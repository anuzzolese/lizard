package it.cnr.istc.stlab.lizard.commons;

import javax.lang.model.SourceVersion;

import org.apache.jena.vocabulary.OWL2;

public class Constants {

    public static final String OWL_NS = OWL2.NS;
    
    public static final String THING = OWL2.Thing.getURI();
    
    public static final String THING_LOCAL_NAME = "Thing";
    
    public static final String INTERFACE_SUFFIX = "I";
    
    public static final String JENA_POSTFIX = "Jena";
    
    public static final String JENA_PACKAGE = "jena";
    
    public static final String BEAN_POSTFIX = "Bean";
    
    public static final String BEAN_PACKAGE = "bean";
    
    public static final String EXTENTIONAL_CLASS_POSTFIX = "Extentional";
    
    public static final String EXTENTIONAL_CLASS_PACKAGE = "extentional";
    
    public static final String FIELD_SUFFIX = "Field";
    
    public static final String UNION_CLASS_SUFFIX = "Union";
    
    public static final String INTERSECTION_CLASS_SUFFIX = "Intersection";
    
    public static final String COMPLEMENT_CLASS_SUFFIX = "Complement";
    
    public static final String ANON_PACKAGE = "anon";
    
    public static final String SPARQL_ARTIFACT = "sparql";
    
    public static String getJavaName(String name){
    	name = name.replace("-", "_");
    	if(!SourceVersion.isName(name)) name = "_" + name;
    	
    	return name;
    }
    
}
