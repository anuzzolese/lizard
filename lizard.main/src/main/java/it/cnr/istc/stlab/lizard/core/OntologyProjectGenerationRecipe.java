package it.cnr.istc.stlab.lizard.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ontology.ComplementClass;
import org.apache.jena.ontology.IntersectionClass;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
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
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

import it.cnr.istc.stlab.lizard.commons.OntologyCodeProject;
import it.cnr.istc.stlab.lizard.commons.exception.NotAvailableOntologyCodeEntityException;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClassImpl;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.datatype.DatatypeCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;
import it.cnr.istc.stlab.lizard.commons.recipe.OntologyCodeGenerationRecipe;
import it.cnr.istc.stlab.lizard.commons.web.swagger.DescriptionGenerator;
import it.cnr.istc.stlab.lizard.core.model.BeanOntologyCodeClass;
import it.cnr.istc.stlab.lizard.core.model.BeanOntologyCodeInterface;
import it.cnr.istc.stlab.lizard.core.model.JenaOntologyCodeClass;
import it.cnr.istc.stlab.lizard.core.model.RestOntologyCodeClass;
import it.cnr.istc.stlab.lizard.core.model.RestOntologyCodeModel;

public class OntologyProjectGenerationRecipe implements OntologyCodeGenerationRecipe {

	private static Logger logger = LoggerFactory.getLogger(OntologyProjectGenerationRecipe.class);
	private static Logger logger_create_bean_methods = LoggerFactory
			.getLogger(OntologyProjectGenerationRecipe.class.getCanonicalName() + ".createBeanMethods");
	private static Logger logger_createRESTMethods = LoggerFactory
			.getLogger(OntologyProjectGenerationRecipe.class.getCanonicalName() + ".createRESTMethods");
	private static Logger logger_high_level = LoggerFactory.getLogger("HIGH_LEVEL");
	private static Logger logger_inspect = LoggerFactory
			.getLogger(OntologyProjectGenerationRecipe.class.getCanonicalName() + ".inspect");
	private OntologyCodeModel ontologyModel;
	private RestOntologyCodeModel restOntologyModel;
	private boolean generateRestProject = true;

	// TODO REMOVE
	private URI ontologyURIBase;

	public OntologyProjectGenerationRecipe(boolean generateRestProject, String fileOntDocumentManager, URI... uris) {
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		if (fileOntDocumentManager != null) {
			OntDocumentManager odm = new OntDocumentManager(fileOntDocumentManager);
			OntModelSpec oms = OntModelSpec.OWL_MEM;
			oms.setDocumentManager(odm);
			ontModel = ModelFactory.createOntologyModel(oms);
		}

		for (URI uri : uris) {
			logger.trace("Reading " + uri.toString());
			ontModel.read(uri.toString());
		}

		OntModel infOntModel = ModelFactory.createOntologyModel(OntologyUtils.getInfProfile());
		infOntModel.add(ontModel);

		OntologyUtils.validateOntology(infOntModel);
		
		
		logger.info("Number of classes: {}",infOntModel.listClasses().toList().size());
		logger.info("Number of properties: {}",infOntModel.listAllOntProperties().toList().size());

		this.ontologyModel = new RestOntologyCodeModel(ontModel);
		this.ontologyModel.setInfOntModel(infOntModel);
		this.generateRestProject = generateRestProject;
	}

	public OntologyProjectGenerationRecipe(URI... uris) {
		this(false, null, uris);
	}
	
	public OntologyProjectGenerationRecipe(String fileOntDocumentManager,URI... uris) {
		this(false, fileOntDocumentManager, uris);
	}

