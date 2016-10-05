package it.cnr.istc.stlab.lizard.core.anonymous;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnonClassRegistry implements ClassRegistry {
    
    private Map<String,AnonymousOntologyCodeClass> classRegistry;
    
    private static AnonClassRegistry unionClassRegistry;
    
    private AnonClassRegistry(){
        
    }
    
    public static AnonClassRegistry getInstance(){
        if(unionClassRegistry == null) unionClassRegistry = new AnonClassRegistry();
        return unionClassRegistry;
    }
    
    @Override
    public boolean contains(String anonymousClassId){
        return classRegistry.containsKey(anonymousClassId);
    }
    
    @Override
    public AnonymousOntologyCodeClass getAnonymousClass(String anonymousClassId){
        return classRegistry.get(anonymousClassId);
    }
    
    public static void main(String[] args) {
        Set<String> s1 = new HashSet<String>();
        s1.add("a");
        s1.add("b");
        s1.add("c");
        
        Set<String> s2 = new HashSet<String>();
        s2.add("c");
        s2.add("b");
        s2.add("a");
        
        System.out.println(s1.hashCode() + " " + s2.hashCode());
    }

}
