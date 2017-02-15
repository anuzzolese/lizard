package it.cnr.istc.stlab.lizard.commons.jena;

import java.io.InputStream;
import java.util.Properties;

public class RuntimeJenaLizardContext {

	private static final String CONF_FILE = "lizard.conf";

	private static JenaLizardContext context;

	public static JenaLizardContext getContext() {
		if (context == null) {
			Properties props = new Properties();

			try {

				InputStream is = RuntimeJenaLizardContext.class
						.getClassLoader().getResourceAsStream(CONF_FILE);
				props.load(is);
				

			} catch (Exception e) {
				props.setProperty("type", "virtuoso");
				props.setProperty("user", "dba");
				props.setProperty("password", "dba");
				props.setProperty("host", "localhost");
				props.setProperty("port", "7777");
				props.setProperty("graph", "music");
				System.out.println("Starting with properties for localhost");
			}

			context = JenaLizardContextManager.getInstance()
					.getJenaLizardContext(new JenaLizardConfiguration(props));
		}

		return context;
	}

	public static void main(String[] args) {
		RuntimeJenaLizardContext.getContext();
	}

}