	private boolean causesNameClash(OntClass ontClass, OntProperty ontProperty, OntologyCodeModel ontologyModel) {

		// A property cannot cause a nameclase on the owl:Thing class
		if (ontClass.getURI().equals(OWL.Thing.getURI()))
			return false;

		OntClass ontClassInf = ontologyModel.getInfOntModel().getOntClass(ontClass.getURI());
		Collection<Restriction> restrictions = new HashSet<>();
		OntClass mostSpecificDomain = OntologyUtils.getMostSpecificDomain(ontProperty, ontologyModel.asOntModel(),
				ontologyModel.getInfOntModel());
		OntClass mostSpecificDomainInf = ontologyModel.getInfOntModel().getOntClass(mostSpecificDomain.getURI());

		for (OntClass superClassInf : ontClassInf.listSuperClasses().toSet()) {
			if (superClassInf.isRestriction()) {
				Restriction restriction = superClassInf.asRestriction();
				if (restriction.getOnProperty().getURI().equals(ontProperty.getURI())) {
					logger.debug("Found a restriction on {} for class {} SV {} AV {}", ontProperty.getLocalName(),
							ontClass.getLocalName(), restriction.isSomeValuesFromRestriction(),
							restriction.isAllValuesFromRestriction());
					restrictions.add(restriction);
				}
			} else if (superClassInf.isURIResource()) {
				// The name clash occurs also when one of the super classes defines a method for
				// this ontProperty
				// This is also caused by the fact that the the parameters of the methods are
				// collections using generics.
				// The class into the diamond is ignored in compilation!
				logger.debug("SC {} CL {} OP: {} SC: {} MSD: {}", superClassInf.asResource().getURI(),
						ontClass.getLocalName(), ontProperty.getLocalName(), superClassInf.getLocalName(),
						mostSpecificDomainInf.getLocalName());
				if (!superClassInf.getURI().equals(ontClass.getURI())
						&& mostSpecificDomainInf.hasEquivalentClass(superClassInf.asClass())) {
					return true;
				}
			}
		}

		if (restrictions.size() > 1) {
			// The name clash occurs only when two restrictions are defined on the same
			// property for the same class but with different ranges
			// FIXME ??? Is it necessary to check the range of the restriction???
			logger.debug("Multiple restictions on {} for property {}!", ontClass.getLocalName(),
					ontProperty.getLocalName());
			Resource range = null;
			for (Restriction restriction : restrictions) {
				// TODO FIXME check other classes of restrictions
				logger.debug("Restriction some {} all {}:: {}", restriction.isSomeValuesFromRestriction(),
						restriction.isAllValuesFromRestriction(), restriction.getClass().getName());
				if (restriction.isAllValuesFromRestriction()) {
					Resource allRange = restriction.asAllValuesFromRestriction().getAllValuesFrom();
					if (range != null && !range.equals(allRange)) {
						return true;
					}
					range = allRange;

				} else if (restriction.isSomeValuesFromRestriction()) {
					Resource someRange = restriction.asSomeValuesFromRestriction().getSomeValuesFrom();
					if (range != null && !range.equals(someRange)) {
						return true;
					}
					range = someRange;
				}
			}
		}
		return false;
	}

