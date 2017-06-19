package it.cnr.istc.stlab.lizard.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.ontology.BooleanClassDescription;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.ValidityReport.Report;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.lizard.commons.exception.OntologyNotValidException;

public class OntologyUtils {

	private static Logger logger = LoggerFactory.getLogger(OntologyUtils.class);

	private static OntModelSpec INF_PROFILE = OntModelSpec.OWL_MEM_MINI_RULE_INF;

	static void validateOntology(OntModel ontModel) {

		logger.info("Validating inf model");
		ValidityReport validity = ontModel.validate();
		if (validity != null) {
			if (!validity.isValid()) {
				for (Iterator<Report> in = validity.getReports(); in.hasNext();) {
					logger.error(" - " + in.next());
				}
				throw new OntologyNotValidException("Ontology not valid!");
			} else {
				logger.info("Ontology valid! Reasoner: " + INF_PROFILE.getReasoner().getClass().getName());
			}
		} else {
			logger.warn("Validation of the ontology not performed!");
		}

	}

	static OntClass getMostSpecificRange(OntProperty op, OntModel ontModel, OntModel ontModelInf) {
		logger.debug("Get ranges of " + op.getURI());

		if (op.isObjectProperty()) {
			boolean foundSomething = false;
			OntClass most_specific = ontModel.getOntClass(OWL2.Thing.getURI());
			Set<OntClass> r = new HashSet<OntClass>();
			r.add(most_specific);

			for (OntResource range : ontModelInf.getOntProperty(op.getURI()).listRange().toSet()) {
				logger.debug("RANGE: " + range.getURI());
				if (range.isURIResource() && range.isClass()) {
					foundSomething = true;
					if (most_specific.hasSubClass(range)) {
						// range is more specific than most_specific
						r.remove(most_specific);
						r.add(range.asClass());
						most_specific = range.asClass();
					} else if (!most_specific.hasSuperClass(range)) {
						// range is neither subclass or superclass of most_specific is a new range
						r.add(range.asClass());
					}
				}
			}
			if (!foundSomething)
				return null;
			if (r.size() > 1) {
				return ontModel.getOntClass(OWL2.Thing.getURI());
			}
			return ontModel.getOntClass(most_specific.getURI());
		} else {

			if (op.getRange() != null) {
				return op.getRange().asClass();
			}

			boolean foundSomething = false;
			OntClass most_specific = ontModelInf.getOntClass(RDFS.Resource.getURI());
			Set<OntClass> r = new HashSet<OntClass>();
			r.add(most_specific);

			for (OntResource range : ontModelInf.getOntProperty(op.getURI()).listRange().toSet()) {
				logger.debug("RANGE: " + range.getURI());
				if (range.isURIResource() && range.isClass()) {
					foundSomething = true;
					if (most_specific.hasSubClass(range)) {
						// range is more specific than most_specific
						logger.debug("Removing " + most_specific.getURI());
						r.remove(most_specific);
						logger.debug("Size set " + r.size());
						r.add(range.asClass());
						most_specific = range.asClass();
						logger.debug("Current most specific " + most_specific.getURI());
					} else if (!most_specific.hasSuperClass(range)) {
						// range is neither subclass or superclass of most_specific is a new range
						r.add(range.asClass());
					}
				}
			}
			if (!foundSomething) {
				return null;
			}
			if (r.size() > 1) {
				return ontModelInf.getOntClass(RDFS.Resource.getURI());
			}
			logger.debug("Returning " + most_specific.getURI());
			return ontModelInf.getOntClass(most_specific.getURI());
		}
	}

	static Set<OntProperty> getPropertiesOfClass(OntClass c, OntModel ontModel, OntModel ontModelInf) {

		logger.trace("Get properties for " + c.getURI());

		Set<OntProperty> r = new HashSet<OntProperty>();

		// taking properties without inference
		c.listDeclaredProperties().toSet().forEach(op -> {
			ontModelInf.getOntProperty(op.getURI()).listDomain().toSet().forEach(dom -> {
				if (dom.isURIResource() && dom.getURI().equals(c.getURI())) {
					r.add(op);
					logger.trace("Adding not inf " + op.getURI());
				}
			});
		});

		Set<OntProperty> ontProperties = ontModelInf.listAllOntProperties().toSet();

		for (OntProperty opInf : ontProperties) {

			logger.debug("Checking add D: " + c.getLocalName() + ", P:" + opInf.getLocalName());

			if (!opInf.isURIResource()) {
				logger.warn("Property chain ignored!");
				continue;
			}

			Set<? extends OntResource> doms = opInf.listDomain().toSet();

			for (OntResource dom : doms) {

				if (dom != null && dom.isClass()) {

					if (dom.asClass().isUnionClass()) {
						// The domain of the property is a restriction on a Union Class
						BooleanClassDescription booleanClassDescription = dom.asClass().asUnionClass();
						ExtendedIterator<? extends OntClass> members = booleanClassDescription.listOperands();
						while (members.hasNext()) {
							OntClass member = members.next();
							if (member.getURI().equals(c.getURI()) || member.hasSubClass(c)) {
								r.add(opInf);
								logger.trace("Adding inf " + opInf.getURI());
							}
						}
					} else if (dom.isURIResource() && !dom.asClass().isRestriction()) {
						logger.trace("Property domain of " + opInf.getURI() + " " + dom.getURI());
						if (dom.asClass().hasEquivalentClass(c)) {
							r.add(opInf);
							logger.debug("Adding not inf " + opInf.getURI() + " ");
						} else {
							OntClass mostspecific_domain = OntologyUtils.getMostSpecificDomain(opInf, ontModel, ontModelInf);
							logger.trace("Most specific domain: " + mostspecific_domain.getURI());
							if (mostspecific_domain != null && mostspecific_domain.hasSubClass(c)) {
								r.add(opInf);
								logger.debug("Adding not inf " + opInf.getURI() + " ");
							}
						}
					}
				}
			}
		}
		return r;
	}

	static Restriction hasRestrictionOnProperty(OntClass ontClass, OntProperty ontProperty) {
		logger.debug("Checking if " + ontClass.getLocalName() + " has a restriction on " + ontProperty.getLocalName());
		for (OntClass sc : ontClass.listSuperClasses().toSet()) {
			if (sc.isRestriction()) {
				Restriction restriction = sc.asRestriction();
				if (restriction.getOnProperty().getURI().equals(ontProperty.getURI())) {
					return restriction;
				}
			}
		}
		return null;
	}

	static OntClass getMostSpecificDomain(OntProperty op, OntModel ontModel, OntModel ontModelInf) {

		OntClass most_specific = ontModel.getOntClass(OWL2.Thing.getURI());
		logger.debug("Get domains of " + op.getURI());
		Set<OntClass> r = new HashSet<OntClass>();
		r.add(most_specific);

		for (OntResource domain : ontModelInf.getOntProperty(op.getURI()).listDomain().toSet()) {

			if (domain.isURIResource() && domain.isClass()) {
				if (most_specific.hasSubClass(domain)) {
					// range is more specific than most_specific
					r.remove(most_specific);
					r.add(domain.asClass());
					most_specific = domain.asClass();
				} else if (!most_specific.hasSuperClass(domain)) {
					// range is neither subclass or superclass of most_specific is a new range
					r.add(domain.asClass());
				}
			}
		}
		return most_specific;
	}

}
