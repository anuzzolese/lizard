package it.cnr.istc.stlab.lizard.commons.templates;

import java.util.Map;

import freemarker.template.Template;

public interface ITemplate {

    Template getTemplate();
    String applyTemplate(Map<String,Object> model);
    
}
