package it.cnr.istc.stlab.lizard.commons.jena;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

public class JenaLizardContext {

	private Model model;
	private JenaLizardConfiguration conf;

	public JenaLizardConfiguration getConf() {
		return conf;
	}

	JenaLizardContext(JenaLizardConfiguration conf) {
		this.conf = conf;
		init();
	}

	private void init() {
		RepositoryType repositoryType = conf.getType();

		System.out.println(conf.toString());

		switch (repositoryType) {
		case Virtuoso:
			String url = "jdbc:virtuoso://" + conf.getVirtuosoHost() + ":" + conf.getVirtuosoPort();
			model = new VirtModel(new VirtGraph(conf.getGraph(), url, conf.getVirtuosoUser(), conf.getVirtuosoPassword()));
			break;
		case TDB:
			model = TDBFactory.createDataset(conf.getTdbLocation()).getDefaultModel();
			break;
		case File:
			if (!conf.getInference()) {
				model = ModelFactory.createDefaultModel();
				RDFDataMgr.read(model  , conf.getModelFilePath());
				model.register(new JenaLizardModelListener(model, conf.getModelFilePath(), conf.getLang()));
			} else {
				OntDocumentManager odm = new OntDocumentManager(conf.getJena_doc_manager());
				OntModelSpec oms = OntModelSpec.OWL_MEM;
				oms.setDocumentManager(odm);
				OntModel om = ModelFactory.createOntologyModel(oms);
				System.out.println("Loading ontologies with language " + oms.getLanguage() + " ");

				List<String> files;
				try {
					files = getLines(conf.getOntologies_file());
					for (String file : files) {
						System.out.println("Loading ontology " + file);
						om.read(file);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("Ontology loaded " + om.size());

				Model data = ModelFactory.createDefaultModel();
				RDFDataMgr.read(data, conf.getModelFilePath());

				System.out.println("Model loaded");

				Reasoner resasoner = ReasonerRegistry.getRDFSSimpleReasoner();
				InfModel infModel = ModelFactory.createInfModel(resasoner, om, data);
				System.out.println("Inf model created, Reasoner: " + resasoner.getClass().getSimpleName());

				model = infModel;
				infModel.register(new JenaLizardModelListener(data, conf.getModelFilePath(), conf.getLang()));
			}

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

	private static List<String> getLines(String filename) throws IOException {
		List<String> result = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		while (line != null) {
			result.add(line);
			line = br.readLine();
		}
		br.close();
		return result;
	}

}
