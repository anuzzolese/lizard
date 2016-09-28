package it.cnr.istc.stlab.lizard.commons;

import org.apache.jena.ontology.OntResource;

public class NoURIClassException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -437709415704757176L;
    
    private OntResource ontResource;
    
    public NoURIClassException(OntResource ontResource) {
        this.ontResource = ontResource;
    }
    
    @Override
    public String getMessage() {
        return "The OWL entity " + ontResource + " is anonymous.";
    }

}
