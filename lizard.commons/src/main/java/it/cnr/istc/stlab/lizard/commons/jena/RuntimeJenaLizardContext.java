package it.cnr.istc.stlab.lizard.commons.jena;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RuntimeJenaLizardContext {
	
	private static final String CONF_FILE = "lizard.conf";
	
	private static JenaLizardContext context;
	
	public static JenaLizardContext getContext(){
		if(context != null){
			Properties props = new Properties();
			
			InputStream is = RuntimeJenaLizardContext.class.getClassLoader().getResourceAsStream(CONF_FILE);
			try {
				props.load(is);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			context = JenaLizardContextManager.getInstance().getJenaLizardContext(new JenaLizardConfiguration(props));
		}
		
		return context;
	}

}
