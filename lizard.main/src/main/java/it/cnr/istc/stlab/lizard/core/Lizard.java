package it.cnr.istc.stlab.lizard.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.jena.riot.web.HttpOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.writer.FileCodeWriter;

import it.cnr.istc.stlab.lizard.commons.MavenUtils;
import it.cnr.istc.stlab.lizard.commons.OntologyCodeProject;
import it.cnr.istc.stlab.lizard.commons.recipe.OntologyCodeGenerationRecipe;

public class Lizard {

	private static Logger logger = LoggerFactory.getLogger(Lizard.class);
	public final static String BUILD = "b";
	public final static String BUILD_LONG = "build";
	public final static String CONFIGURATION_FILE = "c";
	public final static String CONFIGURATION_FILE_LONG = "config";
	public final static String MARVIN = "m";
	public final static String MARVIN_LONG = "marvin";
	public final static String CLEAR = "clear";
	public final static String CLEAR_LONG = "clear_folder";
	public final static String OUTPUT_FOLDER = "o";
	public final static String OUTPUT_FOLDER_LONG = "output";
	public final static String ONTDOCMANAGER = "d";
	public final static String ONTDOCMANAGER_LONG = "ontdocmanager";
	private boolean isForMarvin = false;

	private String outFolder;
	private boolean clearOutputFolder = false, generateSwagger = false;
	private URI[] uris;

	public Lizard(String outFolder, boolean isForMarvin, URI... uris) throws IOException {
		this.uris = uris;
		this.outFolder = outFolder;
		this.isForMarvin = isForMarvin;
		init();
	}

	public Lizard(String outFolder, URI... uris) throws IOException {
		this(outFolder, false, uris);
	}

	private void generateOntologiesFile() throws MalformedURLException, IOException {
		FileOutputStream fos = new FileOutputStream(new File(outFolder + "/ontologies"));
		for (URI u : uris) {
			fos.write(u.toURL().toString().getBytes());
			fos.write('\n');
		}
		fos.flush();
		fos.close();
	}

	public void setClearOutputFolder(boolean c) {
		this.clearOutputFolder = c;
	}

	public void generateProject(boolean buildProject, String fileOntDocumentManager, String groupId, String artifactId,
			String versionId) {

		logger.info("Generating project");

		OntologyCodeGenerationRecipe codegen = new OntologyProjectGenerationRecipe(fileOntDocumentManager, uris);

		OntologyCodeProject ontologyCodeProject = codegen.generate();

		try {
			File testFolder = new File(outFolder);
			if (testFolder.exists() && clearOutputFolder) {
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

			if (generateSwagger) {
				codegen.generateSwaggerDescription(outFolder + "/swagger");
				generateOntologiesFile();
			}

			File pom = new File(outFolder + "/pom.xml");
			Writer pomWriter = new FileWriter(new File(outFolder + "/pom.xml"));
			Map<String, String> dataModel = new HashMap<String, String>();
			dataModel.put("artifactId", artifactId);
			dataModel.put("groupId", groupId);
			dataModel.put("versionId", versionId);

			MavenUtils.generatePOM(pomWriter, dataModel, this.isForMarvin);

			if (buildProject && !this.isForMarvin)
				MavenUtils.buildProject(pom);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void init() throws IOException {
		System.setProperty("M2_HOME", LizardConfiguration.getInstance().getM2_HOME());
		System.setProperty("JAVA_HOME", LizardConfiguration.getInstance().getJAVA_HOME());
	}

	public static void main(String[] args) throws IOException, URISyntaxException {

		Options options = new Options();

		Builder optionBuilder = Option.builder(CONFIGURATION_FILE);
		Option configurationFileOption = optionBuilder.argName("file").hasArg().required(true)
				.desc("MANDATORY - Input file containing the app configuration.").longOpt(CONFIGURATION_FILE_LONG)
				.build();
		options.addOption(configurationFileOption);

		{
			optionBuilder = Option.builder(OUTPUT_FOLDER);
			Option outputFileOption = optionBuilder.argName("folder").hasArg().required(true)
					.desc("MANDATORY - Output directory used to store the generated project.")
					.longOpt(OUTPUT_FOLDER_LONG).build();
			options.addOption(outputFileOption);
		}

		{
			optionBuilder = Option.builder(ONTDOCMANAGER);
			Option outputFileOption = optionBuilder.argName("odm").hasArg().required(false)
					.desc("A filepath to the policy file for the Jena OntDocumentManager.").longOpt(ONTDOCMANAGER_LONG)
					.build();
			options.addOption(outputFileOption);
		}

		{
			optionBuilder = Option.builder(BUILD);
			Option buildOption = optionBuilder.argName("build").desc("Build the project using maven.")
					.longOpt(BUILD_LONG).build();
			options.addOption(buildOption);
		}

		{
			optionBuilder = Option.builder(MARVIN);
			Option buildOption = optionBuilder.argName("marvin").desc("Generate the project for Marvin platform.")
					.longOpt(MARVIN_LONG).build();
			options.addOption(buildOption);
		}

		{
			optionBuilder = Option.builder(CLEAR);
			Option buildOption = optionBuilder.argName("clear_folder").desc("Clear the output folder.")
					.longOpt(CLEAR_LONG).build();
			options.addOption(buildOption);
		}

		CommandLine commandLine = null;

		CommandLineParser cmdLineParser = new DefaultParser();
		try {
			commandLine = cmdLineParser.parse(options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("process", options);
		}

		if (commandLine != null) {

			HttpOp.setDefaultHttpClient(
					HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build());

			String configuration = commandLine.getOptionValue(CONFIGURATION_FILE);
			LizardConfiguration.setConfigFile(configuration);
			LizardConfiguration lizardConfiguration = LizardConfiguration.getInstance();
			String outputFolder = commandLine.getOptionValue(OUTPUT_FOLDER);
			String ontDocManager = null;
			if (commandLine.hasOption(ONTDOCMANAGER)) {
				ontDocManager = commandLine.getOptionValue(ONTDOCMANAGER);
			}
			boolean build = commandLine.hasOption(BUILD);
			boolean marvin = commandLine.hasOption(MARVIN);
			boolean clear = commandLine.hasOption(CLEAR);

			URI[] uris = new URI[lizardConfiguration.getOntologies().length];
			for (int i = 0; i < lizardConfiguration.getOntologies().length; i++) {
				uris[i] = new URI(lizardConfiguration.getOntologies()[i]);
			}
			Lizard lizard = new Lizard(outputFolder, marvin, uris);
			lizard.generateSwagger = lizardConfiguration.getGenerateSwagger();

			if (clear) {
				System.out.println("Clear output folder");
				lizard.setClearOutputFolder(true);
			}

			long t1 = System.currentTimeMillis();
			lizard.generateProject(build, ontDocManager, lizardConfiguration.getGroupId(),
					lizardConfiguration.getArtifactId(), lizardConfiguration.getVersionId());
			long t2 = System.currentTimeMillis();

			System.out.println("Output folder " + outputFolder);
			System.out.println("Project generated in " + (t2 - t1) + "ms");

		}
	}

}
