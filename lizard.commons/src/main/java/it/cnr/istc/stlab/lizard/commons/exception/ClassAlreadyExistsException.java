package it.cnr.istc.stlab.lizard.commons.exception;

import org.apache.jena.ontology.OntResource;

public class ClassAlreadyExistsException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 3831689534372366817L;
    
    private OntResource resource;
    
    public ClassAlreadyExistsException(OntResource resource) {
        this.resource = resource;
    }
    
    @Override
    public String getMessage() {
        return "A class for the resource " + resource + " already exists.";
    }

}
