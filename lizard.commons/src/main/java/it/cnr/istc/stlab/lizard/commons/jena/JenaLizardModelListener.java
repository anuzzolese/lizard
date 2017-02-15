package it.cnr.istc.stlab.lizard.commons.jena;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenaLizardModelListener implements ModelChangedListener {

	private static Logger logger = LoggerFactory
			.getLogger(JenaLizardModelListener.class);

	private Model model;
	private String filePath, lang;

	public JenaLizardModelListener(Model m, String filePath, String lang) {
		this.setModel(m);
		this.setFilePath(filePath);
		this.setLang(lang);
	}

	private void updateFileModel() {
		try {
			model.write(new FileOutputStream(new File(filePath)),lang);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addedStatement(Statement arg0) {
		logger.debug("Adding " + arg0);
		updateFileModel();
	}

	@Override
	public void addedStatements(Statement[] arg0) {
		for (int i = 0; i < arg0.length; i++) {
			Statement statement = arg0[i];
			logger.debug("Adding " + statement);
		}
		updateFileModel();
	}

	@Override
	public void addedStatements(List<Statement> arg0) {
		for (Statement statement : arg0) {
			logger.debug("Adding " + statement);
		}
		updateFileModel();
	}

	@Override
	public void addedStatements(StmtIterator arg0) {
		while (arg0.hasNext()) {
			Statement statement = (Statement) arg0.next();
			logger.debug("Adding " + statement);
		}
		updateFileModel();
	}

	@Override
	public void addedStatements(Model arg0) {
		for (Statement statement : arg0.listStatements().toList()) {
			logger.debug("Adding " + statement);
		}
		updateFileModel();
	}

	@Override
	public void notifyEvent(Model arg0, Object arg1) {
		updateFileModel();
	}

	@Override
	public void removedStatement(Statement arg0) {
		logger.debug("Removed " + arg0.toString());
		updateFileModel();

	}

	@Override
	public void removedStatements(Statement[] arg0) {
		for (Statement statement : arg0) {
			logger.debug("Removed " + statement);
		}
		updateFileModel();

	}

	@Override
	public void removedStatements(List<Statement> arg0) {
		for (Statement statement : arg0) {
			logger.debug("Removed " + statement);
		}
		updateFileModel();

	}

	@Override
	public void removedStatements(StmtIterator arg0) {
		while (arg0.hasNext()) {
			Statement statement = (Statement) arg0.next();
			logger.debug("Removed " + statement);
		}
		updateFileModel();

	}

	@Override
	public void removedStatements(Model arg0) {
		for (Statement statement : arg0.listStatements().toList()) {
			logger.debug("Removed " + statement);
		}
		updateFileModel();

	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

}
