package it.cnr.istc.stlab.lizard.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ontology.BooleanClassDescription;
import org.apache.jena.ontology.ComplementClass;
import org.apache.jena.ontology.IntersectionClass;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.OntTools;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.ValidityReport.Report;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.writer.FileCodeWriter;

import it.cnr.istc.stlab.lizard.commons.MavenUtils;
import it.cnr.istc.stlab.lizard.commons.OntologyCodeProject;
import it.cnr.istc.stlab.lizard.commons.exception.NotAvailableOntologyCodeEntityException;
import it.cnr.istc.stlab.lizard.commons.exception.OntologyNotValidException;
import it.cnr.istc.stlab.lizard.commons.inmemory.RestInterface;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClassImpl;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.datatype.DatatypeCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;
import it.cnr.istc.stlab.lizard.commons.recipe.OntologyCodeGenerationRecipe;
import it.cnr.istc.stlab.lizard.core.model.BeanOntologyCodeClass;
import it.cnr.istc.stlab.lizard.core.model.BeanOntologyCodeInterface;
import it.cnr.istc.stlab.lizard.core.model.JenaOntologyCodeClass;
import it.cnr.istc.stlab.lizard.core.model.RestOntologyCodeClass;
import it.cnr.istc.stlab.lizard.core.model.RestOntologyCodeModel;

public class LizardCore implements OntologyCodeGenerationRecipe {

	private static final OntModelSpec INF_PROFILE = OntModelSpec.OWL_MEM_MINI_RULE_INF;
	private static Logger logger = LoggerFactory.getLogger(LizardCore.class);
	private static Logger logger_getProperties = LoggerFactory.getLogger(LizardCore.class.getCanonicalName() + ".getProperties");
	private static Logger logger_high_level = LoggerFactory.getLogger("HIGH_LEVEL");

	private static Logger logger_inspect = LoggerFactory.getLogger(LizardCore.class.getCanonicalName() + ".inspect");
	private OntologyCodeModel ontologyModel;
	private URI ontologyURIBase;

	private RestOntologyCodeModel restOntologyModel;

	public LizardCore(URI ontologyURI) {
		this.ontologyURIBase = ontologyURI;
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

		ontModel.read(ontologyURI.toString());

		OntModel infOntModel = ModelFactory.createOntologyModel(INF_PROFILE);
		infOntModel.add(ontModel);

		validateOntology(infOntModel);

		this.ontologyModel = new RestOntologyCodeModel(ontModel);
		this.ontologyModel.setInfOntModel(infOntModel);
	}

	public LizardCore(URI ontologyURI, URI[] others) {
		this.ontologyURIBase = ontologyURI;
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

		ontModel.read(ontologyURI.toString());

		for (URI uri : others) {
			ontModel.read(uri.toString());
		}

		OntModel infOntModel = ModelFactory.createOntologyModel(INF_PROFILE);
		infOntModel.add(ontModel);

		validateOntology(infOntModel);

		this.ontologyModel = new RestOntologyCodeModel(ontModel);
		this.ontologyModel.setInfOntModel(infOntModel);
	}

