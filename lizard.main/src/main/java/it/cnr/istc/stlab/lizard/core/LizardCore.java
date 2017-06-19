package it.cnr.istc.stlab.lizard.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.writer.FileCodeWriter;

import it.cnr.istc.stlab.lizard.commons.MavenUtils;
import it.cnr.istc.stlab.lizard.commons.OntologyCodeProject;
import it.cnr.istc.stlab.lizard.commons.inmemory.RestInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.recipe.OntologyCodeGenerationRecipe;
import it.cnr.istc.stlab.lizard.core.model.RestOntologyCodeClass;

public class LizardCore {

	private static Logger logger = LoggerFactory.getLogger(LizardCore.class);
	private URI[] uris;
	private String outFolder;
	private boolean isForMarvin = false;
	private static final String configFile = "lizard-core.conf";

	public LizardCore(String outFolder, URI... uris) throws IOException {
		this.uris = uris;
		this.outFolder = outFolder;
		init();
	}

	public LizardCore(String outFolder, boolean isForMarvin, URI... uris) throws IOException {
		this.uris = uris;
		this.outFolder = outFolder;
		this.isForMarvin = isForMarvin;
		init();
	}

	private void init() throws IOException {
		Properties props = new Properties();
		InputStream is = new FileInputStream(new File(configFile));
		props.load(is);
		System.setProperty("M2_HOME", props.getProperty("M2_HOME"));
		System.setProperty("JAVA_HOME", props.getProperty("JAVA_HOME"));
	}

	public static void createServiceAnnotations(File root, OntologyCodeModel ontologyCodeModel) {
		Map<OntResource, RestOntologyCodeClass> restClassMap = ontologyCodeModel.getOntologyClasses(RestOntologyCodeClass.class);
		Collection<RestOntologyCodeClass> restCalasses = restClassMap.values();
		File metaInfFolder = new File(root, "src/main/resources/META-INF/services");
		if (!metaInfFolder.exists())
			metaInfFolder.mkdirs();
		File restInterfaceAnnotation = new File(metaInfFolder, RestInterface.class.getCanonicalName());
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(restInterfaceAnnotation));
			restCalasses.forEach(restClass -> {
				try {
					bw.write(restClass.asJDefinedClass().fullName());
					bw.newLine();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void generate() {

		logger.info("Generating project");

		OntologyCodeGenerationRecipe codegen = new OntologyProjectGenerationRecipe(uris);
		OntologyCodeProject ontologyCodeProject = codegen.generate();

		try {
			File testFolder = new File(outFolder);
			if (testFolder.exists()) {
				logger.info("Folder {} exists", testFolder.getAbsolutePath());
				logger.info("Delete {}", testFolder.getAbsolutePath());
				FileUtils.deleteDirectory(testFolder);
			} else {
				logger.info("not esists");
			}
			File src = new File(outFolder + "/src/main/java");
			File resources = new File(outFolder + "/src/main/resources");
			File test = new File(outFolder + "/src/test/java");
			if (!src.exists())
				src.mkdirs();
			if (!resources.exists())
				resources.mkdirs();
			if (!test.exists())
				test.mkdirs();

			CodeWriter writer = new FileCodeWriter(src, "UTF-8");
			ontologyCodeProject.getOntologyCodeModel().asJCodeModel().build(writer);

			LizardCore.createServiceAnnotations(new File(outFolder), ontologyCodeProject.getOntologyCodeModel());

			File pom = new File(outFolder + "/pom.xml");
			Writer pomWriter = new FileWriter(new File(outFolder + "/pom.xml"));
			Map<String, String> dataModel = new HashMap<String, String>();
			dataModel.put("artifactId", ontologyCodeProject.getArtifactId());
			dataModel.put("groupId", ontologyCodeProject.getGroupId());

			MavenUtils.generatePOM(pomWriter, dataModel, this.isForMarvin);

			if (!this.isForMarvin)
				MavenUtils.buildProject(pom);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws IOException, URISyntaxException {

		String outFolder = "/Users/lgu/Desktop/Lizard/generated-projects/prova-ppdb";
		String[] ontologiesUris = { "http://etna.istc.cnr.it/ppdb/ontology/ppdb.owl", "http://www.ontologydesignpatterns.org/ont/mario/cga.owl" };
		URI[] uris = new URI[ontologiesUris.length];
		for (int i = 0; i < ontologiesUris.length; i++) {
			uris[i] = new URI(ontologiesUris[i]);
		}
		LizardCore lizardCore = new LizardCore(outFolder, uris);

		long t1 = System.currentTimeMillis();
		//OntologyUtils.loadInfModel(uris).write(new FileOutputStream(new File("/Users/lgu/Desktop/b.ttl")), "TTL");
		lizardCore.generate();
		long t2 = System.currentTimeMillis();

		System.out.println("Output folder " + outFolder);
		System.out.println("Project generated in " + (t2 - t1) + "ms");

	}

}
