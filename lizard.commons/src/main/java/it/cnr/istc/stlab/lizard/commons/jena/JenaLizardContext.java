package it.cnr.istc.stlab.lizard.commons.jena;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;

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

}
