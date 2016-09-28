package it.cnr.istc.stlab.lizard.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.lizard.commons.templates.ITemplate;
import it.cnr.istc.stlab.lizard.commons.templates.sparql.SparqlGetTemplate;
import it.cnr.istc.stlab.lizard.commons.templates.sparql.SparqlSetTemplate;

public class SparqlLizardClass extends LizardClass {

    protected SparqlConfig cfg;
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    protected SparqlLizardClass() {
        super();
    }
    
    protected SparqlLizardClass(Resource individual, OntResource classResource, SparqlConfig cfg) {
        super();
        this.cfg = cfg;
    }
    
    public SparqlConfig getCfg() {
        return cfg;
    }
    
    public void setCfg(SparqlConfig cfg) {
        this.cfg = cfg;
    }
    
    protected <T extends LizardInterface> Set<T> get(OntProperty property, Class<T> type){
        ITemplate sparqlTemplate = SparqlGetTemplate.getInstance();
        Map<String,Object> model = new HashMap<String,Object>();
        //model.put("uri", individual.getURI());
        model.put("property", property.getURI());
        
        String sparql = sparqlTemplate.applyTemplate(model);
        Query query = cfg.parse(sparql);
        try {
            sparql = URLEncoder.encode(query.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            sparql = null;
        }
        
        if(sparql != null){
            String requestURI = cfg.getEndpoint() + "?query=" + sparql; 
            System.out.println("Request " + requestURI);
            URLConnection connection;
            try {
                connection = new URL(requestURI).openConnection();
                connection.addRequestProperty("Accept", "application/json");
                InputStream inputStream = connection.getInputStream();
                ResultSet resultSet = ResultSetFactory.fromJSON(inputStream);
                Set<T> objects = new HashSet<T>();
                while (resultSet.hasNext()) {
                    QuerySolution querySolution = resultSet.next();
                    Resource objectResource = querySolution.getResource("object");
                    System.out.println("Cicciovello " + objectResource);
                    T object = null;
                    try {
                        object = type.newInstance();
                    } catch (InstantiationException e) {
                        log.error(e.getMessage(), e);
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        log.error(e.getMessage(), e);
                        e.printStackTrace();
                    }
                    System.out.println(object.getClass());
                    if(object != null && object instanceof SparqlLizardClass){
                        SparqlLizardClass ontologyClass = (SparqlLizardClass) object;
                        ontologyClass.setCfg(cfg);
                        ontologyClass.setClassResource(classResource);
                        ontologyClass.setIndividual(objectResource);
                        objects.add(object);
                    }
                }
                return objects;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return Collections.emptySet();
            } 
            
            
            
        }
        else return Collections.emptySet();
    }
    

    protected <T extends LizardInterface> void set(OntProperty property, Set<T> objects){
        ITemplate sparqlTemplate = SparqlSetTemplate.getInstance();
        Map<String,Object> model = new HashMap<String,Object>();
        //model.put("uri", individual.getURI());
        model.put("property", property.getURI());
        model.put("objects", objects);
        if(cfg.getDataset() != null)
            model.put("dataset", cfg.getDataset().toString());
        else model.put("dataset", null);
        
        String sparql = sparqlTemplate.applyTemplate(model);
        System.out.println(sparql);
        try {
            sparql = URLEncoder.encode(sparql.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            sparql = null;
        }
        
        if(sparql != null){
            String requestURI = cfg.getEndpoint() + "?query=" + sparql; 
            
            URLConnection connection;
            try {
                connection = new URL(requestURI).openConnection();
                connection.getInputStream();
                
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } 
            
            
            
        }
    }
    
    public static void main(String[] args) {
        SparqlLizardClass bsc = new SparqlLizardClass();
        SparqlLizardClass bsc2 = new SparqlLizardClass();
        SparqlLizardClass bsc3 = new SparqlLizardClass();
        SparqlLizardClass bsc4 = new SparqlLizardClass();
        try {
            bsc2.setIndividual(ModelFactory.createDefaultModel().createResource("http://dbpedia.org/resource/Miles_Davis"));
            bsc3.setIndividual(ModelFactory.createDefaultModel().createResource("http://dbpedia.org/resource/Roberto_Baggio"));
            bsc4.setIndividual(ModelFactory.createDefaultModel().createResource("http://dbpedia.org/resource/Franco_Baresi"));
            
            bsc.setIndividual(ModelFactory.createDefaultModel().createResource("http://dbpedia.org/resource/Bob_Marley"));
            //bsc.setCfg(new SparqlConfig(new URI("http://dbpedia.org/sparql"), new URI("ciccio")));
            bsc.setCfg(new SparqlConfig(new URI("http://dbpedia.org/sparql"), null));
            
            Set<LizardInterface> classes = new HashSet<LizardInterface>();
            classes.add(bsc2);
            classes.add(bsc3);
            classes.add(bsc4);
            
            bsc.set(ModelFactory.createOntologyModel().createOntProperty("http://dbpedia.org/ontology/prop"), classes);
            
            Set<SparqlLizardClass> inds = bsc.get(ModelFactory.createOntologyModel().createOntProperty("http://dbpedia.org/ontology/birthPlace"), SparqlLizardClass.class);
            System.out.println("INDS size " + inds.size());
            for(LizardInterface ind : inds) System.out.println("IND " + ind.getIndividual());
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
}
