package it.cnr.istc.stlab.lizard.commons.templates.sparql;

import it.cnr.istc.stlab.lizard.commons.templates.AbstractTemplate;

import java.io.IOException;

public class SparqlGetTemplate extends AbstractTemplate {

    
    private static SparqlGetTemplate sparqlGetTemplate;
    
    private SparqlGetTemplate(){
        super(SparqlTemplateConfig.TEMPLATE_FOLDER);
        
        try {
            super.template = cfg.getTemplate("get.ftl");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static SparqlGetTemplate getInstance(){
        if(sparqlGetTemplate == null) sparqlGetTemplate = new SparqlGetTemplate();
        return sparqlGetTemplate;
    }
    
}
