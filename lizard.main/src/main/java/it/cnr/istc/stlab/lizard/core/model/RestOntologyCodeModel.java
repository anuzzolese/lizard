package it.cnr.istc.stlab.lizard.core.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.BooleanClassDescription;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

import it.cnr.istc.stlab.lizard.commons.AnonClassType;
import it.cnr.istc.stlab.lizard.commons.exception.ClassAlreadyExistsException;
import it.cnr.istc.stlab.lizard.commons.exception.NotAvailableOntologyCodeEntityException;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClassImpl;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.datatype.DatatypeCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeClassType;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;
import it.cnr.istc.stlab.lizard.core.OntologyProjectGenerationRecipe;
import it.cnr.istc.stlab.lizard.core.anonymous.AnonymousClassBuilder;

public class RestOntologyCodeModel implements OntologyCodeModel {

	private static Logger logger = LoggerFactory.getLogger(RestOntologyCodeModel.class);

	private OntologyCodeModel apiCodeModel;
	protected JCodeModel codeModel;
	protected OntModel ontModel;
	protected OntModel infOntModel;

	protected Map<OntResource, Set<AbstractOntologyCodeMethod>> methodMap;

	protected Map<Class<? extends AbstractOntologyCodeClass>, Map<OntResource, AbstractOntologyCodeClass>> entityMap;

	public RestOntologyCodeModel(OntModel ontModel) {
		this.codeModel = new JCodeModel();
		this.ontModel = ontModel;
		this.methodMap = new HashMap<OntResource, Set<AbstractOntologyCodeMethod>>();
		this.entityMap = new HashMap<Class<? extends AbstractOntologyCodeClass>, Map<OntResource, AbstractOntologyCodeClass>>();

	}

	public RestOntologyCodeModel(OntologyCodeModel apiCodeModel) {
		this.ontModel = apiCodeModel.asOntModel();
		this.codeModel = apiCodeModel.asJCodeModel();
		this.entityMap = apiCodeModel.getEntityMap();
		this.methodMap = apiCodeModel.getMethodMap();
	}

	public OntologyCodeModel getApiCodeModel() {
		return apiCodeModel;
	}

	@SuppressWarnings({ "unchecked" })
	private <T extends AbstractOntologyCodeClass> T createBeanClass(OntResource resource) {

		OntologyCodeClass ontologyClass = null;
		try {
			if (resource.isURIResource()) {
				ontologyClass = new BeanOntologyCodeClass(resource, this, codeModel);
			} else {
				ontologyClass = createAnonClass((OntClass) resource);
			}

			Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(BeanOntologyCodeClass.class);

			if (beanClasses == null) {
				beanClasses = new HashMap<OntResource, AbstractOntologyCodeClass>();
				entityMap.put(BeanOntologyCodeClass.class, beanClasses);
			}
			beanClasses.put(resource, ontologyClass);

		} catch (ClassAlreadyExistsException e) {
			Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(BeanOntologyCodeClass.class);
			if (beanClasses != null)
				ontologyClass = (OntologyCodeClass) beanClasses.get(resource);
		}
		return (T) ontologyClass;
	}

	@SuppressWarnings({ "unchecked" })
	private <T extends OntologyCodeInterface> T createInterface(OntResource resource) {

		OntologyCodeInterface ontologyInterface = null;
		try {
			if (resource.isURIResource()) {
				ontologyInterface = new BeanOntologyCodeInterface(resource, this, codeModel);
			}

			Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(BeanOntologyCodeInterface.class);
			if (beanClasses == null) {

				beanClasses = new HashMap<OntResource, AbstractOntologyCodeClass>();
				entityMap.put(BeanOntologyCodeInterface.class, beanClasses);
			}
			beanClasses.put(resource, ontologyInterface);

		} catch (ClassAlreadyExistsException e) {
			Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(BeanOntologyCodeInterface.class);
			if (beanClasses != null)
				ontologyInterface = (OntologyCodeInterface) beanClasses.get(resource);
		}
		return (T) ontologyInterface;
	}

	@SuppressWarnings({ "unchecked" })
	private <T extends AbstractOntologyCodeClass> T createJenaClass(OntResource resource) {

		OntologyCodeClass ontologyClass = null;
		try {
			if (resource.isURIResource()) {
				ontologyClass = new JenaOntologyCodeClass(resource, this, codeModel);
			}

			Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(JenaOntologyCodeClass.class);
			if (beanClasses == null) {
				beanClasses = new HashMap<OntResource, AbstractOntologyCodeClass>();
				entityMap.put(JenaOntologyCodeClass.class, beanClasses);
			}
			beanClasses.put(resource, ontologyClass);

		} catch (ClassAlreadyExistsException e) {
			Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(JenaOntologyCodeClass.class);
			if (beanClasses != null)
				ontologyClass = (JenaOntologyCodeClass) beanClasses.get(resource);
		}
		return (T) ontologyClass;
	}

