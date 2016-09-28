package it.cnr.istc.stlab.lizard.anonymous;


/**
 * 
 * @author Andrea Nuzzolese
 *
 */
public interface ClassRegistry {
    
    boolean contains(String anonymousClassId);
    AnonymousOntologyCodeClass getAnonymousClass(String anonymousClassId);

}
