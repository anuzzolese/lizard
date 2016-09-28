package it.cnr.istc.stlab.lizard.commons;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;

public class SparqlConfig {

    private URI endpoint, dataset;
    
    public SparqlConfig(URI endpoint, URI dataset) {
        this.endpoint = endpoint;
        this.dataset = dataset;
    }
    
    public URI getDataset() {
        return dataset;
    }
    
    public URI getEndpoint() {
        return endpoint;
    }
    
    public Query parse(String sparql){
        Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
        if(dataset != null) query.addGraphURI(dataset.toString());
        return query;
    }
    
    
    public static void main(String[] args) {
        String sparql = "INSERT DATA {<http://www.ontologydesignpatterns.org/cp/owl/timeinterval.owl#cicico> <http://www.ontologydesignpatterns.org/cp/owl/timeinterval.owl#hasIntervalDate> \"22\"}";
        try {
            SparqlConfig cfg = new SparqlConfig(new URI("http://dbpedia.org/sparql"), new URI("default_bla"));
            Query query = cfg.parse(sparql);
            System.out.println(query.toString());
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
