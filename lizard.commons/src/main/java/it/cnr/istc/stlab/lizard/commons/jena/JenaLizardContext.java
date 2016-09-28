package it.cnr.istc.stlab.lizard.commons.jena;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;

public class JenaLizardContext {
	
	private Model model;
	
	JenaLizardContext(JenaLizardConfiguration conf){
		String location = conf.getLocation();
		if(location != null){
			if(conf.isTdb()){
				model = TDBFactory.createDataset(location).getDefaultModel();
			}
			
		}
		else model = ModelFactory.createDefaultModel();
	}
	
	public Model getModel() {
		return model;
	}

}
