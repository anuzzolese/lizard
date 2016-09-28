package it.cnr.istc.stlab.lizard.anonymous;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.ontology.OntResource;

import com.sun.codemodel.JCodeModel;

import it.cnr.istc.stlab.lizard.commons.AnonClassType;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;

public class AnonymousClassBuilder {
    
    private static int unionCounter = 0;
    private static int intersectionCounter = 0;
    private static int complementCounter = 0;
    
    private static Map<Integer,UnionClass> unionClasses = new HashMap<Integer,UnionClass>();
    private static Map<Integer,IntersectionClass> intersectionClasses = new HashMap<Integer,IntersectionClass>();
    private static Map<Integer,ComplementClass> complementClasses = new HashMap<Integer,ComplementClass>();
    
    public static BooleanAnonClass build(AnonClassType anonClassType, OntResource ontClass, JCodeModel codeModel, AbstractOntologyCodeClass...members){
    	BooleanAnonClass anonymousClass = null;
    	
    	StringBuilder sb = new StringBuilder();
    	for(AbstractOntologyCodeClass member : members){
    		sb.append(member.getOntResource().toString());
    	}
    	int code = sb.toString().hashCode();
    	
    	switch (anonClassType) {
		case Union:
			
			anonymousClass = unionClasses.get(code);
			if(anonymousClass == null){
				unionCounter += 1;
				anonymousClass = new UnionClass(String.valueOf(unionCounter), ontClass, codeModel, members);
				unionClasses.put(code, (UnionClass) anonymousClass);
			}
			break;
		case Intersection:
			anonymousClass = intersectionClasses.get(code);
			if(anonymousClass == null){
				intersectionCounter += 1;
				anonymousClass = new IntersectionClass(String.valueOf("Intersection") + intersectionCounter, ontClass, codeModel, members);
				intersectionClasses.put(code, (IntersectionClass) anonymousClass);
			}
		case Complement:
			anonymousClass = complementClasses.get(code);
			if(anonymousClass == null){
				complementCounter += 1;
				anonymousClass = new ComplementClass(String.valueOf("Complement") + complementCounter, ontClass, codeModel, members);
				complementClasses.put(code, (ComplementClass) anonymousClass);
			}

		default:
			break;
		}
    	
        return anonymousClass;
    }

}
