package it.cnr.istc.stlab.lizard.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class LizardConfiguration {

	private static final String configFile = "lizard-core.conf";
	private static LizardConfiguration instance;
	private String M2_HOME, JAVA_HOME, apiVersion, contactName, contanctEmail, licenseName, licenseUrl, host;

	private LizardConfiguration() {

	}

	public static LizardConfiguration getInstance() {
		if (instance == null) {
			Properties props = new Properties();
			InputStream is;
			try {
				is = new FileInputStream(new File(configFile));
				props.load(is);
				instance = new LizardConfiguration();
				instance.M2_HOME = props.getProperty("M2_HOME");
				instance.JAVA_HOME = props.getProperty("JAVA_HOME");
				instance.apiVersion = props.getProperty("apiVersion");
				instance.contactName = props.getProperty("contactName");
				instance.contanctEmail = props.getProperty("contanctEmail");
				instance.licenseName = props.getProperty("licenseName");
				instance.licenseUrl = props.getProperty("licenseUrl");
				instance.host = props.getProperty("host");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public String getM2_HOME() {
		return M2_HOME;
	}

	public String getJAVA_HOME() {
		return JAVA_HOME;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public String getContactName() {
		return contactName;
	}

	public String getContanctEmail() {
		return contanctEmail;
	}

	public String getLicenseName() {
		return licenseName;
	}

	public String getLicenseUrl() {
		return licenseUrl;
	}

	public String getHost() {
		return host;
	}

}
