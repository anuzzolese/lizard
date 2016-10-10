package it.cnr.istc.stlab.lizard.commons.jena;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.vocabulary.RDF;

import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;
import com.sun.research.ws.wadl.Resource;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;

public class JenaLizardContext {
	
	private Model model;
	
	JenaLizardContext(JenaLizardConfiguration conf){
		RepositoryType repositoryType = conf.getType();
		
		switch (repositoryType) {
		case Virtuoso:
			
			String url = "jdbc:virtuoso://" + conf.getVirtuosoHost() + ":" + conf.getVirtuosoPort();
			System.out.println(getClass() + " : "  + url + " credentials " + conf.getVirtuosoUser() + ":" + conf.getVirtuosoPassword());
			model = new VirtModel(new VirtGraph("http://www.ontologydesignpatterns.org/ont/framenet/abox/", url, conf.getVirtuosoUser(), conf.getVirtuosoPassword()));
			
			//model = new VirtModel(new VirtGraph(url, conf.getVirtuosoUser(), conf.getVirtuosoPassword()));
			break;

		case TDB:
			model = TDBFactory.createDataset(conf.getTdbLocation()).getDefaultModel();
			break;
		default:
			model = ModelFactory.createDefaultModel();
			break;
		}
		
	}
	
	public Model getModel() {
		return model;
	}

}
