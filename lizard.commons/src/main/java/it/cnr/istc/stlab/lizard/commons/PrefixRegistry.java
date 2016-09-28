package it.cnr.istc.stlab.lizard.commons;

import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Implementation of a prefix registry based on prefix.cc.
 * 
 * @author Andrea Nuzzolese
 *
 */
public class PrefixRegistry {

	private final String NS_DATA = "prefixcc/prefixcc.ttl";
	
	private final String BLANK_NS_SUFFIX = "ns";
	
	private int blankNsCounter;
	
	private static PrefixRegistry prefixRegistryInstance;
	private Model nsModel;
	
	private PrefixRegistry(){
		InputStream is = getClass().getClassLoader().getResourceAsStream(NS_DATA);
		nsModel = ModelFactory.createDefaultModel();
		nsModel.read(is, null, "TURTLE");
		blankNsCounter = 0;
	}
	
	public static PrefixRegistry getInstance(){
		if(prefixRegistryInstance == null) prefixRegistryInstance = new PrefixRegistry();
		return prefixRegistryInstance;
	}
	
	public String getNsPrefix(String namespace){
		return nsModel.getNsURIPrefix(namespace);
	}
	
	public String getNsURI(String prefix){
		return nsModel.getNsPrefixURI(prefix);
	}
	
	public String createNsPrefix(String namespace){
		blankNsCounter += 1;
		String prefix = BLANK_NS_SUFFIX + blankNsCounter;
		
		nsModel.setNsPrefix(prefix, namespace);
		
		return prefix;
	}
	
}
