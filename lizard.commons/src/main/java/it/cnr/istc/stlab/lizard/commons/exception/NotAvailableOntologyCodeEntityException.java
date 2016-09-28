package it.cnr.istc.stlab.lizard.commons.exception;

import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeEntity;

public class NotAvailableOntologyCodeEntityException extends Exception {
    
    /**
     * 
     */
    private static final long serialVersionUID = 731368640221349053L;
    
    private Class<? extends OntologyCodeEntity> ontologyCodeEntityClass;
    
    public <T extends OntologyCodeEntity> NotAvailableOntologyCodeEntityException(Class<T> ontologyClass) {
        this.ontologyCodeEntityClass = (Class<? extends OntologyCodeEntity>) ontologyClass;
    }
    
    @Override
    public String getMessage() {
        return "Not available ontology code entity for class " + ontologyCodeEntityClass.getCanonicalName();
    }

}