	@SuppressWarnings({ "unchecked" })
	private <T extends AbstractOntologyCodeClass> T createRestClass(OntResource resource) {

		OntologyCodeClass ontologyClass = null;
		try {
			if (resource.isURIResource()) {
				ontologyClass = new RestOntologyCodeClass(resource, this, codeModel);
			}

			Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(RestOntologyCodeClass.class);
			if (beanClasses == null) {
				beanClasses = new HashMap<OntResource, AbstractOntologyCodeClass>();
				entityMap.put(RestOntologyCodeClass.class, beanClasses);
			}
			beanClasses.put(resource, ontologyClass);

		} catch (ClassAlreadyExistsException e) {
			Map<OntResource, AbstractOntologyCodeClass> beanClasses = entityMap.get(RestOntologyCodeClass.class);
			if (beanClasses != null)
				ontologyClass = (RestOntologyCodeClass) beanClasses.get(resource);
		}
		return (T) ontologyClass;
	}

	@Override
	public AbstractOntologyCodeMethod createMethod(OntologyCodeMethodType methodType, OntResource methodResource,
			AbstractOntologyCodeClass owner, Collection<AbstractOntologyCodeClass> domain,
			AbstractOntologyCodeClass range) {

		AbstractOntologyCodeMethod ontologyMethod = null;

		if (logger.isDebugEnabled()) {
			logger.trace(
					"Create method for " + methodType + " " + owner.getOntResource() + " " + methodResource.getURI());
			if (range != null) {
				logger.trace("RANGE: " + range.getOntResource().getURI());
			}
			if (domain != null) {
				domain.forEach(d -> {
					logger.trace("DOMAIN " + d.getOntResource().getURI());
				});
			}
		}

		if (owner != null) {

			Set<AbstractOntologyCodeMethod> existingMethodsToRemove = new HashSet<AbstractOntologyCodeMethod>();
			Set<AbstractOntologyCodeMethod> existingMethods = owner.getMethods(methodResource);

			if (existingMethods != null) {

				for (AbstractOntologyCodeMethod existingMethod : existingMethods) {

					// Searching for a compatible already defined method
					if (existingMethod.getMethodType() != methodType) {
						continue;
					}

					// Same type and same resource
					if (!existingMethod.getMethodType().equals(OntologyCodeMethodType.GET)) {

						// It is compatible only if they have same domains and same range
						if (existingMethod.getDomain() != null && domain != null) {

							Set<String> urisDomainExistingMethod = new HashSet<>();

							for (AbstractOntologyCodeClass domainClass : existingMethod.getDomain()) {
								urisDomainExistingMethod.add(domainClass.getOntResource().getURI());
							}

							Set<String> urisDomainNewMethod = new HashSet<>();
							for (AbstractOntologyCodeClass dom : domain) {
								urisDomainNewMethod.add(dom.getOntResource().getURI());
							}

							if (urisDomainExistingMethod.equals(urisDomainNewMethod)
									|| urisDomainExistingMethod.size() != urisDomainNewMethod.size()) {

								ontologyMethod = existingMethod;
								break;

							} else if (urisDomainExistingMethod.size() > 0 && urisDomainNewMethod.size() > 0) {

								// The Existing method is compatible with the new one only if its domain is less
								// specific than the new one
								Iterator<String> domainNewIterator = urisDomainNewMethod.iterator();
								Iterator<String> domainExistingIterator = urisDomainExistingMethod.iterator();

								boolean isCompatible = true;

								while (domainNewIterator.hasNext()) {

									String abstractOntologyCodeClassInNewDomain = domainExistingIterator.next();

									boolean isSubsumed = false;

									while (domainExistingIterator.hasNext()) {

										String abstractOntologyCodeClassInExistingDomain = domainExistingIterator
												.next();

										OntClass rangeExistingInf = this.infOntModel
												.getOntClass(abstractOntologyCodeClassInExistingDomain);
										OntClass rangeInf = this.infOntModel
												.getOntClass(abstractOntologyCodeClassInNewDomain);

										if (rangeExistingInf.hasSubClass(rangeInf)) {
											isSubsumed = true;
											break;
										}
									}

									if (!isSubsumed) {
										isCompatible = false;
										break;
									}

								}

								if (isCompatible) {
									ontologyMethod = existingMethod;
								}

							}

						} else if (existingMethod.getDomain() == null && domain == null) {
							ontologyMethod = existingMethod;
							break;
						}
					} else {

						if (existingMethod.getRange() == null || range == null) {
							ontologyMethod = existingMethod;
							break;
						}

						if (existingMethod.getRange().getOntResource().isURIResource()
								&& range.getOntResource().isURIResource()) {

							if (this.infOntModel == null)
								throw new RuntimeException();

							OntClass rangeExistingInf = this.infOntModel
									.getOntClass(existingMethod.getRange().getOntResource().getURI());
							OntClass rangeInf = this.infOntModel.getOntClass(range.getOntResource().getURI());

							if (rangeExistingInf.hasSubClass(rangeInf)) {
								// it the existing method's range is superclass of the new one, it is not need
								// to add the new one
								ontologyMethod = existingMethod;
							} else {
								// the existing method has to be substituted by the new one
								existingMethodsToRemove.add(existingMethod);
							}
						} else {

							// FIXME ANON
							ontologyMethod = existingMethod;
						}
					}
				}
			} else {

				logger.trace("The class " + owner.getOntResource().getURI() + " does not contain any method for "
						+ methodResource.getURI() + " yet");

			}

			for (AbstractOntologyCodeMethod methodToRemove : existingMethodsToRemove) {
				existingMethods.remove(methodToRemove);
			}
		}

		if (ontologyMethod == null) {

			if (BeanOntologyCodeClass.class.isAssignableFrom(owner.getClass())
					|| BeanOntologyCodeInterface.class.isAssignableFrom(owner.getClass())) {

				ontologyMethod = new BeanOntologyCodeMethod(methodType, methodResource, owner, domain, range, this,
						codeModel);

			} else if (JenaOntologyCodeClass.class.isAssignableFrom(owner.getClass())) {

				ontologyMethod = new JenaOntologyCodeMethod(methodType, methodResource, owner, domain, range, this,
						codeModel);

			} else if (RestOntologyCodeClass.class.isAssignableFrom(owner.getClass())) {

				ontologyMethod = new RestOntologyCodeMethod(methodType, methodResource, owner, domain, range, this,
						codeModel);

			}

			Set<AbstractOntologyCodeMethod> ontologyMethods = methodMap.get(methodResource);
			if (ontologyMethods == null) {
				ontologyMethods = new HashSet<>();
				methodMap.put(methodResource, ontologyMethods);
			}
			ontologyMethods.add(ontologyMethod);
			owner.addMethod(ontologyMethod);
		}

		return ontologyMethod;
	}

