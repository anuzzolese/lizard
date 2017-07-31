package it.cnr.istc.stlab.lizard.commons.jena;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RuntimeJenaLizardContext {

	private static String configurationFilePath = "lizard.conf";
	private static JenaLizardContext context;

	public static JenaLizardContext getContext() {
		if (context == null) {
			Properties props = new Properties();

			try {

				InputStream is = new FileInputStream(new File(configurationFilePath));
				props.load(is);

			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

			context = JenaLizardContextManager.getInstance().getJenaLizardContext(new JenaLizardConfiguration(props));
		}

		return context;
	}

	public static void changeContext(JenaLizardConfiguration config) {
		if (context != null) {
			System.err.println("closing model");
			context.getModel().close();
		}
		context = JenaLizardContextManager.getInstance().getJenaLizardContext(config);
	}

	public static void newContext(JenaLizardConfiguration config) {
		File repoFile = new File(config.getModelFilePath());
		if (repoFile.getParentFile() != null) {
			repoFile.getParentFile().mkdirs();
		}
		try {
			repoFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (context != null) {
			System.err.println("closing model");
			context.getModel().close();
		}
		context = JenaLizardContextManager.getInstance().getJenaLizardContext(config);
	}

}
