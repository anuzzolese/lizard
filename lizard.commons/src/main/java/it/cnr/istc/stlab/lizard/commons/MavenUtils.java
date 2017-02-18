package it.cnr.istc.stlab.lizard.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.cnr.istc.stlab.lizard.commons.templates.maven.MavenTemplate;

public class MavenUtils {

	public static void generatePOM(Writer out, Map<String, String> model) {

		MavenTemplate mavenTemplate = new MavenTemplate();
		Template template = mavenTemplate.getTemplate();
		try {
			template.process(model, out);
		} catch (TemplateException | IOException e) {
			e.printStackTrace();
		}

	}

	public static void buildProject(File pom) {

		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(pom);
		request.setGoals(Arrays.asList("clean", "install"));

		String javaHome = System.getProperty("JAVA_HOME");
		if (javaHome != null)
			request.setJavaHome(new File(javaHome));

		Invoker invoker = new DefaultInvoker();
		if (invoker.getMavenHome() == null)
			invoker.setMavenHome(new File(System.getProperty("M2_HOME")));

		try {
			invoker.execute(request);
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}

	}

	public static String detectMvnHome() {
		String mvnHome = null;

		try {
			Process process = Runtime.getRuntime().exec(new String[] { "mvn", "-version" });

			InputStream inputStream = process.getInputStream();

			process.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			boolean stop = false;

			String marker = "Maven home: ";
			while ((line = reader.readLine()) != null && !stop)
				if (line.startsWith(marker))
					mvnHome = line.substring(marker.length()).trim();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		return mvnHome;
	}

}