	private Collection<AbstractOntologyCodeMethod> getMethodsOf(OntResource ontResource, OntologyCodeMethodType type,
			Collection<AbstractOntologyCodeMethod> methods) {
		Collection<AbstractOntologyCodeMethod> result = new HashSet<>();
		for (AbstractOntologyCodeMethod method : methods) {
			if (method.getOntResource().getURI().equals(ontResource.getURI()) && type.equals(method.getMethodType())) {
				result.add(method);
			}
		}
		return result;
	}

	private Collection<String> detectSameErasures(Collection<AbstractOntologyCodeMethod> methods) {
		Collection<String> result = new HashSet<>();
		for (AbstractOntologyCodeMethod method : methods) {
			logger.trace("Method {}, Type {}", method.getOntResource().getLocalName(), method.getMethodType());
			if (getMethodsOf(method.getOntResource(), method.getMethodType(), methods).size() > 1) {
				logger.trace("Raise same erasure");
				result.add(method.getOntResource().getURI());
			}
		}
		return result;
	}

	public AbstractOntologyCodeClassImpl createClassImplements(AbstractOntologyCodeClassImpl ontologyClass,
			OntologyCodeInterface... ontologyInterfaces) {

		// FIXME detect methods with same erasure!
		Set<AbstractOntologyCodeMethod> methodsToImplement = new HashSet<>();

		for (OntologyCodeInterface ontologyInterface : ontologyInterfaces) {
			if (ontologyInterface.getOntologyClassType() == OntologyCodeClassType.Interface) {
				((JDefinedClass) ontologyClass.asJDefinedClass())._implements(ontologyInterface.asJDefinedClass());
				ontologyClass.implementsInterfaces(ontologyInterface);
				methodsToImplement.addAll(ontologyInterface.getMethods());
			}
		}

		// The Resources that provoke the same erasure are implemented as default method
		// in THING class
		Collection<String> ontResourcesProvokeSameErasure = detectSameErasures(methodsToImplement);
		logger.trace("Methods to implent for " + ontologyClass.getOntResource().getLocalName() + " ");
		for (String s : ontResourcesProvokeSameErasure) {
			logger.trace(s);

		}

		for (AbstractOntologyCodeMethod method : methodsToImplement) {
			logger.trace("Method " + method.getOwner().getOntResource().getLocalName() + " "
					+ method.getOntResource().getLocalName() + " " + method.getOwner().getClass().getName());
			if (!ontResourcesProvokeSameErasure.contains(method.getOntResource().getURI())) {
				logger.trace("Do not provoke same erasure problem.");
				createMethod(method.getMethodType(), method.getOntResource(), ontologyClass, method.getDomain(),
						method.getRange());
			} else {
				logger.trace("** Provoke same erasure problem!");
				// TODO Same erasure are managed by generalizing to thing
				AbstractOntologyCodeClass thingInteface = getOntologyClass(
						this.asOntModel().getOntClass(OWL2.Thing.getURI()), BeanOntologyCodeInterface.class);
				Collection<AbstractOntologyCodeClass> domain = new HashSet<>();
				domain.add(thingInteface);
				if (method.getMethodType().equals(OntologyCodeMethodType.GET)) {
					createMethod(method.getMethodType(), method.getOntResource(), ontologyClass, domain,
							method.getRange());
				} else {
					createMethod(method.getMethodType(), method.getOntResource(), ontologyClass, method.getDomain(),
							thingInteface);
				}

				// Fix method on interface
				for (OntologyCodeInterface ontologyInterface : ontologyInterfaces) {
					AbstractOntologyCodeMethod methodInterfaceToRemove = null;
					for (AbstractOntologyCodeMethod m : ontologyInterface.getMethods(method.getOntResource())) {
						logger.trace("Method interface " + method.getOntResource().getLocalName());
						if (m.getMethodType().equals(method.getMethodType())) {
							logger.trace("Removing " + m.getEntityName());
							methodInterfaceToRemove = m;
						}
					}
					ontologyInterface.getMethods(method.getOntResource()).remove(methodInterfaceToRemove);
					if (method.getMethodType().equals(OntologyCodeMethodType.GET)) {
						createMethod(method.getMethodType(), method.getOntResource(), ontologyInterface, domain,
								method.getRange());
					} else {
						createMethod(method.getMethodType(), method.getOntResource(), ontologyInterface,
								method.getDomain(), thingInteface);
					}
				}
			}
		}

		return ontologyClass;

	}

