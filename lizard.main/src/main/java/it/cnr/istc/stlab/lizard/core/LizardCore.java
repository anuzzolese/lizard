package it.cnr.istc.stlab.lizard.core;

import it.cnr.istc.stlab.lizard.commons.MavenUtils;
import it.cnr.istc.stlab.lizard.commons.OntologyCodeProject;
import it.cnr.istc.stlab.lizard.commons.exception.NotAvailableOntologyCodeEntityException;
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

import java.io.BufferedWriter;
import java.io.File;
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
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.OntTools;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
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

public class LizardCore implements OntologyCodeGenerationRecipe {

	private static Logger logger = LoggerFactory.getLogger(LizardCore.class);

	private URI ontologyURI;
	private RestOntologyCodeModel restOntologyModel;
	private OntologyCodeModel ontologyModel;

	public LizardCore(URI ontologyURI) {
		this.ontologyURI = ontologyURI;

		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		ontModel.read(ontologyURI.toString());

		validateOntology(ontModel);

		this.ontologyModel = new RestOntologyCodeModel(ontModel);
	}

	private void validateOntology(OntModel ontModel) {

		// TODO JENA resasoner takes so long, use another reasoners

		// logger.trace("Validating inf model");
		// ValidityReport validity = ontModel.validate();
		// if (!validity.isValid()) {
		// for (Iterator<Report> in = validity.getReports(); in.hasNext();) {
		// logger.error(" - " + in.next());
		// }
		// throw new OntologyNotValidException("Ontology not valid!");
		// } else {
		// logger.trace("Ontology valid!");
		// }

		logger.warn("Validation of the ontology not performed!");

	}

	@Override
	public OntologyCodeProject generate() {

		OntologyCodeProject project = null;
		try {
			project = generateBeans();
		} catch (NotAvailableOntologyCodeEntityException e) {
			e.printStackTrace();
		}
		if (project != null) {
			OntologyCodeModel beansModel = project.getOntologyCodeModel();
			project = generateRestProject(beansModel);
		}

		return project;

	}