	private void createBeanMethods(AbstractOntologyCodeClass owner, OntologyCodeModel ontologyModel) {
		OntClass ontClass = ontologyModel.asOntModel().getOntClass(owner.getOntResource().getURI());
		logger.trace("Create bean methods for: " + owner.getOntResource().getLocalName());
		logger_create_bean_methods.trace("Create bean methods for: " + owner.getOntResource().getLocalName());

		for (OntProperty ontProperty : OntologyUtils.getPropertiesOfClass(ontClass, ontologyModel.getInfOntModel(),
				ontologyModel.getInfOntModel())) {

			OntClass mostSpecificDomain = OntologyUtils.getMostSpecificDomain(ontProperty,
					ontologyModel.getInfOntModel(), ontologyModel.getInfOntModel());

			logger.trace("Create bean methods for: " + ontProperty.getLocalName() + " most specific domain "
					+ mostSpecificDomain.getLocalName());

			if (!mostSpecificDomain.hasEquivalentClass(ontClass) && mostSpecificDomain.hasSuperClass(ontClass)) {
				continue;
			}

			if (ontologyModel.asOntModel().getOntProperty(ontProperty.getURI()) == null) {
				// The ont property has been introduced by the JENA reasoner
				logger_create_bean_methods.trace(ontProperty + "has been introduced by the reasoner!");
				continue;
			}

			if (!causesNameClash(ontClass, ontProperty, ontologyModel)) {
				Restriction restriction = OntologyUtils.hasRestrictionOnProperty(ontClass, ontProperty);
				if (restriction != null) {
					createBeanMethodsForRestrictions(restriction, owner, ontologyModel);
				} else {
					OntResource range = OntologyUtils.getMostSpecificRange(ontProperty, ontologyModel.getInfOntModel(),
							ontologyModel.getInfOntModel());
					if (range != null) {
						if (range.isURIResource()) {
							logger_create_bean_methods.trace("RANGE " + range.getURI());
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

	private void createBeanMethods(OntProperty ontProperty, AbstractOntologyCodeClass owner,
			AbstractOntologyCodeClass rangeClass, OntologyCodeModel ontologyCodeModel) {
		Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
		domain.add(rangeClass);
		ontologyCodeModel.createMethod(OntologyCodeMethodType.GET, ontProperty, owner, null, rangeClass);
		ontologyCodeModel.createMethod(OntologyCodeMethodType.SET, ontProperty, owner, domain, null);
		ontologyCodeModel.createMethod(OntologyCodeMethodType.ADD_ALL, ontProperty, owner, domain, null);
		ontologyCodeModel.createMethod(OntologyCodeMethodType.REMOVE_ALL, ontProperty, owner, domain, null);
	}

	private void createBeanMethodsForAnonClass(OntProperty ontProperty, OntResource range,
			AbstractOntologyCodeClass owner, OntologyCodeModel ontologyCodeModel) {
		BooleanAnonClass anonClass = manageAnonClasses(range.asClass(), ontologyCodeModel);
		createBeanMethods(ontProperty, owner, anonClass, ontologyModel);
	}

	private void createBeanMethodsForClassRange(OntProperty ontProperty, OntClass rangeOntClass,
			AbstractOntologyCodeClass owner, OntologyCodeModel ontologyCodeModel) {
		logger_inspect
				.trace(owner.getOntResource().getURI() + " " + ontProperty.getURI() + " " + rangeOntClass.getURI());
		/*
		 * Range of the property is a class
		 */

		OntologyCodeInterface rangeClass = null;

		if (ontProperty.isDatatypeProperty()) {
			try {
				rangeClass = ontologyCodeModel.createOntologyClass(rangeOntClass, DatatypeCodeInterface.class);
			} catch (NotAvailableOntologyCodeEntityException e) {
				e.printStackTrace();
			}
		} else {
			try {
				rangeClass = ontologyCodeModel.createOntologyClass(rangeOntClass, BeanOntologyCodeInterface.class);
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

	private void createBeanMethodsForOntPropertyWithoutRange(OntProperty ontProperty, AbstractOntologyCodeClass owner,
			OntologyCodeModel ontologyModel) {

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

	private void createBeanMethodsForRestrictions(Restriction restriction, AbstractOntologyCodeClass owner,
			OntologyCodeModel ontologyModel) {
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
					createBeanMethodsForAnonClass(onProperty, onClass.as(IntersectionClass.class), owner,
							ontologyModel);
				} else if (onClass.canAs(ComplementClass.class)) {
					createBeanMethodsForAnonClass(onProperty, onClass.as(ComplementClass.class), owner, ontologyModel);
				}
			} else if (!onProperty.isDatatypeProperty()) {
				createBeanMethodsForClassRange(onProperty, ontologyModel.asOntModel().getOntClass(onClass.getURI()),
						owner, ontologyModel);
			} else {
				OntProperty datatypeProperty = ontologyModel.asOntModel().getOntProperty(onProperty.getURI());
				OntClass rangeOntClass = ontologyModel.getInfOntModel().getOntClass(onClass.getURI());
				logger_inspect.trace(owner.getOntResource().getURI() + " " + datatypeProperty.getURI() + " "
						+ rangeOntClass.getURI());
				logger_inspect.trace(owner.getOntResource().getURI() + " " + datatypeProperty.getURI() + " "
						+ rangeOntClass.getURI());
				createBeanMethodsForClassRange(datatypeProperty, rangeOntClass, owner, ontologyModel);
			}
		}
	}

	private void createRESTMethod(OntologyCodeModel ontologyModel, AbstractOntologyCodeClass owner,
			OntProperty ontProperty, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass rangeClass,
			OntologyCodeMethodType type) {
		ontologyModel.createMethod(type, ontProperty, owner, domain, rangeClass);
	}

	private void createRESTMethods(AbstractOntologyCodeClass owner, OntologyCodeModel ontologyModel) {

		logger_high_level.trace("Creating REST methods for class " + owner.getOntResource().getURI());

		OntClass ontClass = ontologyModel.getInfOntModel().getOntClass(owner.getOntResource().getURI());
		logger_createRESTMethods.debug("Create methods for class " + ontClass.getURI());

		AbstractOntologyCodeClass beanClass = ontologyModel.getOntologyClass(ontClass, BeanOntologyCodeClass.class);
		for (AbstractOntologyCodeMethod beanMethod : beanClass.getMethods()) {
			createRESTMethod(ontologyModel, owner, beanMethod.getOntResource().asProperty(), beanMethod.getDomain(),
					beanMethod.getRange(), beanMethod.getMethodType());
		}

	}

	@Override
	public OntologyCodeProject generate() {

		OntologyCodeProject project = null;
		try {
			logger.info("Create Bean Project");
			logger_high_level.info("Create Bean Project");
			project = generateBeans();
		} catch (NotAvailableOntologyCodeEntityException e) {
			e.printStackTrace();
		}
		if (project != null && generateRestProject) {
			logger.info("Create REST Project");
			logger_high_level.info("Create REST Project");
			project = generateRestProject(project.getOntologyCodeModel());
		}

		return project;

	}

	private OntologyCodeProject generateBeans() throws NotAvailableOntologyCodeEntityException {

		OntologyCodeModel ontologyCodeModel = new RestOntologyCodeModel(this.ontologyModel.asOntModel());
		ontologyCodeModel.setInfOntModel(this.ontologyModel.getInfOntModel());

		// TODO //FIXME
		this.ontologyModel = ontologyCodeModel;

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
		OntologyCodeInterface ontologyThingInterface = ontologyCodeModel.createOntologyClass(owlThing,
				BeanOntologyCodeInterface.class);
		createBeanMethods(ontologyThingInterface, ontologyCodeModel);

		((JDefinedClass) ontologyThingInterface.asJDefinedClass())
				.method(JMod.PUBLIC, ontologyCodeModel.asJCodeModel().VOID, "setId").param(String.class, "id");
		((JDefinedClass) ontologyThingInterface.asJDefinedClass()).method(JMod.PUBLIC, String.class, "getId");
		((JDefinedClass) ontologyThingInterface.asJDefinedClass())
				.method(JMod.PUBLIC, ontologyCodeModel.asJCodeModel().VOID, "setIsCompleted")
				.param(Boolean.class, "isCompleted");
		((JDefinedClass) ontologyThingInterface.asJDefinedClass()).method(JMod.PUBLIC, Boolean.class, "getIsCompleted");

		/*
		 * Create bean for owl:Thing
		 */
		/*
		 * Create java bean and Jena-based class.
		 */
		ontologyCodeModel.createOntologyClass(owlThing, BeanOntologyCodeClass.class);
		ontologyCodeModel.createOntologyClass(owlThing, JenaOntologyCodeClass.class);
		
		
//		ontologyCodeModel.createOntologyClass(ontModel.getOntClass("https://w3id.org/italia/onto/Project/PublicInvestmentProject"), BeanOntologyCodeInterface.class);
//		ontologyCodeModel.createOntologyClass(ontModel.getOntClass("https://w3id.org/italia/onto/Project/PublicInvestmentProject"), BeanOntologyCodeClass.class);
//		ontologyCodeModel.createOntologyClass(ontModel.getOntClass("https://w3id.org/italia/onto/Project/PublicInvestmentProject"), JenaOntologyCodeClass.class);
		

		logger.info("Visiting class hierarchy");
		
		ontModel.setStrictMode(false);

		List<OntClass> roots = OntTools.namedHierarchyRoots(ontModel);

		for (OntClass root : roots) {
			visitHierarchyTreeForBeans(root, ontologyCodeModel);
		}

		// Extends for interfaces
		logger.info("Creating bean classes");
		Map<OntResource, BeanOntologyCodeInterface> interfaceClassMap = ontologyCodeModel
				.getOntologyClasses(BeanOntologyCodeInterface.class);
		interfaceClassMap.values().forEach(_interface -> {
			OntClass interfaceOntClass = ontologyCodeModel.getInfOntModel()
					.getOntClass(_interface.getOntResource().getURI());
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

		Map<OntResource, BeanOntologyCodeClass> beanClassMap = ontologyCodeModel
				.getOntologyClasses(BeanOntologyCodeClass.class);

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
			OntologyCodeInterface ontologyInterface = ontologyCodeModel.getOntologyClass(ontClass,
					BeanOntologyCodeInterface.class);
			ExtendedIterator<OntClass> superClassIt = ontClass.listSuperClasses();
			List<OntologyCodeInterface> ontologySuperInterfaces = new ArrayList<OntologyCodeInterface>();
			ontologySuperInterfaces.add(ontologyCodeModel.getOntologyClass(
					ModelFactory.createOntologyModel().createOntResource(OWL2.Thing.getURI()),
					BeanOntologyCodeInterface.class));

			if (ontologyInterface != null)
				ontologySuperInterfaces.add(ontologyInterface);

			while (superClassIt.hasNext()) {
				OntClass superClass = superClassIt.next();
				if (superClass.isURIResource()) {
					OntologyCodeInterface ontologySuperInterface = ontologyCodeModel.getOntologyClass(superClass,
							BeanOntologyCodeInterface.class);
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
		logger.info("Creating jena classes");
		Map<OntResource, JenaOntologyCodeClass> jenaClassMap = ontologyCodeModel
				.getOntologyClasses(JenaOntologyCodeClass.class);
		ontResources = jenaClassMap.keySet();
		final Set<AbstractOntologyCodeClassImpl> jenaClasses = new HashSet<AbstractOntologyCodeClassImpl>();
		for (OntResource ontResource : ontResources) {
			OntologyCodeClass ontologyClass = jenaClassMap.get(ontResource);
			jenaClasses.add(ontologyClass);
		}

		jenaClasses.forEach(ontologyClass -> {

			OntClass ontClass = ontologyCodeModel.getInfOntModel().getOntClass(ontologyClass.getOntResource().getURI());
			logger_high_level.info("Creating JENA classes of " + ontClass.getURI());
			OntologyCodeInterface ontologyInterface = ontologyCodeModel.getOntologyClass(ontClass,
					BeanOntologyCodeInterface.class);

			ExtendedIterator<OntClass> superClassIt = ontClass.listSuperClasses();
			List<OntologyCodeInterface> ontologySuperInterfaces = new ArrayList<OntologyCodeInterface>();
			ontologySuperInterfaces.add(ontologyCodeModel.getOntologyClass(
					ModelFactory.createOntologyModel().createOntResource(OWL2.Thing.getURI()),
					BeanOntologyCodeInterface.class));

			if (ontologyInterface != null)
				ontologySuperInterfaces.add(ontologyInterface);

			while (superClassIt.hasNext()) {
				OntClass superClass = superClassIt.next();
				if (superClass.isURIResource()) {
					OntologyCodeInterface ontologySuperInterface = ontologyCodeModel.getOntologyClass(superClass,
							BeanOntologyCodeInterface.class);
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

		// TODO generate swagger description

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

		visitHierarchyTreeForRest(ontModel.getOntClass(OWL2.Thing.getURI()), restOntologyModel);

		for (OntClass root : roots) {
			visitHierarchyTreeForRest(root, restOntologyModel);
		}

		return new OntologyCodeProject(ontologyBaseURI, restOntologyModel);

	}

	public void generateSwaggerDescription(String swaggerFolder) {

		LizardConfiguration l = LizardConfiguration.getInstance();

		ImmutableMultimap.Builder<String, AbstractOntologyCodeClass> builderMap = new ImmutableMultimap.Builder<>();

		AbstractOntologyCodeClass thingClass = this.ontologyModel.getOntologyClass(
				this.ontologyModel.asOntModel().getOntClass(OWL2.Thing.getURI()), BeanOntologyCodeInterface.class);

		this.ontologyModel.getEntityMap().get(BeanOntologyCodeClass.class).values().forEach(occ -> {
			builderMap.put(occ.getPackageName(), occ);
			builderMap.put(occ.getPackageName(), thingClass);
		});

		Multimap<String, AbstractOntologyCodeClass> map = builderMap.build();

		map.keySet().forEach(pack -> {
			String ontologyBasePath = "/" + pack.replaceAll("\\.", "_");
			new File(swaggerFolder + ontologyBasePath).mkdirs();
			DescriptionGenerator dg = new DescriptionGenerator();
			dg.setApiDescription("Rest services for accessing the api defined in the package " + pack
					+ " for accessing the corresponding ontology.");
			dg.setApiTitle(pack);
			dg.setApiVersion(l.getApiVersion());
			dg.setBasePath(ontologyBasePath);
			dg.setContactName(l.getContactName());
			dg.setContanctEmail(l.getContanctEmail());
			dg.setHost(l.getHost());
			dg.setLicenseName(l.getLicenseName());
			dg.setLicenseUrl(l.getLicenseUrl());
			dg.setClasses(map.get(pack));
			try {
				FileOutputStream fos = new FileOutputStream(
						new File(swaggerFolder + ontologyBasePath + "/swagger.json"));
				fos.write(dg.generateSwaggerJSONStringDescription().getBytes());
				fos.flush();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

	}

	private BooleanAnonClass manageAnonClasses(OntClass ontClass, OntologyCodeModel ontologyModel) {
		return ontologyModel.createAnonClass(ontClass);
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
				((JDefinedClass) ontologyInterface.asJDefinedClass())
						.method(JMod.PUBLIC, ontologyInterface.getJCodeModel().VOID, "setId").param(String.class, "id");
				((JDefinedClass) ontologyInterface.asJDefinedClass()).method(JMod.PUBLIC, String.class, "getId");
				((JDefinedClass) ontologyInterface.asJDefinedClass())
						.method(JMod.PUBLIC, ontologyInterface.getJCodeModel().VOID, "setIsCompleted")
						.param(Boolean.class, "isCompleted");
				((JDefinedClass) ontologyInterface.asJDefinedClass()).method(JMod.PUBLIC, Boolean.class,
						"getIsCompleted");
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

	private static boolean hasMethod(JDefinedClass jdefClass, String methodName) {
		for (JMethod m : jdefClass.methods()) {
			if (m.name().equals(methodName)) {
				return true;
			}
		}
		return false;
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

}