	@Override
	public BooleanAnonClass createAnonClass(OntClass ontResource) {

		BooleanAnonClass anonClass = null;
		if (ontResource.isClass()) {
			OntClass ontClass = (OntClass) ontResource;
			BooleanClassDescription booleanClassDescription = null;
			if (ontClass.isUnionClass()) {
				booleanClassDescription = ontClass.asUnionClass();
			} else if (ontClass.isIntersectionClass()) {
				booleanClassDescription = ontClass.asIntersectionClass();
			} else if (ontClass.isComplementClass()) {
				booleanClassDescription = ontClass.asComplementClass();
			}

			if (booleanClassDescription != null) {

				ExtendedIterator<? extends OntClass> members = booleanClassDescription.listOperands();

				Set<AbstractOntologyCodeClass> memberClasses = new HashSet<AbstractOntologyCodeClass>();
				while (members.hasNext()) {
					OntClass member = members.next();
					logger.debug("ANON class member " + member.getURI());

					AbstractOntologyCodeClass memberClass = null;

					if (member.isURIResource()) {
						memberClass = createInterface(member);
					} else
						memberClass = createAnonClass(member);

					if (memberClass != null) {
						memberClasses.add(memberClass);
					}
				}

				if (!memberClasses.isEmpty()) {

					AbstractOntologyCodeClass[] membs = new AbstractOntologyCodeClass[memberClasses.size()];
					memberClasses.toArray(membs);

					if (ontClass.isUnionClass()) {
						anonClass = createAnonClass(AnonClassType.Union, ontClass, membs);
					} else if (ontClass.isIntersectionClass()) {
						anonClass = createAnonClass(AnonClassType.Intersection, ontClass, membs);
						booleanClassDescription = ontClass.asIntersectionClass();
					} else if (ontClass.isComplementClass()) {
						anonClass = createAnonClass(AnonClassType.Complement, ontClass, membs);
						booleanClassDescription = ontClass.asComplementClass();
					}

					if (anonClass != null) {
						Map<OntResource, AbstractOntologyCodeClass> anonClasses = entityMap.get(BooleanAnonClass.class);
						if (anonClasses == null) {
							anonClasses = new HashMap<OntResource, AbstractOntologyCodeClass>();
							entityMap.put(BooleanAnonClass.class, anonClasses);
						}
						anonClasses.put(ontClass, anonClass);
					}

				}

			}
		}
		return anonClass;
	}