	private OntologyCodeProject generateBeans() throws NotAvailableOntologyCodeEntityException {

		OntologyCodeModel ontologyCodeModel = new RestOntologyCodeModel(this.ontologyModel.asOntModel());
		OntModel ontModel = ontologyCodeModel.asOntModel();

		String baseURI = ontModel.getNsPrefixURI("");
		if (baseURI == null) {
			ExtendedIterator<Ontology> ontologyIt = ontModel.listOntologies();
			while (ontologyIt.hasNext())
				baseURI = ontologyIt.next().getURI();
			if (baseURI == null)
				ontModel.setNsPrefix("", ontologyURI.toString());
			else
				ontModel.setNsPrefix("", baseURI);
		}

		URI ontologyBaseURI;
		try {
			ontologyBaseURI = new URI(baseURI);
		} catch (URISyntaxException e) {
			ontologyBaseURI = ontologyURI;
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

			OntClass ontClass = (OntClass) ontologyClass.getOntResource();
			OntologyCodeInterface ontologyInterface = ontologyCodeModel.getOntologyClass(ontClass, BeanOntologyCodeInterface.class);

			ExtendedIterator<OntClass> superClassIt = ontClass.listSuperClasses(false);
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

			OntClass ontClass = (OntClass) ontologyClass.getOntResource();
			OntologyCodeInterface ontologyInterface = ontologyCodeModel.getOntologyClass(ontClass, BeanOntologyCodeInterface.class);

			ExtendedIterator<OntClass> superClassIt = ontClass.listSuperClasses(false);
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

		OntModel ontModel = restOntologyModel.asOntModel();

		String baseURI = ontModel.getNsPrefixURI("");
		if (baseURI == null) {
			ExtendedIterator<Ontology> ontologyIt = ontModel.listOntologies();
			while (ontologyIt.hasNext())
				baseURI = ontologyIt.next().getURI();
			if (baseURI == null)
				ontModel.setNsPrefix("", ontologyURI.toString());
			else
				ontModel.setNsPrefix("", baseURI);
		}

		URI ontologyBaseURI;
		try {
			ontologyBaseURI = new URI(baseURI);
		} catch (URISyntaxException e) {
			ontologyBaseURI = ontologyURI;
		}

		List<OntClass> roots = OntTools.namedHierarchyRoots(ontModel);

		for (OntClass root : roots) {
			visitHierarchyTreeForRest(root, restOntologyModel);
		}

		/*
		 * CodeWriter writer = new SingleStreamCodeWriter(System.out); try { codeModel.build(writer); } catch (IOException e) { e.printStackTrace(); }
		 */

		return new OntologyCodeProject(ontologyBaseURI, restOntologyModel);

	}

	private BooleanAnonClass manageAnonClasses(OntClass ontClass, OntologyCodeModel ontologyModel) {
		return ontologyModel.createAnonClass(ontClass);
	}

	private void visitHierarchyTreeForRest(OntClass ontClass, OntologyCodeModel ontologyModel) {

		logger.debug("Visit hierarchy for rest " + ontClass.getURI());

		OntologyCodeClass ontologyClass;
		try {
			if (ontologyModel.getOntologyClass(ontClass, BeanOntologyCodeClass.class) != null) {
				ontologyClass = ontologyModel.createOntologyClass(ontClass, RestOntologyCodeClass.class);
				createMethods(ontologyClass, ontologyModel);
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

	private void visitHierarchyTreeForBeans(OntClass ontClass, OntologyCodeModel ontologyModel) {

		OntologyCodeInterface ontologyInterface = null;
		try {
			ontologyInterface = ontologyModel.createOntologyClass(ontClass, BeanOntologyCodeInterface.class);
		} catch (NotAvailableOntologyCodeEntityException e) {
			e.printStackTrace();
		}

		if (ontologyInterface != null) {

			createBeanMethods(ontologyInterface, ontologyModel);

			// TODO
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

				if (subClass.isURIResource())
					visitHierarchyTreeForBeans(subClass, ontologyModel);
				else
					manageAnonClasses(subClass, ontologyModel);
			}
		}

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

	private void createMethods(AbstractOntologyCodeClass owner, OntologyCodeModel ontologyModel) {

		OntClass ontClass = ontologyModel.asOntModel().getOntClass(owner.getOntResource().getURI());

		Set<OntProperty> props = ontClass.listDeclaredProperties().toSet();
		props.addAll(getRestrictionsPropertyDomain(ontClass));
		Iterator<OntProperty> propIt = props.iterator();

		while (propIt.hasNext()) {

			OntProperty ontProperty = propIt.next();
			OntResource range = ontProperty.getRange();

			if (range != null) {
				if (range.isURIResource()) {
					if (range.isClass()) {

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

						if (rangeClass == null) {
							System.out.println(getClass() + " ATTENTION ");
						}

						Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
						domain.add(rangeClass);

						ontologyModel.createMethod(OntologyCodeMethodType.Get, ontProperty, owner, domain, rangeClass);
						ontologyModel.createMethod(OntologyCodeMethodType.Set, ontProperty, owner, domain, rangeClass);
						ontologyModel.createMethod(OntologyCodeMethodType.Delete, ontProperty, owner, domain, rangeClass);
					}
				} else {

					BooleanAnonClass anonClass = manageAnonClasses(range.asClass(), ontologyModel);

					Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
					domain.add(anonClass);

					ontologyModel.createMethod(OntologyCodeMethodType.Get, ontProperty, owner, domain, anonClass);
					ontologyModel.createMethod(OntologyCodeMethodType.Set, ontProperty, owner, domain, anonClass);
					ontologyModel.createMethod(OntologyCodeMethodType.Delete, ontProperty, owner, domain, anonClass);

				}
			} else {
				OntResource thing = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM).createOntResource(OWL2.Thing.getURI());

				Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
				domain.add(ontologyModel.getOntologyClass(thing, BeanOntologyCodeInterface.class));

				ontologyModel.createMethod(OntologyCodeMethodType.Get, ontProperty, owner, domain, ontologyModel.getOntologyClass(thing, RestOntologyCodeClass.class));
				ontologyModel.createMethod(OntologyCodeMethodType.Set, ontProperty, owner, domain, ontologyModel.getOntologyClass(thing, RestOntologyCodeClass.class));
				ontologyModel.createMethod(OntologyCodeMethodType.Delete, ontProperty, owner, domain, ontologyModel.getOntologyClass(thing, RestOntologyCodeClass.class));
			}

		}

		ExtendedIterator<OntClass> superClassesIt = ontClass.listSuperClasses();

		while (superClassesIt.hasNext()) {

			OntClass superClass = superClassesIt.next();

			if (superClass.isRestriction()) {
				Restriction restriction = superClass.asRestriction();
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

						ontologyModel.createMethod(OntologyCodeMethodType.Get, onProperty, owner, null, rangeClass);
						ontologyModel.createMethod(OntologyCodeMethodType.Set, onProperty, owner, null, rangeClass);
						ontologyModel.createMethod(OntologyCodeMethodType.Delete, onProperty, owner, null, rangeClass);

					} catch (NotAvailableOntologyCodeEntityException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private Set<OntProperty> getRestrictionsPropertyDomain(OntClass c) {
		Set<OntProperty> r = new HashSet<OntProperty>();
		this.ontologyModel.asOntModel().listAllOntProperties().forEachRemaining(op -> {
			if (op.getDomain() != null && op.getDomain().isClass()) {
				OntClass ac = op.getDomain().asClass();
				if (ac.isUnionClass()) {
					BooleanClassDescription booleanClassDescription = ac.asUnionClass();
					ExtendedIterator<? extends OntClass> members = booleanClassDescription.listOperands();
					while (members.hasNext()) {
						OntClass member = members.next();
						if (member.getURI().equals(c.getURI())) {
							r.add(op);
						}
					}
				}
			}
		});
		return r;
	}

	private void createBeanMethods(AbstractOntologyCodeClass owner, OntologyCodeModel ontologyModel) {
		OntClass ontClass = ontologyModel.asOntModel().getOntClass(owner.getOntResource().getURI());

		Set<OntProperty> props = ontClass.listDeclaredProperties().toSet();
		props.addAll(getRestrictionsPropertyDomain(ontClass));
		Iterator<OntProperty> propIt = props.iterator();

		while (propIt.hasNext()) {

			OntProperty ontProperty = propIt.next();
			OntResource range = ontProperty.getRange();

			if (range != null) {
				if (range.isURIResource()) {
					if (range.isClass()) {

						/*
						 * Range of the property is a class
						 */

						OntologyCodeInterface rangeClass = null;
						OntClass rangeOntClass = ModelFactory.createOntologyModel().createClass(range.getURI());

						if (ontProperty.isDatatypeProperty()) {

							// if (!hasTypeMapper(range.getURI())) {
							try {
								rangeClass = ontologyModel.createOntologyClass(rangeOntClass, DatatypeCodeInterface.class);
							} catch (NotAvailableOntologyCodeEntityException e) {
								e.printStackTrace();
							}
							// }

						} else {
							try {
								rangeClass = ontologyModel.createOntologyClass(rangeOntClass, BeanOntologyCodeInterface.class);
							} catch (NotAvailableOntologyCodeEntityException e) {
								e.printStackTrace();
							}
						}

						Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
						if (rangeClass != null)
							domain.add(rangeClass);

						ontologyModel.createMethod(OntologyCodeMethodType.Get, ontProperty, owner, null, rangeClass);
						ontologyModel.createMethod(OntologyCodeMethodType.Set, ontProperty, owner, domain, null);
						ontologyModel.createMethod(OntologyCodeMethodType.Delete, ontProperty, owner, domain, null);

					}
				} else {

					BooleanAnonClass anonClass = manageAnonClasses(range.asClass(), ontologyModel);

					Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
					domain.add(anonClass);

					ontologyModel.createMethod(OntologyCodeMethodType.Get, ontProperty, owner, null, anonClass);
					ontologyModel.createMethod(OntologyCodeMethodType.Set, ontProperty, owner, domain, null);
					ontologyModel.createMethod(OntologyCodeMethodType.Delete, ontProperty, owner, domain, null);
				}
			} else {

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

				Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
				domain.add(rangeClass);

				ontologyModel.createMethod(OntologyCodeMethodType.Get, ontProperty, owner, null, rangeClass);
				ontologyModel.createMethod(OntologyCodeMethodType.Set, ontProperty, owner, domain, null);
				ontologyModel.createMethod(OntologyCodeMethodType.Delete, ontProperty, owner, domain, null);
			}

		}

		ExtendedIterator<OntClass> superClassesIt = ontClass.listSuperClasses();
		while (superClassesIt.hasNext()) {
			OntClass superClass = superClassesIt.next();
			if (superClass.isRestriction()) {
				Restriction restriction = superClass.asRestriction();
				OntProperty onProperty = restriction.getOnProperty();
				Resource onClass = null;
				if (restriction.isSomeValuesFromRestriction()) {
					onClass = restriction.asSomeValuesFromRestriction().getSomeValuesFrom();
				} else if (restriction.isAllValuesFromRestriction()) {
					onClass = restriction.asAllValuesFromRestriction().getAllValuesFrom();
				}

				if (onClass != null) {

					if (!onProperty.isDatatypeProperty()) {
						try {
							AbstractOntologyCodeClass rangeClass = ontologyModel.createOntologyClass(ontologyModel.asOntModel().getOntResource(onClass), BeanOntologyCodeInterface.class);

							Collection<AbstractOntologyCodeClass> domain = new ArrayList<AbstractOntologyCodeClass>();
							domain.add(rangeClass);

							ontologyModel.createMethod(OntologyCodeMethodType.Get, onProperty, owner, null, rangeClass);
							ontologyModel.createMethod(OntologyCodeMethodType.Set, onProperty, owner, null, rangeClass);
							ontologyModel.createMethod(OntologyCodeMethodType.Delete, onProperty, owner, null, rangeClass);

						} catch (NotAvailableOntologyCodeEntityException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) {

		System.setProperty("M2_HOME", "/Users/lgu/Programs/apache-maven");
		System.setProperty("JAVA_HOME", "/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home");

		registerDatatypes();

		URI uri = null;

		try {
			// uri = new URI("http://www.ontologydesignpatterns.org/cp/owl/timeindexedsituation.owl");
			// uri = new URI("http://stlab.istc.cnr.it/documents/mibact/cultural-ON_xml.owl");
			// uri = new URI("http://www.ontologydesignpatterns.org/ont/mario/tagging.owl");
			// uri = new URI("http://www.ontologydesignpatterns.org/ont/framester/framester.owl");
			// uri = new URI("http://www.ontologydesignpatterns.org/ont/mario/music.owl");
			uri = new URI("http://www.ontologydesignpatterns.org/ont/mario/person.owl");
			// uri = new URI("/Users/lgu/Desktop/prova.owl");
			// uri = new URI("/Users/lgu/Desktop/cga.owl");
			// uri = new URI("vocabs/foaf.rdf");

			OntologyCodeGenerationRecipe codegen = new LizardCore(uri);
			OntologyCodeProject ontologyCodeProject = codegen.generate();

			try {
				File testFolder = new File("test_out");
				if (testFolder.exists()) {
					System.out.println("esists " + testFolder.getClass());
					FileUtils.deleteDirectory(testFolder);
				} else {
					System.out.println("not esists");
				}
				File src = new File("test_out/src/main/java");
				File resources = new File("test_out/src/main/resources");
				File test = new File("test_out/src/test/java");
				if (!src.exists())
					src.mkdirs();
				if (!resources.exists())
					resources.mkdirs();
				if (!test.exists())
					test.mkdirs();

				CodeWriter writer = new FileCodeWriter(src, "UTF-8");
				ontologyCodeProject.getOntologyCodeModel().asJCodeModel().build(writer);
				((LizardCore) codegen).createServiceAnnotations(new File("test_out"), ontologyCodeProject.getOntologyCodeModel());

				/*
				 * Generate the POM descriptor file and build the project as a Maven project.
				 */
				File pom = new File("test_out/pom.xml");
				Writer pomWriter = new FileWriter(new File("test_out/pom.xml"));
				Map<String, String> dataModel = new HashMap<String, String>();
				dataModel.put("artifactId", ontologyCodeProject.getArtifactId());
				dataModel.put("groupId", ontologyCodeProject.getGroupId());
				MavenUtils.generatePOM(pomWriter, dataModel);
				MavenUtils.buildProject(pom);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
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

	private static void registerDatatypes() {
		// TODO let register custom datatype
		// TypeMapper.getInstance().registerDatatype(new XSDNonNegativeIntegerType());
	}

}
