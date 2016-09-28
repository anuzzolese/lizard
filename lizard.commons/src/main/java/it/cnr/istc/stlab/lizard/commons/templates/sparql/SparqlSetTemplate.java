package it.cnr.istc.stlab.lizard.commons.templates.sparql;

import it.cnr.istc.stlab.lizard.commons.templates.AbstractTemplate;

import java.io.IOException;

public class SparqlSetTemplate extends AbstractTemplate {

    
    private static SparqlSetTemplate sparqlGetTemplate;
    
    private SparqlSetTemplate(){
        super(SparqlTemplateConfig.TEMPLATE_FOLDER);
        
        try {
            super.template = cfg.getTemplate("set.ftl");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static SparqlSetTemplate getInstance(){
        if(sparqlGetTemplate == null) sparqlGetTemplate = new SparqlSetTemplate();
        return sparqlGetTemplate;
    }
    
}
