package it.cnr.istc.stlab.lizard.commons.jena;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class RuntimeJenaLizardContext {

	private static String configurationFilePath = "lizard.conf";
	private static JenaLizardContext context;

	public static boolean contextExists(String c) {

		try {
			Properties props = new Properties();
			System.out.println("PROVA context exists");
			InputStream is = new FileInputStream(new File(c));
			props.load(is);
			System.out.println(props.toString());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public static JenaLizardContext getContext() {
		if (context == null) {
			Properties props = new Properties();

			try {

				InputStream is = new FileInputStream(new File(configurationFilePath));
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

			context = JenaLizardContextManager.getInstance().getJenaLizardContext(new JenaLizardConfiguration(props));
		}

		return context;
	}

	public static void resetContext() {
		context = null;
	}

	public static void switchContext(String lizardConfFilepath) {
		context = null;
		configurationFilePath = lizardConfFilepath;
	}

	public static void main(String[] args) {
		RuntimeJenaLizardContext.getContext();
	}

}
