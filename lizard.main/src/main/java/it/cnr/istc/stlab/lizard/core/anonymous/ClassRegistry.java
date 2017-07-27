package it.cnr.istc.stlab.lizard.core.anonymous;

/**
 * 
 * @author Andrea Nuzzolese
 *
 */
public interface ClassRegistry {

	boolean contains(String anonymousClassId);

	AnonymousOntologyCodeClass getAnonymousClass(String anonymousClassId);

}