	public void createServiceAnnotations(File root, OntologyCodeModel ontologyCodeModel) {
		Map<OntResource, RestOntologyCodeClass> restClassMap = ontologyCodeModel.getOntologyClasses(RestOntologyCodeClass.class);
		Collection<RestOntologyCodeClass> restCalasses = restClassMap.values();
		File metaInfFolder = new File(root, "src/main/resources/META-INF/services");
		if (!metaInfFolder.exists())
			metaInfFolder.mkdirs();
		File restInterfaceAnnotation = new File(metaInfFolder, RestInterface.class.getCanonicalName());
		System.out.println(getClass() + " created file " + restInterfaceAnnotation.getAbsolutePath());
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

	@Override
	public OntologyCodeProject generate() {

		OntologyCodeProject project = null;
		try {
			logger_high_level.info("Create Bean Project");
			project = generateBeans();
		} catch (NotAvailableOntologyCodeEntityException e) {
			e.printStackTrace();
		}
		if (project != null) {
			logger_high_level.info("Create REST Project");
			project = generateRestProject(project.getOntologyCodeModel());
		}
		return project;

	}

	private boolean causesNameClash(OntClass ontClass, OntProperty ontProperty, OntologyCodeModel ontologyModel) {
		OntClass ontClassInf = ontologyModel.getInfOntModel().getOntClass(ontClass.getURI());

		Collection<Restriction> restrictions = new HashSet<>();
		for (OntClass oc : ontClassInf.listSuperClasses().toSet()) {
			if (oc.isRestriction()) {
				Restriction restriction = oc.asRestriction();
				if (restriction.getOnProperty().getURI().equals(ontProperty.getURI())) {
					restrictions.add(restriction);
				}
			} else if (oc.isURIResource()) {
				// The name clash occurs also when one of the super classes defines a method for this ontProperty
				// This is also caused by the fact that the the parameters of the methods are collections using generics.
				// The class into the diamond is ignored in compilation!
				OntClass mostSpecificDomain = getMostSpecificDomain(ontProperty);
				if (!oc.getURI().equals(ontClass.getURI()) && mostSpecificDomain.hasEquivalentClass(oc.asClass())) {
					return true;
				}
			}
		}

		if (restrictions.size() > 1) {
			// The name clash occurs only when two restrictions are defined on the same property for the same class but with different ranges
			// FIXME ??? Is it necessary to check the range of the restriction???
			return true;
		}
		return false;
	}

	private void createBeanMethods(AbstractOntologyCodeClass owner, OntologyCodeModel ontologyModel) {
		OntClass ontClass = ontologyModel.asOntModel().getOntClass(owner.getOntResource().getURI());

		logger_high_level.info("Create bean methods for: " + owner.getOntResource().getLocalName());

		for (OntProperty ontProperty : getPropertiesOfClass(ontClass)) {

			OntClass mostSpecificDomain = getMostSpecificDomain(ontProperty);

			logger_high_level.info("Create bean methods for: " + ontProperty.getLocalName() + " most specific domain " + mostSpecificDomain.getLocalName());

			if (!mostSpecificDomain.hasEquivalentClass(ontClass) && mostSpecificDomain.hasSuperClass(ontClass)) {
				continue;
			}

			if (ontologyModel.asOntModel().getOntProperty(ontProperty.getURI()) == null) {
				// The ont property has been introduced by the JENA reasoner
				logger_high_level.trace(ontProperty + "has been introduced by the reasoner!");
				continue;
			}

			if (!causesNameClash(ontClass, ontProperty, ontologyModel)) {
				Restriction restriction = hasRestrictionOnProperty(ontClass, ontProperty);
				if (restriction != null) {
					createBeanMethodsForRestrictions(restriction, owner, ontologyModel);
				} else {
					OntResource range = getMostSpecificRange(ontProperty);
					if (range != null) {
						if (range.isURIResource()) {
							logger_high_level.trace("RANGE " + range.getURI());
							if (range.isClass()) {
								createBeanMethodsForClassRange(ontProperty, range.asClass(), owner, ontologyModel);
							}
						} else {
							createBeanMethodsForAnonClass(ontProperty, range, owner, ontologyModel);
						}
					} else {
						createBeanMethodsForOntPropertyWithoutRange(ontProperty, owner, ontologyModel);
					}
				}
			}
		}

	}

	private void createBeanMethods(OntProperty ontProperty, AbstractOntologyCodeClass owner, AbstractOntologyCodeClass rangeClass, OntologyCodeModel ontologyCodeModel) {
		Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
		domain.add(rangeClass);
		ontologyCodeModel.createMethod(OntologyCodeMethodType.GET, ontProperty, owner, null, rangeClass);
		ontologyCodeModel.createMethod(OntologyCodeMethodType.SET, ontProperty, owner, domain, null);
		ontologyCodeModel.createMethod(OntologyCodeMethodType.ADD_ALL, ontProperty, owner, domain, null);
		ontologyCodeModel.createMethod(OntologyCodeMethodType.REMOVE_ALL, ontProperty, owner, domain, null);
	}

	private void createBeanMethodsForAnonClass(OntProperty ontProperty, OntResource range, AbstractOntologyCodeClass owner, OntologyCodeModel ontologyCodeModel) {
		BooleanAnonClass anonClass = manageAnonClasses(range.asClass(), ontologyCodeModel);
		createBeanMethods(ontProperty, owner, anonClass, ontologyModel);
	}

	private void createBeanMethodsForClassRange(OntProperty ontProperty, OntClass rangeOntClass, AbstractOntologyCodeClass owner, OntologyCodeModel ontologyCodeMode) {
		logger_inspect.trace(owner.getOntResource().getURI() + " " + ontProperty.getURI() + " " + rangeOntClass.getURI());
		/*
		 * Range of the property is a class
		 */

		OntologyCodeInterface rangeClass = null;

		if (ontProperty.isDatatypeProperty()) {
			try {
				rangeClass = ontologyCodeMode.createOntologyClass(rangeOntClass, DatatypeCodeInterface.class);
			} catch (NotAvailableOntologyCodeEntityException e) {
				e.printStackTrace();
			}
		} else {
			try {
				rangeClass = ontologyCodeMode.createOntologyClass(rangeOntClass, BeanOntologyCodeInterface.class);
			} catch (NotAvailableOntologyCodeEntityException e) {
				e.printStackTrace();
			}
		}

		Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
		logger_inspect.trace("RANGE CLASS " + rangeClass.getOntResource().getURI());
		if (rangeClass != null)
			domain.add(rangeClass);

		createBeanMethods(ontProperty, owner, rangeClass, ontologyModel);
	}

	private void createBeanMethodsForOntPropertyWithoutRange(OntProperty ontProperty, AbstractOntologyCodeClass owner, OntologyCodeModel ontologyModel) {

		/*
		 * Range null
		 */

		OntologyCodeInterface rangeClass = null;
		OntResource rangeOntClass = null;

		if (ontProperty.isDatatypeProperty()) {
			try {
				rangeOntClass = ModelFactory.createOntologyModel().createOntResource(RDFS.Literal.getURI());
				rangeClass = ontologyModel.createOntologyClass(rangeOntClass, DatatypeCodeInterface.class);

			} catch (NotAvailableOntologyCodeEntityException e) {
				e.printStackTrace();
			}
		} else {
			try {
				rangeOntClass = ModelFactory.createOntologyModel().createOntResource(OWL2.Thing.getURI());
				rangeClass = ontologyModel.createOntologyClass(rangeOntClass, BeanOntologyCodeInterface.class);
			} catch (NotAvailableOntologyCodeEntityException e) {
				e.printStackTrace();
			}
		}
		createBeanMethods(ontProperty, owner, rangeClass, ontologyModel);
	}

	private void createBeanMethodsForRestrictions(Restriction restriction, AbstractOntologyCodeClass owner, OntologyCodeModel ontologyModel) {
		OntProperty onProperty = restriction.getOnProperty();
		Resource onClass = null;
		if (restriction.isSomeValuesFromRestriction()) {
			onClass = restriction.asSomeValuesFromRestriction().getSomeValuesFrom();
		} else if (restriction.isAllValuesFromRestriction()) {
			onClass = restriction.asAllValuesFromRestriction().getAllValuesFrom();
		}

		if (onClass != null) {
			if (onClass.isAnon()) {
				if (onClass.canAs(UnionClass.class)) {
					createBeanMethodsForAnonClass(onProperty, onClass.as(UnionClass.class), owner, ontologyModel);
				} else if (onClass.canAs(IntersectionClass.class)) {
					createBeanMethodsForAnonClass(onProperty, onClass.as(IntersectionClass.class), owner, ontologyModel);
				} else if (onClass.canAs(ComplementClass.class)) {
					createBeanMethodsForAnonClass(onProperty, onClass.as(ComplementClass.class), owner, ontologyModel);
				}
			} else if (!onProperty.isDatatypeProperty()) {
				createBeanMethodsForClassRange(onProperty, ontologyModel.asOntModel().getOntClass(onClass.getURI()), owner, ontologyModel);
			} else {
				OntProperty datatypeProperty = ontologyModel.asOntModel().getOntProperty(onProperty.getURI());
				OntClass rangeOntClass = ontologyModel.getInfOntModel().getOntClass(onClass.getURI());
				logger_inspect.trace(owner.getOntResource().getURI() + " " + datatypeProperty.getURI() + " " + rangeOntClass.getURI());
				logger_inspect.trace(owner.getOntResource().getURI() + " " + datatypeProperty.getURI() + " " + rangeOntClass.getURI());
				createBeanMethodsForClassRange(datatypeProperty, rangeOntClass, owner, ontologyModel);
			}
		}
	}

	private void createRESTMethods(AbstractOntologyCodeClass owner, OntologyCodeModel ontologyModel) {

		logger_high_level.trace("Creating REST methods for class " + owner.getOntResource().getURI());

		OntClass ontClass = ontologyModel.asOntModel().getOntClass(owner.getOntResource().getURI());
		logger.debug("Create methods for class " + ontClass.getURI());

		for (OntProperty ontProperty : getPropertiesOfClass(ontClass)) {

			logger.debug("Creating REST method for class " + owner.getOntResource().getURI() + " and method " + ontProperty.getURI());

			OntClass mostSpecificDomain = getMostSpecificDomain(ontProperty);

			if (!mostSpecificDomain.hasEquivalentClass(ontClass) && mostSpecificDomain.hasSuperClass(ontClass)) {
				continue;
			}

			if (ontologyModel.asOntModel().getOntProperty(ontProperty.getURI()) == null) {
				// The ont property has been introduced by the JENA reasoner
				logger_high_level.trace(ontProperty + "has been introduced by the reasoner!");
				continue;
			}

			if (!causesNameClash(ontClass, ontProperty, ontologyModel)) {
				Restriction restriction = hasRestrictionOnProperty(ontClass, ontProperty);
				if (restriction != null) {
					createRESTMethodsForRestriction(restriction, owner, ontologyModel);
				} else {
					OntResource range = getMostSpecificRange(ontProperty);
					if (range != null) {
						if (range.isURIResource()) {
							logger_high_level.trace("RANGE " + range.getURI());
							if (range.isClass()) {
								createRESTMethodsForRangeClasses(ontProperty, range.asClass(), owner, ontologyModel);
							}
						} else {
							createRESTMethodsForAnonClasses(ontProperty, range, owner, ontologyModel);
						}
					} else {
						createRESTMethodsForPropertyWithoutRange(ontProperty, owner, ontologyModel);
					}
				}
			}
		}
	}

	private void createRESTMethodsForAnonClasses(OntProperty ontProperty, OntResource range, AbstractOntologyCodeClass owner, OntologyCodeModel ontologyModel) {
		BooleanAnonClass anonClass = manageAnonClasses(range.asClass(), ontologyModel);

		Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
		domain.add(anonClass);

		ontologyModel.createMethod(OntologyCodeMethodType.GET, ontProperty, owner, domain, anonClass);
		ontologyModel.createMethod(OntologyCodeMethodType.SET, ontProperty, owner, domain, anonClass);
		ontologyModel.createMethod(OntologyCodeMethodType.ADD_ALL, ontProperty, owner, domain, anonClass);
		ontologyModel.createMethod(OntologyCodeMethodType.REMOVE_ALL, ontProperty, owner, domain, anonClass);
	}

	private void createRESTMethodsForPropertyWithoutRange(OntProperty ontProperty, AbstractOntologyCodeClass owner, OntologyCodeModel ontologyModel) {
		OntResource thing = ontologyModel.asOntModel().createOntResource(OWL2.Thing.getURI());

		Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
		domain.add(ontologyModel.getOntologyClass(thing, BeanOntologyCodeInterface.class));

		ontologyModel.createMethod(OntologyCodeMethodType.GET, ontProperty, owner, domain, ontologyModel.getOntologyClass(thing, RestOntologyCodeClass.class));
		ontologyModel.createMethod(OntologyCodeMethodType.SET, ontProperty, owner, domain, ontologyModel.getOntologyClass(thing, RestOntologyCodeClass.class));
		ontologyModel.createMethod(OntologyCodeMethodType.ADD_ALL, ontProperty, owner, domain, ontologyModel.getOntologyClass(thing, RestOntologyCodeClass.class));
		ontologyModel.createMethod(OntologyCodeMethodType.REMOVE_ALL, ontProperty, owner, domain, ontologyModel.getOntologyClass(thing, RestOntologyCodeClass.class));
	}

	private void createRESTMethodsForRangeClasses(OntProperty ontProperty, OntClass range, AbstractOntologyCodeClass owner, OntologyCodeModel ontologyModel) {

		OntologyCodeInterface rangeClass = null;
		/*
		 * The property is a datatype property. In this case we use Jena to map the range to the appropriate Java type. E.g. xsd:string -> java.lang.String
		 */

		OntClass rangeOntClass = ModelFactory.createOntologyModel().createClass(range.getURI());
		if (ontProperty.isDatatypeProperty()) {
			try {
				rangeClass = ontologyModel.createOntologyClass(rangeOntClass, DatatypeCodeInterface.class);
			} catch (NotAvailableOntologyCodeEntityException e) {
				e.printStackTrace();
			}
		} else {
			try {
				rangeClass = ontologyModel.createOntologyClass(rangeOntClass, BeanOntologyCodeInterface.class);
			} catch (NotAvailableOntologyCodeEntityException e) {
				e.printStackTrace();
			}
		}

		Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
		domain.add(rangeClass);

		logger.trace("Create method for ont " + ontProperty.getLocalName() + " RANGE " + range.getLocalName() + " in class " + owner.getOntResource().getLocalName());

		ontologyModel.createMethod(OntologyCodeMethodType.GET, ontProperty, owner, domain, rangeClass);
		ontologyModel.createMethod(OntologyCodeMethodType.SET, ontProperty, owner, domain, rangeClass);
		ontologyModel.createMethod(OntologyCodeMethodType.ADD_ALL, ontProperty, owner, domain, rangeClass);
		ontologyModel.createMethod(OntologyCodeMethodType.REMOVE_ALL, ontProperty, owner, domain, rangeClass);

	}

	private void createRESTMethodsForRestriction(Restriction restriction, AbstractOntologyCodeClass owner, OntologyCodeModel ontologyModel) {
		OntProperty onProperty = restriction.getOnProperty();
		Resource onClass = null;
		if (restriction.isSomeValuesFromRestriction()) {
			onClass = restriction.asSomeValuesFromRestriction().getSomeValuesFrom();
		} else if (restriction.isAllValuesFromRestriction()) {
			onClass = restriction.asAllValuesFromRestriction().getAllValuesFrom();
		}

		if (onClass != null && !onProperty.isDatatypeProperty()) {

			try {
				OntologyCodeClass rangeClass = ontologyModel.createOntologyClass(ontologyModel.asOntModel().getOntResource(onClass), RestOntologyCodeClass.class);

				Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
				domain.add(rangeClass);

				ontologyModel.createMethod(OntologyCodeMethodType.GET, onProperty, owner, domain, rangeClass);
				ontologyModel.createMethod(OntologyCodeMethodType.SET, onProperty, owner, domain, rangeClass);
				ontologyModel.createMethod(OntologyCodeMethodType.ADD_ALL, onProperty, owner, domain, rangeClass);
				ontologyModel.createMethod(OntologyCodeMethodType.REMOVE_ALL, onProperty, owner, domain, rangeClass);

			} catch (NotAvailableOntologyCodeEntityException e) {
				e.printStackTrace();
			}
		}
	}

	private OntologyCodeProject generateBeans() throws NotAvailableOntologyCodeEntityException {

		OntologyCodeModel ontologyCodeModel = new RestOntologyCodeModel(this.ontologyModel.asOntModel());

		ontologyCodeModel.setInfOntModel(this.ontologyModel.getInfOntModel());
		OntModel ontModel = ontologyCodeModel.asOntModel();

		String baseURI = ontModel.getNsPrefixURI("");
		if (baseURI == null) {
			ExtendedIterator<Ontology> ontologyIt = ontModel.listOntologies();
			while (ontologyIt.hasNext())
				baseURI = ontologyIt.next().getURI();
			if (baseURI == null)
				ontModel.setNsPrefix("", ontologyURIBase.toString());
			else
				ontModel.setNsPrefix("", baseURI);
		}

		URI ontologyBaseURI;
		try {
			ontologyBaseURI = new URI(baseURI);
		} catch (URISyntaxException e) {
			ontologyBaseURI = ontologyURIBase;
		}
		OntClass owlThing = ontModel.getOntClass(OWL2.Thing.getURI());

		/*
		 * Create interface for owl:Thing
		 */
		OntologyCodeInterface ontologyThingInterface = ontologyCodeModel.createOntologyClass(owlThing, BeanOntologyCodeInterface.class);
		createBeanMethods(ontologyThingInterface, ontologyCodeModel);

		((JDefinedClass) ontologyThingInterface.asJDefinedClass()).method(JMod.PUBLIC, ontologyCodeModel.asJCodeModel().VOID, "setId").param(String.class, "id");
		((JDefinedClass) ontologyThingInterface.asJDefinedClass()).method(JMod.PUBLIC, String.class, "getId");
		((JDefinedClass) ontologyThingInterface.asJDefinedClass()).method(JMod.PUBLIC, ontologyCodeModel.asJCodeModel().VOID, "setIsCompleted").param(Boolean.class, "isCompleted");
		((JDefinedClass) ontologyThingInterface.asJDefinedClass()).method(JMod.PUBLIC, Boolean.class, "getIsCompleted");

		/*
		 * Create bean for owl:Thing
		 */
		/*
		 * Create java bean and Jena-based class.
		 */
		ontologyCodeModel.createOntologyClass(owlThing, BeanOntologyCodeClass.class);
		ontologyCodeModel.createOntologyClass(owlThing, JenaOntologyCodeClass.class);

		List<OntClass> roots = OntTools.namedHierarchyRoots(ontModel);

		for (OntClass root : roots) {
			visitHierarchyTreeForBeans(root, ontologyCodeModel);
		}

		// Extends for interfaces
		Map<OntResource, BeanOntologyCodeInterface> interfaceClassMap = ontologyCodeModel.getOntologyClasses(BeanOntologyCodeInterface.class);
		interfaceClassMap.values().forEach(_interface -> {
			OntClass interfaceOntClass = ontologyCodeModel.getInfOntModel().getOntClass(_interface.getOntResource().getURI());
			for (OntClass superClass : interfaceOntClass.listSuperClasses().toSet()) {
				if (superClass.isURIResource() && !superClass.isRestriction()) {
					// add extends to interface
					BeanOntologyCodeInterface interfaceToExtend = interfaceClassMap.get(superClass);
					if (interfaceToExtend != null) {
						_interface.addInterfaceToExtend(interfaceToExtend);
					}
				}
			}
		});

		/*
		 * Create class implementations for java beans
		 */
		Map<OntResource, BeanOntologyCodeClass> beanClassMap = ontologyCodeModel.getOntologyClasses(BeanOntologyCodeClass.class);

		Set<OntResource> ontResources = beanClassMap.keySet();
		final Set<AbstractOntologyCodeClassImpl> ontologyClasses = new HashSet<AbstractOntologyCodeClassImpl>();
		ontResources.forEach(ontResource -> {
			if (ontResource.isURIResource()) {
				BeanOntologyCodeClass ontologyClass = beanClassMap.get(ontResource);
				ontologyClasses.add(ontologyClass);
			}
		});

		ontologyClasses.forEach(ontologyClass -> {

			logger_high_level.info("Creating class bean implements for " + ontologyClass.getOntResource());

			OntClass ontClass = ontologyCodeModel.getInfOntModel().getOntClass(ontologyClass.getOntResource().getURI());
			OntologyCodeInterface ontologyInterface = ontologyCodeModel.getOntologyClass(ontClass, BeanOntologyCodeInterface.class);
			ExtendedIterator<OntClass> superClassIt = ontClass.listSuperClasses();
			List<OntologyCodeInterface> ontologySuperInterfaces = new ArrayList<OntologyCodeInterface>();
			ontologySuperInterfaces.add(ontologyCodeModel.getOntologyClass(ModelFactory.createOntologyModel().createOntResource(OWL2.Thing.getURI()), BeanOntologyCodeInterface.class));

			if (ontologyInterface != null)
				ontologySuperInterfaces.add(ontologyInterface);

			while (superClassIt.hasNext()) {
				OntClass superClass = superClassIt.next();
				if (superClass.isURIResource()) {
					OntologyCodeInterface ontologySuperInterface = ontologyCodeModel.getOntologyClass(superClass, BeanOntologyCodeInterface.class);
					if (ontologySuperInterface != null) {
						ontologySuperInterfaces.add(ontologySuperInterface);
					}
				}
			}
			OntologyCodeInterface[] classArray = new OntologyCodeInterface[ontologySuperInterfaces.size()];
			ontologyCodeModel.createClassImplements(ontologyClass, ontologySuperInterfaces.toArray(classArray));
		});

		/*
		 * Create class implementations for Jena-based classes
		 */
		Map<OntResource, JenaOntologyCodeClass> jenaClassMap = ontologyCodeModel.getOntologyClasses(JenaOntologyCodeClass.class);
		ontResources = jenaClassMap.keySet();
		final Set<AbstractOntologyCodeClassImpl> jenaClasses = new HashSet<AbstractOntologyCodeClassImpl>();
		for (OntResource ontResource : ontResources) {
			OntologyCodeClass ontologyClass = jenaClassMap.get(ontResource);
			jenaClasses.add(ontologyClass);
		}

		jenaClasses.forEach(ontologyClass -> {

			OntClass ontClass = ontologyCodeModel.getInfOntModel().getOntClass(ontologyClass.getOntResource().getURI());
			logger_high_level.info("Creating JENA classes of " + ontClass.getURI());
			OntologyCodeInterface ontologyInterface = ontologyCodeModel.getOntologyClass(ontClass, BeanOntologyCodeInterface.class);

			ExtendedIterator<OntClass> superClassIt = ontClass.listSuperClasses();
			List<OntologyCodeInterface> ontologySuperInterfaces = new ArrayList<OntologyCodeInterface>();
			ontologySuperInterfaces.add(ontologyCodeModel.getOntologyClass(ModelFactory.createOntologyModel().createOntResource(OWL2.Thing.getURI()), BeanOntologyCodeInterface.class));

			if (ontologyInterface != null)
				ontologySuperInterfaces.add(ontologyInterface);

			while (superClassIt.hasNext()) {
				OntClass superClass = superClassIt.next();
				if (superClass.isURIResource()) {
					OntologyCodeInterface ontologySuperInterface = ontologyCodeModel.getOntologyClass(superClass, BeanOntologyCodeInterface.class);
					if (ontologySuperInterface != null)
						ontologySuperInterfaces.add(ontologySuperInterface);
				}
			}
			OntologyCodeInterface[] classArray = new OntologyCodeInterface[ontologySuperInterfaces.size()];

			ontologyCodeModel.createClassImplements(ontologyClass, ontologySuperInterfaces.toArray(classArray));

		});

		return new OntologyCodeProject(ontologyBaseURI, ontologyCodeModel);
	}

	private OntologyCodeProject generateRestProject(OntologyCodeModel model) {

		this.restOntologyModel = new RestOntologyCodeModel(model);
		this.restOntologyModel.setInfOntModel(model.getInfOntModel());

		OntModel ontModel = restOntologyModel.asOntModel();

		String baseURI = ontModel.getNsPrefixURI("");
		if (baseURI == null) {
			ExtendedIterator<Ontology> ontologyIt = ontModel.listOntologies();
			while (ontologyIt.hasNext())
				baseURI = ontologyIt.next().getURI();
			if (baseURI == null)
				ontModel.setNsPrefix("", ontologyURIBase.toString());
			else
				ontModel.setNsPrefix("", baseURI);
		}

		URI ontologyBaseURI;
		try {
			ontologyBaseURI = new URI(baseURI);
		} catch (URISyntaxException e) {
			ontologyBaseURI = ontologyURIBase;
		}

		List<OntClass> roots = OntTools.namedHierarchyRoots(ontModel);

		for (OntClass root : roots) {
			visitHierarchyTreeForRest(root, restOntologyModel);
		}

		return new OntologyCodeProject(ontologyBaseURI, restOntologyModel);

	}

	private OntClass getMostSpecificDomain(OntProperty op) {

		OntClass most_specific = this.ontologyModel.asOntModel().getOntClass(OWL2.Thing.getURI());
		logger.debug("Get domains of " + op.getURI());
		Set<OntClass> r = new HashSet<OntClass>();
		r.add(most_specific);

		for (OntResource domain : this.ontologyModel.getInfOntModel().getOntProperty(op.getURI()).listDomain().toSet()) {

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

	private OntClass getMostSpecificRange(OntProperty op) {
		logger.debug("Get ranges of " + op.getURI());

		if (op.isObjectProperty()) {
			boolean foundSomething = false;
			OntClass most_specific = this.ontologyModel.asOntModel().getOntClass(OWL2.Thing.getURI());
			Set<OntClass> r = new HashSet<OntClass>();
			r.add(most_specific);

			for (OntResource range : this.ontologyModel.getInfOntModel().getOntProperty(op.getURI()).listRange().toSet()) {
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
				return this.ontologyModel.asOntModel().getOntClass(OWL2.Thing.getURI());
			}
			return this.ontologyModel.asOntModel().getOntClass(most_specific.getURI());
		} else {
			boolean foundSomething = false;
			OntClass most_specific = this.ontologyModel.getInfOntModel().getOntClass(RDFS.Resource.getURI());
			Set<OntClass> r = new HashSet<OntClass>();
			r.add(most_specific);

			for (OntResource range : this.ontologyModel.getInfOntModel().getOntProperty(op.getURI()).listRange().toSet()) {
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
				return this.ontologyModel.getInfOntModel().getOntClass(RDFS.Resource.getURI());
			}
			logger.debug("Returning " + most_specific.getURI());
			return this.ontologyModel.getInfOntModel().getOntClass(most_specific.getURI());
		}
	}

	private Set<OntProperty> getPropertiesOfClass(OntClass c) {

		logger_getProperties.trace("Get properties for " + c.getURI());
		logger.debug("Property of " + c.getURI());

		Set<OntProperty> r = new HashSet<OntProperty>();

		// taking properties without inference
		c.listDeclaredProperties().toSet().forEach(op -> {
			this.ontologyModel.getInfOntModel().getOntProperty(op.getURI()).listDomain().toSet().forEach(dom -> {
				if (dom.isURIResource() && dom.getURI().equals(c.getURI())) {
					r.add(op);
					logger_getProperties.trace("Adding not inf " + op.getURI());
				}
			});
		});

		Set<OntProperty> ontProperties = this.ontologyModel.getInfOntModel().listAllOntProperties().toSet();

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
								logger_getProperties.trace("Adding inf " + opInf.getURI());
							}
						}
					} else if (dom.isURIResource() && !dom.asClass().isRestriction()) {

						if (dom.getURI().equals(c.getURI()) || dom.asClass().hasSubClass(c)) {
							// The domain domain is a class
							r.add(opInf);
							logger.debug("Adding " + opInf.getURI() + " ");
							logger_getProperties.debug("Adding not inf " + opInf.getURI() + " ");
						}

					}
				}
			}
		}
		return r;
	}

	private Restriction hasRestrictionOnProperty(OntClass ontClass, OntProperty ontProperty) {
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

	private BooleanAnonClass manageAnonClasses(OntClass ontClass, OntologyCodeModel ontologyModel) {
		return ontologyModel.createAnonClass(ontClass);
	}

	private void validateOntology(OntModel ontModel) {

		logger.trace("Validating inf model");
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

	private void visitHierarchyTreeForBeans(OntClass ontClass, OntologyCodeModel ontologyModel) {

		logger_high_level.info("Creating class " + ontClass.getURI());

		OntologyCodeInterface ontologyInterface = null;
		try {
			ontologyInterface = ontologyModel.createOntologyClass(ontClass, BeanOntologyCodeInterface.class);
		} catch (NotAvailableOntologyCodeEntityException e) {
			e.printStackTrace();
		}

		if (ontologyInterface != null) {

			createBeanMethods(ontologyInterface, ontologyModel);

			if (!hasMethod(((JDefinedClass) ontologyInterface.asJDefinedClass()), "setId")) {
				((JDefinedClass) ontologyInterface.asJDefinedClass()).method(JMod.PUBLIC, ontologyInterface.getJCodeModel().VOID, "setId").param(String.class, "id");
				((JDefinedClass) ontologyInterface.asJDefinedClass()).method(JMod.PUBLIC, String.class, "getId");
				((JDefinedClass) ontologyInterface.asJDefinedClass()).method(JMod.PUBLIC, ontologyInterface.getJCodeModel().VOID, "setIsCompleted").param(Boolean.class, "isCompleted");
				((JDefinedClass) ontologyInterface.asJDefinedClass()).method(JMod.PUBLIC, Boolean.class, "getIsCompleted");
			}

			try {
				ontologyModel.createOntologyClass(ontClass, BeanOntologyCodeClass.class);
				ontologyModel.createOntologyClass(ontClass, JenaOntologyCodeClass.class);
			} catch (NotAvailableOntologyCodeEntityException e) {
				e.printStackTrace();
			}

			ExtendedIterator<OntClass> subClasses = ontClass.listSubClasses();
			while (subClasses.hasNext()) {
				OntClass subClass = subClasses.next();

				if (subClass.isURIResource()) {
					visitHierarchyTreeForBeans(subClass, ontologyModel);
				} else {
					manageAnonClasses(subClass, ontologyModel);
				}
			}
		}

	}

	private void visitHierarchyTreeForRest(OntClass ontClass, OntologyCodeModel ontologyModel) {

		logger_high_level.trace("Creating rest class for " + ontClass.getURI());

		OntologyCodeClass ontologyClass;
		try {
			if (ontologyModel.getOntologyClass(ontClass, BeanOntologyCodeClass.class) != null) {
				ontologyClass = ontologyModel.createOntologyClass(ontClass, RestOntologyCodeClass.class);
				createRESTMethods(ontologyClass, ontologyModel);
			}
		} catch (NotAvailableOntologyCodeEntityException e) {
			e.printStackTrace();
		}

		ExtendedIterator<OntClass> subClasses = ontClass.listSubClasses();
		while (subClasses.hasNext()) {
			OntClass subClass = subClasses.next();

			if (subClass.isURIResource())
				visitHierarchyTreeForRest(subClass, ontologyModel);
			else
				manageAnonClasses(subClass, ontologyModel);
		}
	}

	public static boolean hasTypeMapper(String uri) {
		Iterator<RDFDatatype> it = TypeMapper.getInstance().listTypes();
		while (it.hasNext()) {
			RDFDatatype rdfDatatype = (RDFDatatype) it.next();
			if (rdfDatatype.getURI().equals(uri)) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {

		System.setProperty("M2_HOME", "/Users/lgu/Programs/apache-maven");
		System.setProperty("JAVA_HOME", "/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home");

		registerDatatypes();

		URI uri = null;
		URI[] uris = {};

		try {
			// uri = new URI("/Users/lgu/Dropbox/stlab/ontologies/paraphrase/ppdb.owl");
			uri = new URI("http://www.ontologydesignpatterns.org/ont/mario/tagging.owl");
			// uri = new URI("/Users/lgu/Desktop/ont.owl");
			OntologyCodeGenerationRecipe codegen = new LizardCore(uri);
			OntologyCodeProject ontologyCodeProject = codegen.generate();

			try {
				String outFolder = "test_out";
				File testFolder = new File(outFolder);
				if (testFolder.exists()) {
					System.out.println("esists " + testFolder.getClass());
					FileUtils.deleteDirectory(testFolder);
				} else {
					System.out.println("not esists");
				}
				File src = new File(outFolder+"/src/main/java");
				File resources = new File(outFolder+"/src/main/resources");
				File test = new File(outFolder+"/src/test/java");
				if (!src.exists())
					src.mkdirs();
				if (!resources.exists())
					resources.mkdirs();
				if (!test.exists())
					test.mkdirs();

				CodeWriter writer = new FileCodeWriter(src, "UTF-8");
				ontologyCodeProject.getOntologyCodeModel().asJCodeModel().build(writer);
				((LizardCore) codegen).createServiceAnnotations(new File(outFolder), ontologyCodeProject.getOntologyCodeModel());
				/*
				 * Generate the POM descriptor file and build the project as a Maven project.
				 */
				File pom = new File(outFolder+"/pom.xml");
				Writer pomWriter = new FileWriter(new File(outFolder+"/pom.xml"));
				Map<String, String> dataModel = new HashMap<String, String>();
				dataModel.put("artifactId", ontologyCodeProject.getArtifactId());
				dataModel.put("groupId", ontologyCodeProject.getGroupId());
				MavenUtils.generatePOM(pomWriter, dataModel);
				MavenUtils.buildProject(pom);

				/*
				 * Generate Lizard file
				 */

				OntModel om = ModelFactory.createOntologyModel(INF_PROFILE);
				om.read(uri.toString());

				for (URI u : uris) {
					om.read(u.toString());
				}

				writeGeneratedOntologies(om, outFolder);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}

	public static final String ontologiesFileName = "ontologies";

	private static void writeGeneratedOntologies(OntModel ontModel, String folder) throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(folder + "/" + ontologiesFileName));

		String ontModelBase = ontModel.getNsPrefixURI("");
		if (ontModelBase.endsWith("#")) {
			ontModelBase = ontModelBase.substring(0, ontModelBase.length() - 1);
		}
		ontModelBase += "\n";

		fos.write(ontModelBase.getBytes());

		ontModel.listSubModels(true).forEachRemaining(om -> {
			String toPrint = om.getNsPrefixURI("");
			if (toPrint.endsWith("#")) {
				toPrint = toPrint.substring(0, toPrint.length() - 1);
			}
			toPrint += "\n";
			try {
				fos.write(toPrint.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		fos.close();
	}

	private static boolean hasMethod(JDefinedClass jdefClass, String methodName) {
		for (JMethod m : jdefClass.methods()) {
			if (m.name().equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	private static void registerDatatypes() {
		// TODO let register custom datatype
		// TypeMapper.getInstance().registerDatatype(new XSDNonNegativeIntegerType());
	}

}
