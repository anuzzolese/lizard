package it.cnr.istc.stlab.lizard.commons.jena;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

public class JenaLizardContext {

	private Model model;
	private JenaLizardConfiguration conf;

	JenaLizardContext(JenaLizardConfiguration conf) {
		this.conf = conf;
		RepositoryType repositoryType = conf.getType();

		switch (repositoryType) {
		case Virtuoso:

			System.out.println("Configuration [graph:" + conf.getGraph() + ", Host:" + conf.getVirtuosoHost() + ",Port:" + conf.getVirtuosoPort() + ",User:" + conf.getVirtuosoUser() + ",Password:" + conf.getVirtuosoPassword() + "]");

			String url = "jdbc:virtuoso://" + conf.getVirtuosoHost() + ":" + conf.getVirtuosoPort();
			System.out.println(getClass() + " : " + url + " credentials " + conf.getVirtuosoUser() + ":" + conf.getVirtuosoPassword());

			model = new VirtModel(new VirtGraph(conf.getGraph(), url, conf.getVirtuosoUser(), conf.getVirtuosoPassword()));

			// model = new VirtModel(new VirtGraph(url, conf.getVirtuosoUser(),
			// conf.getVirtuosoPassword()));
			break;

		case TDB:
			model = TDBFactory.createDataset(conf.getTdbLocation()).getDefaultModel();
			break;
		case File:
			System.out.println("Configuration for model file [modelFilePath=" + conf.getModelFilePath() + ",lang=" + conf.getLang() + "]");
			model = ModelFactory.createDefaultModel();
			model.register(new JenaLizardModelListener(model, conf.getModelFilePath(), conf.getLang()));
			RDFDataMgr.read(model, conf.getModelFilePath());
			break;
		default:
			model = ModelFactory.createDefaultModel();
			break;
		}

	}

	public Model getModel() {
		return model;
	}

	public QueryExecution createQueryExecution(Query q, Model m) {
		switch (conf.getType()) {
		case File:
			return QueryExecutionFactory.create(q, m);
		case InMemory:
			break;
		case TDB:
			break;
		case Virtuoso:
			return VirtuosoQueryExecutionFactory.create(q, m);
		default:
			break;
		}
		return null;
	}
	

}
