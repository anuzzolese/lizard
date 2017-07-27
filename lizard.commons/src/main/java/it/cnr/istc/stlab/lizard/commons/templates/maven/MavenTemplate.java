package it.cnr.istc.stlab.lizard.commons.templates.maven;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.cnr.istc.stlab.lizard.commons.templates.AbstractTemplate;
import it.cnr.istc.stlab.lizard.commons.templates.ITemplate;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class MavenTemplate extends AbstractTemplate {

	protected static final String TEMPLATE_FOLDER = "/templates/maven";

	public MavenTemplate(boolean marvin) {
		super(TEMPLATE_FOLDER);
		try {
			if (marvin)
				template = cfg.getTemplate("pom_marvin.ftl");
			else
				template = cfg.getTemplate("pom.ftl");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ITemplate iTemplate = new MavenTemplate(false);
		Template template = iTemplate.getTemplate();
		Map<String, Object> dataModel = new HashMap<String, Object>();
		dataModel.put("groupId", "test");
		dataModel.put("artifactId", "test");
		try {
			template.process(dataModel, new OutputStreamWriter(System.out));
		} catch (TemplateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