	@SuppressWarnings("unchecked")
	private <T extends AbstractOntologyCodeClass> T createAnonClass(AnonClassType anonClassType, OntResource anon,
			AbstractOntologyCodeClass... members) {
		return (T) AnonymousClassBuilder.build(anonClassType, anon, codeModel, members);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends AbstractOntologyCodeClass> T createOntologyClass(OntResource resource,
			Class<T> ontologyEntityClass) throws NotAvailableOntologyCodeEntityException {

		T retrievedClass = getOntologyClass(resource, ontologyEntityClass);
		if (retrievedClass != null) {
			return retrievedClass;
		}

		logger.trace("Creating class of " + resource.getURI());
		if ((OntologyProjectGenerationRecipe.hasTypeMapper(resource.getURI())
				&& !DatatypeCodeInterface.class.isAssignableFrom(ontologyEntityClass))) {

//		if (resource.isProperty() || (OntologyProjectGenerationRecipe.hasTypeMapper(resource.getURI()) TODO check
//				&& !DatatypeCodeInterface.class.isAssignableFrom(ontologyEntityClass))) {
//			logger.trace("Resource is a property {}", resource.isProperty());
			logger.trace("Resource has a TypeMapper but it is not DatatypeCodeInterface {}",
					(OntologyProjectGenerationRecipe.hasTypeMapper(resource.getURI())
							&& !DatatypeCodeInterface.class.isAssignableFrom(ontologyEntityClass)));
			logger.trace("OntologyEntityClass {}", ontologyEntityClass.getName());
			throw new RuntimeException("Cannot create a class of the resource " + resource.getURI());
		}

		T ontologyClass = null;
		if (resource.isAnon()) {
			ontologyClass = (T) createAnonClass(resource.asClass());
		} else {

			if (DatatypeCodeInterface.class.isAssignableFrom(ontologyEntityClass)) {
				try {
					ontologyClass = (T) new DatatypeCodeInterface(resource, this, this.codeModel);
				} catch (ClassAlreadyExistsException e) {
					ontologyClass = (T) getOntologyClass(resource, BeanOntologyCodeInterface.class);
					e.printStackTrace();
				}
			} else if (BeanOntologyCodeClass.class.isAssignableFrom(ontologyEntityClass)) {
//				if (resource.isDataRange() || resource.isDatatypeProperty()) { TODO check
//					throw new RuntimeException();
//				}
				ontologyClass = (T) createBeanClass(resource);
			} else if (BeanOntologyCodeInterface.class.isAssignableFrom(ontologyEntityClass)) {
//				if (resource.isDataRange()) { TODO check
//					throw new RuntimeException();
//				}
				ontologyClass = (T) createInterface(resource);
			} else if (JenaOntologyCodeClass.class.isAssignableFrom(ontologyEntityClass)) {
//				if (resource.isDataRange()) { TODO check
//					throw new RuntimeException();
//				}
				ontologyClass = (T) createJenaClass(resource);
			} else if (RestOntologyCodeClass.class.isAssignableFrom(ontologyEntityClass)) {
				if (resource.isDataRange()) {
					throw new RuntimeException();
				}
				ontologyClass = (T) createRestClass(resource);
			} else {
				throw new NotAvailableOntologyCodeEntityException(ontologyEntityClass);
			}
		}

		return ontologyClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractOntologyCodeClass> T getOntologyClass(OntResource ontResource, Class<T> ontologyClass) {
		if (entityMap.get(ontologyClass) == null)
			return null;
		return (T) entityMap.get(ontologyClass).get(ontResource);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractOntologyCodeClass> Map<OntResource, T> getOntologyClasses(Class<T> ontologyEntityClass) {
		return (Map<OntResource, T>) entityMap.get(ontologyEntityClass);
	}

	public JCodeModel asJCodeModel() {
		return codeModel;
	}

	public OntModel asOntModel() {
		return ontModel;
	}

	@Override
	public String getBaseNamespace() {
		return ontModel.getNsPrefixURI("");
	}

	@Override
	public Map<Class<? extends AbstractOntologyCodeClass>, Map<OntResource, AbstractOntologyCodeClass>> getEntityMap() {
		return entityMap;
	}

	@Override
	public Map<OntResource, Set<AbstractOntologyCodeMethod>> getMethodMap() {
		return methodMap;
	}

	public OntModel getInfOntModel() {
		return infOntModel;
	}

	public void setInfOntModel(OntModel infOntModel) {
		this.infOntModel = infOntModel;
	}

}
