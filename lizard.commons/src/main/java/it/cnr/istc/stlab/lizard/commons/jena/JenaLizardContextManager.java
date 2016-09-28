package it.cnr.istc.stlab.lizard.commons.jena;

import java.util.HashMap;
import java.util.Map;


public class JenaLizardContextManager {
	
	private static JenaLizardContextManager instance;  
	
	private Map<JenaLizardConfiguration, JenaLizardContext> contextInstances;
	
	
	private JenaLizardContextManager() {
		this.contextInstances = new HashMap<JenaLizardConfiguration, JenaLizardContext>();
	}
	
	public static JenaLizardContextManager getInstance(){
		if(instance == null) instance = new JenaLizardContextManager();
		return instance;
	}
	
	public JenaLizardContext getJenaLizardContext(JenaLizardConfiguration conf){
		JenaLizardContext ctx = contextInstances.get(conf);
		if(ctx == null) {
			ctx = new JenaLizardContext(conf);
			contextInstances.put(conf, ctx);
		}
		return ctx;
	}

}
