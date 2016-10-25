package it.cnr.istc.stlab.lizard.commons.templates;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public abstract class AbstractTemplate implements ITemplate {

    protected Configuration cfg;
    protected Template template;
    protected String templateFolder;
    
    protected AbstractTemplate(String templateFolder){
        
        this.templateFolder = templateFolder;
        cfg = new Configuration();
        
        //String directoryPath = getClass().getClassLoader().getResource(templateFolder).getFile();
        String directoryPath = templateFolder;
        
        TemplateLoader loader = new ClassTemplateLoader(this.getClass(), directoryPath);
        //cfg.setDirectoryForTemplateLoading(new File(directoryPath));
        cfg.setTemplateLoader(loader);
    
        cfg.setDefaultEncoding("UTF-8");
        
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }
    
    @Override
    public Template getTemplate() {
        return template;
    }
    
    @Override
    public String applyTemplate(Map<String,Object> model){
        Writer out = new StringWriter();
        try {
            template.process(model, out);
            return ((StringWriter)out).toString();
        } catch (TemplateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
        
    }
    
}
