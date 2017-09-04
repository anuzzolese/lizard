package it.cnr.istc.stlab.lizard.commons.jena;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenaLizardModelListener implements ModelChangedListener {

	private static Logger logger = LoggerFactory.getLogger(JenaLizardModelListener.class);

	private Model model;
	private String filePath, lang;

	public JenaLizardModelListener(Model m, String filePath, String lang) {
		Model mod = ModelFactory.createDefaultModel();
		mod.add(m);
		this.setModel(mod);
		this.setFilePath(filePath);
		this.setLang(lang);
	}

	private void updateFileModel() {
		Thread t = new Thread(new Saver(model, this.filePath, this.lang));
		t.start();
	}

	@Override
	public void addedStatement(Statement arg0) {
		logger.debug("Adding " + arg0);
		try {
			model.enterCriticalSection(Lock.WRITE);
			model.add(arg0);
		} finally {
			model.leaveCriticalSection();
		}
		updateFileModel();
	}

	@Override
	public void addedStatements(Statement[] arg0) {
		for (int i = 0; i < arg0.length; i++) {
			Statement statement = arg0[i];
			try {
				model.enterCriticalSection(Lock.WRITE);
				model.add(statement);
			} finally {
				model.leaveCriticalSection();
			}
			logger.debug("Adding " + statement);
		}
		updateFileModel();
	}

	@Override
	public void addedStatements(List<Statement> arg0) {
		for (Statement statement : arg0) {
			logger.debug("Adding " + statement);
			try {
				model.enterCriticalSection(Lock.WRITE);
				model.add(statement);
			} finally {
				model.leaveCriticalSection();
			}
		}
		updateFileModel();
	}

	@Override
	public void addedStatements(StmtIterator arg0) {
		while (arg0.hasNext()) {
			Statement statement = (Statement) arg0.next();
			try {
				model.enterCriticalSection(Lock.WRITE);
				model.add(statement);
			} finally {
				model.leaveCriticalSection();
			}
			logger.debug("Adding " + statement);
		}
		updateFileModel();
	}

	@Override
	public void addedStatements(Model arg0) {
		for (Statement statement : arg0.listStatements().toList()) {
			try {
				model.enterCriticalSection(Lock.WRITE);
				model.add(statement);
			} finally {
				model.leaveCriticalSection();
			}
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
		try {
			model.enterCriticalSection(Lock.WRITE);
			model.remove(arg0);
		} finally {
			model.leaveCriticalSection();
		}
		updateFileModel();

	}

	@Override
	public void removedStatements(Statement[] arg0) {
		for (Statement statement : arg0) {
			try {
				model.enterCriticalSection(Lock.WRITE);
				model.remove(statement);
			} finally {
				model.leaveCriticalSection();
			}
			logger.debug("Removed " + statement);
		}
		updateFileModel();

	}

	@Override
	public void removedStatements(List<Statement> arg0) {
		for (Statement statement : arg0) {
			try {
				model.enterCriticalSection(Lock.WRITE);
				model.remove(statement);
			} finally {
				model.leaveCriticalSection();
			}
			logger.debug("Removed " + statement);
		}
		updateFileModel();

	}

	@Override
	public void removedStatements(StmtIterator arg0) {
		while (arg0.hasNext()) {
			Statement statement = (Statement) arg0.next();
			try {
				model.enterCriticalSection(Lock.WRITE);
				model.remove(statement);
			} finally {
				model.leaveCriticalSection();
			}
			logger.debug("Removed " + statement);
		}
		updateFileModel();

	}

	@Override
	public void removedStatements(Model arg0) {
		for (Statement statement : arg0.listStatements().toList()) {
			try {
				model.enterCriticalSection(Lock.WRITE);
				model.remove(statement);
			} finally {
				model.leaveCriticalSection();
			}

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

class Saver implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(JenaLizardModelListener.class);

	private Model model;
	private String filePath, lang;

	Saver(Model model, String filePath, String lang) {
		this.model = model;
		this.filePath = filePath;
		this.lang = lang;
	}

	@Override
	public void run() {
		try {
			logger.trace("Entering critical section");
			model.enterCriticalSection(Lock.WRITE);
			logger.trace("Writing");
			model.write(new FileOutputStream(new File(filePath)), lang);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			logger.trace("Leaving critical section");
			model.leaveCriticalSection();
		}

	}

}
