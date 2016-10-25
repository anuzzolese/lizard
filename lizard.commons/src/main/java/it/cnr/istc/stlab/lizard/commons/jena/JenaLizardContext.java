package it.cnr.istc.stlab.lizard.commons.jena;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

public class JenaLizardContext {
	
	private Model model;
	
	JenaLizardContext(JenaLizardConfiguration conf) {
		RepositoryType repositoryType = conf.getType();

		switch (repositoryType) {
		case Virtuoso:

			System.out.println("Configuration [graph:" + conf.getGraph() + ", Host:"
					+ conf.getVirtuosoHost() + ",Port:" + conf.getVirtuosoPort()
					+ ",User:" + conf.getVirtuosoUser() + ",Password:"
					+ conf.getVirtuosoPassword()+"]");

			String url = "jdbc:virtuoso://" + conf.getVirtuosoHost() + ":"
					+ conf.getVirtuosoPort();
			System.out
					.println(getClass() + " : " + url + " credentials "
							+ conf.getVirtuosoUser() + ":"
							+ conf.getVirtuosoPassword());
			
			model = new VirtModel(new VirtGraph(conf.getGraph(), url,
					conf.getVirtuosoUser(), conf.getVirtuosoPassword()));

			// model = new VirtModel(new VirtGraph(url, conf.getVirtuosoUser(),
			// conf.getVirtuosoPassword()));
			break;

		case TDB:
			model = TDBFactory.createDataset(conf.getTdbLocation())
					.getDefaultModel();
			break;
		default:
			model = ModelFactory.createDefaultModel();
			break;
		}

	}
	
	public Model getModel() {
		return model;
	}
	
	public static void main(String[] args) {
		
		
//		Model model = new VirtModel(new VirtGraph("music", "jdbc:virtuoso://localhost:7777",
//				"dba","dba"));
//		System.out.println(model.size());
//        Query query = QueryFactory.create("SELECT * { <http://dbpedia.org/resource/Classical_music> ?p ?o}");
//        System.out.println("\n\n\n\n"+query.toString());
//        System.out.println(model.size()+"\n\n\n\n");
//        QueryExecution qexec = QueryExecutionFactory.create(query, model);
//        Model m = qexec.execDescribe();
//        System.out.println("Result "+m.size());
		VirtModel vm = new VirtModel(new VirtGraph("music", "jdbc:virtuoso://localhost:7777",
				"dba","dba"));
		System.out.println("Virt Model size: "+vm.size());
		Query query = QueryFactory.create("DESCRIBE <http://dbpedia.org/resource/Classical_music>");
		QueryExecution qexec = VirtuosoQueryExecutionFactory.create(query, vm);
		Model r = qexec.execDescribe();
		System.out.println("Result "+r.size());
	}

}
