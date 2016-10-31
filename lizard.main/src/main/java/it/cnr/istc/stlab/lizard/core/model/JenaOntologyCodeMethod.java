package it.cnr.istc.stlab.lizard.core.model;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.PrefixRegistry;
import it.cnr.istc.stlab.lizard.commons.annotations.ObjectPropertyAnnotation;
import it.cnr.istc.stlab.lizard.commons.exception.NotAvailableOntologyCodeEntityException;
import it.cnr.istc.stlab.lizard.commons.jena.RuntimeJenaLizardContext;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClassImpl;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeModel;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.datatype.DatatypeCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JWhileLoop;

public class JenaOntologyCodeMethod extends OntologyCodeMethod {

	JenaOntologyCodeMethod(OntologyCodeMethodType methodType, OntResource methodResource, AbstractOntologyCodeClass owner, Collection<AbstractOntologyCodeClass> domain, AbstractOntologyCodeClass range, OntologyCodeModel ontologyModel, JCodeModel codeModel) {
		super(methodType, methodResource, owner, domain, range, ontologyModel, codeModel);
		
//		System.out.println("JenaOntologyCodeMethod.JenaOntologyCodeMethod() "+(domain==null));
		
		if (methodResource.isURIResource()) {

			String namespace = methodResource.getNameSpace();

			String prefix = ontologyModel.asOntModel().getNsURIPrefix(namespace);
			// look-up on prefix.cc
			if (prefix == null)
				prefix = PrefixRegistry.getInstance().getNsPrefix(namespace);
			// if the prefix is again null, then we create it
			if (prefix == null)
				prefix = PrefixRegistry.getInstance().createNsPrefix(namespace);

			String localName = Constants.getJavaName(methodResource.getLocalName());

			if (prefix.isEmpty())
				entityName = localName;
			else
				entityName = prefix + "_" + localName;

			JDefinedClass domainJClass = (JDefinedClass) owner.asJDefinedClass();

			if (methodType == OntologyCodeMethodType.Get) {

				JType setClass = codeModel.ref(Set.class).narrow(range.asJDefinedClass());
				JDefinedClass jOwner = ((JDefinedClass) owner.asJDefinedClass());
				String methodName = "get" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
				jMethod = jOwner.method(1, setClass, methodName);
				/*
				 * Code the method for converting a JenaOntologyClass to its
				 * corresponding Java bean.
				 */
				JMethod asBeanMethod = jOwner.getMethod("asBean", new JType[] {});
				JMethod asMicroBeanMethod = jOwner.getMethod("asMicroBean", new JType[] {});

				JVar beanVar = null;
				JVar beanVarMB = null;

				String name = owner.asJDefinedClass().name();
				name = name.substring(0, 1).toLowerCase() + name.substring(1);

				if (asBeanMethod == null) {

					JClass beanClass = ontologyModel.getOntologyClass(owner.getOntResource(), BeanOntologyCodeClass.class).asJDefinedClass();

					asBeanMethod = jOwner.method(JMod.PUBLIC, beanClass, "asBean");
					asMicroBeanMethod = jOwner.method(JMod.PUBLIC, beanClass, "asMicroBean");

					JBlock asBeanMethodBody = asBeanMethod.body();
					JBlock asMicroBeanMethodBody = asMicroBeanMethod.body();

					JVar jenaModelVar = asBeanMethodBody.decl(jCodeModel._ref(Model.class), "model", jCodeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
					JVar jenaModelVarMB = asMicroBeanMethodBody.decl(jCodeModel._ref(Model.class), "model", jCodeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));

					JVar queryVar = asBeanMethodBody.decl(jCodeModel._ref(Query.class), "query", jCodeModel.ref(QueryFactory.class).staticInvoke("create").arg(JExpr.lit("DESCRIBE <").plus(JExpr._super().ref("individual").invoke("asResource").invoke("getURI").plus(JExpr.lit(">")))));
					JVar queryVarMB = asMicroBeanMethodBody.decl(jCodeModel._ref(Query.class), "query", jCodeModel.ref(QueryFactory.class).staticInvoke("create").arg(JExpr.lit("DESCRIBE <").plus(JExpr._super().ref("individual").invoke("asResource").invoke("getURI").plus(JExpr.lit(">")))));

					JVar qexecVar = asBeanMethodBody.decl(jCodeModel._ref(QueryExecution.class), "qexec", jCodeModel.ref(VirtuosoQueryExecutionFactory.class).staticInvoke("create").arg(queryVar).arg(jenaModelVar));
					JVar qexecVarMB = asMicroBeanMethodBody.decl(jCodeModel._ref(QueryExecution.class), "qexec", jCodeModel.ref(VirtuosoQueryExecutionFactory.class).staticInvoke("create").arg(queryVarMB).arg(jenaModelVarMB));

					asBeanMethodBody.decl(jCodeModel._ref(Model.class), "m", qexecVar.invoke("execDescribe"));
					asMicroBeanMethodBody.decl(jCodeModel._ref(Model.class), "m", qexecVarMB.invoke("execDescribe"));

					beanVar = asBeanMethodBody.decl(beanClass, name, JExpr._new(beanClass));
					asBeanMethodBody.directStatement(name + ".setId(super.individual.asResource().getURI());");
					asBeanMethodBody.directStatement(name + ".setIsCompleted(true);");

					beanVarMB = asMicroBeanMethodBody.decl(beanClass, name, JExpr._new(beanClass));
					asMicroBeanMethodBody.directStatement(name + ".setId(super.individual.asResource().getURI());");
					asMicroBeanMethodBody.directStatement(name + ".setIsCompleted(true);");

					asBeanMethodBody._return(beanVar);
					asMicroBeanMethodBody._return(beanVarMB);

					asBeanMethodBody.pos(asBeanMethodBody.pos() - 1);
					asMicroBeanMethodBody.pos(asMicroBeanMethodBody.pos() - 1);

				}

				JBlock asBeanMethodBody = asBeanMethod.body();
				JBlock asMicroBeanMethodBody = asMicroBeanMethod.body();

				String beanSetMethodName = "set" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
				String beanGetMethodName = "get" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);

				if (methodResource.isDatatypeProperty()) {
					asBeanMethodBody.directStatement(name + "." + beanSetMethodName + "(this." + beanGetMethodName + "(m));");
					asMicroBeanMethodBody.directStatement(name + "." + beanSetMethodName + "(this." + beanGetMethodName + "(m));");
				} else {
					asBeanMethodBody.directStatement(name + "." + beanSetMethodName + "(this." + beanGetMethodName + "(m,false));");
					asMicroBeanMethodBody.directStatement(name + "." + beanSetMethodName + "(this." + beanGetMethodName + "(m,true));");
				}

				addSideGetMethod();
				
			} else {

				String methodName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
				jMethod = domainJClass.method(1, void.class, "set" + methodName);
				if (domain != null) {
					for (AbstractOntologyCodeClass domainClass : domain) {
						String name = domainClass.getEntityName();
						name = name.substring(name.lastIndexOf(".") + 1);
						name = name.substring(0, 1).toLowerCase() + name.substring(1);
						JType setClass = codeModel.ref(Set.class).narrow(domainClass.asJDefinedClass());
						jMethod.param(setClass, name);
					}
				} else {
					JType setClass = codeModel.ref(Set.class).narrow(range.asJDefinedClass());
					jMethod.param(setClass, entityName);
				}
			}

			char[] fieldNameChars = entityName.toCharArray();
			StringBuilder sb = new StringBuilder();

			Character previous = null;
			for (char fieldNameChar : fieldNameChars) {
				if (previous != null) {
					if (Character.isLowerCase(previous) && Character.isUpperCase(fieldNameChar))
						sb.append("_");
				}
				sb.append(Character.toUpperCase(fieldNameChar));
				previous = fieldNameChar;
			}

			if (owner instanceof OntologyCodeInterface) {
				JFieldVar staticField = domainJClass.fields().get(sb.toString());
				if (staticField == null)
					staticField = domainJClass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, String.class, sb.toString(), JExpr.lit(ontResource.getURI()));

				JAnnotationUse jAnnotationUse = jMethod.annotate(ObjectPropertyAnnotation.class);
				jAnnotationUse.param("uri", staticField);
				jAnnotationUse.param("method", methodType);

			} else
				jMethod.annotate(Override.class);

		}

		addMethodBody();
	}

	@Override
	public int hashCode() {
		return methodType.hashCode() + super.hashCode();
	}

	private void addSideGetMethod() {

		// TODO
		
		JType setClass = super.jCodeModel.ref(Set.class).narrow(range.asJDefinedClass());
		JDefinedClass jOwner = ((JDefinedClass) owner.asJDefinedClass());
		String methodName = "get" + entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
		JMethod sideGetMethod = jOwner.method(JMod.PRIVATE, setClass, methodName);
		JVar jenaModelVar = sideGetMethod.param(Model.class, "model");
		JVar isForMicroBeanVar = null;
		if (!ontResource.isDatatypeProperty()) {
			isForMicroBeanVar = sideGetMethod.param(Boolean.class, "isForMicroBean");
		}

		/*
		 * Add the body to the method.
		 */
		if (owner instanceof OntologyCodeClass) {

			if (methodType == OntologyCodeMethodType.Get) {
				if (owner instanceof OntologyCodeClass) {

					JBlock methodBody = sideGetMethod.body();

					JClass hashSetClass = jCodeModel.ref(HashSet.class).narrow(range.asJDefinedClass());

					JVar returnVar = methodBody.decl(setClass, "retValue", JExpr._new(hashSetClass));

					JVar ontPropertyVar = methodBody.decl(jCodeModel._ref(Property.class), "predicate", jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(ontResource.toString()));

					JVar stmtIteratorVar = methodBody.decl(jCodeModel._ref(StmtIterator.class), "stmtIt", jenaModelVar.invoke("listStatements").arg(JExpr.cast(jCodeModel._ref(Resource.class), JExpr._super().ref("individual"))).arg(ontPropertyVar).arg(JExpr.cast(jCodeModel._ref(RDFNode.class), JExpr._null())));

					JWhileLoop stmtItHasNextWhile = methodBody._while(stmtIteratorVar.invoke("hasNext"));
					JBlock stmtItHasNextWhileBlock = stmtItHasNextWhile.body();
					JVar stmtVar = stmtItHasNextWhileBlock.decl(jCodeModel._ref(Statement.class), "stmt", stmtIteratorVar.invoke("next"));
					JVar stmtObjectVar = stmtItHasNextWhileBlock.decl(jCodeModel._ref(RDFNode.class), "object", stmtVar.invoke("getObject"));

					JClass rangeClass = range.asJDefinedClass();

					if (range.getOntResource() != null) {

						OntResource rangeRes = range.getOntResource();
						AbstractOntologyCodeClass rangeConcreteClass = null;
						AbstractOntologyCodeClass rangeConcreteClassBean = null;

						if (rangeRes.isURIResource()) {
							rangeConcreteClass = ontologyModel.getOntologyClass(range.getOntResource(), JenaOntologyCodeClass.class);
							rangeConcreteClassBean = ontologyModel.getOntologyClass(range.getOntResource(), BeanOntologyCodeClass.class);
						} else {
							rangeConcreteClass = ontologyModel.getOntologyClass(range.getOntResource(), BooleanAnonClass.class);
						}

						if (rangeConcreteClass == null) {
							try {
								OntologyCodeInterface rangeInterface = ontologyModel.getOntologyClass(range.getOntResource(), BeanOntologyCodeInterface.class);
								if (rangeInterface != null) {
									rangeConcreteClass = ontologyModel.createOntologyClass(range.getOntResource(), JenaOntologyCodeClass.class);
									rangeConcreteClassBean = ontologyModel.createOntologyClass(range.getOntResource(), BeanOntologyCodeClass.class);
									ontologyModel.createClassImplements((AbstractOntologyCodeClassImpl) rangeConcreteClass, rangeInterface);
								} else {
									// System.out.println(owner.getOntResource().getURI()+" "+super.entityName+" "+range.getOntResource());
									if (ontResource.isDatatypeProperty()) {
										System.out.println("datatype " + ontResource);
										rangeConcreteClass = ontologyModel.createOntologyClass(rangeRes, BeanOntologyCodeInterface.class);
									} else {
										rangeConcreteClass = ontologyModel.getOntologyClass(range.getOntResource(), BooleanAnonClass.class);
									}
									if (rangeConcreteClass == null)
										rangeConcreteClass = ontologyModel.createAnonClass(range.getOntResource().asClass());
								}

							} catch (NotAvailableOntologyCodeEntityException e) {
								e.printStackTrace();
							}
						}

						JVar retObj = null;
						if (range instanceof DatatypeCodeInterface) {
							JVar objectLiteralVar = stmtItHasNextWhileBlock.decl(jCodeModel.ref(Literal.class), "objectLiteral", JExpr.cast(jCodeModel.ref(Literal.class), stmtObjectVar));
							retObj = stmtItHasNextWhileBlock.decl(rangeClass, "obj", JExpr.cast(rangeClass, objectLiteralVar.invoke("getValue")));
						} else {
							if (rangeConcreteClassBean != null && !ontResource.isDatatypeProperty()) {
								retObj = stmtItHasNextWhileBlock.decl(rangeClass, "obj", JExpr._null());
								JConditional condition = stmtItHasNextWhileBlock._if(isForMicroBeanVar);
								JBlock thenBlock = condition._then();
								thenBlock.assign(retObj, JExpr._new(rangeConcreteClassBean.asJDefinedClass()).arg(stmtObjectVar));
								thenBlock.directStatement("obj.setId(object.asResource().getURI());");
								thenBlock.directStatement("obj.setIsCompleted(false);");
								JBlock elseBlock = condition._else();
								elseBlock.assign(retObj, JExpr._new(rangeConcreteClass.asJDefinedClass()).arg(stmtObjectVar));
							} else {
								// TODO manage problem with AnonClasses
								retObj = stmtItHasNextWhileBlock.decl(rangeClass, "obj", JExpr._new(rangeConcreteClass.asJDefinedClass()).arg(stmtObjectVar));
								stmtItHasNextWhileBlock.directStatement("obj.setIsCompleted(false);");
							}

						}
						stmtItHasNextWhileBlock.add(returnVar.invoke("add").arg(retObj));

						methodBody._return(returnVar);
					}
				}
			} else {
				if (owner instanceof OntologyCodeClass) {

					JBlock methodBody = jMethod.body();

					/*
					 * Add the code to set a variable for the URI representing
					 * the property and the type of the method.
					 */
					JVar ontPropertyVar = methodBody.decl(jCodeModel._ref(Property.class), "predicate", jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(ontResource.toString()));

					int paramCounter = 0;
					for (AbstractOntologyCodeClass domain : this.domain) {
						JForEach forEach = methodBody.forEach(domain.asJDefinedClass(), "object", jMethod.params().get(paramCounter));
						JBlock forEachBlock = forEach.body();

						JInvocation invocation = jenaModelVar.invoke("add").arg(JExpr.cast(jCodeModel._ref(Resource.class), JExpr._super().ref("individual"))).arg(ontPropertyVar);

						if (domain instanceof DatatypeCodeInterface) {
							JVar literalVar = forEachBlock.decl(jCodeModel._ref(Literal.class), "_literal_", jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createTypedLiteral").arg(forEach.var()));

							invocation.arg(literalVar);
						} else {
							invocation.arg(forEach.var().invoke("getIndividual"));
						}

						forEachBlock.add(invocation);

						paramCounter += 1;
					}

				}
			}
		}

	}

	private void addMethodBody() {
		/*
		 * Add the body to the method.
		 */
		if (owner instanceof OntologyCodeClass) {

			if (methodType == OntologyCodeMethodType.Get) {

				if (owner instanceof OntologyCodeClass) {

					JBlock methodBody = jMethod.body();

					JType setClass = jCodeModel.ref(Set.class).narrow(range.asJDefinedClass());
					JClass hashSetClass = jCodeModel.ref(HashSet.class).narrow(range.asJDefinedClass());

					JVar returnVar = methodBody.decl(setClass, "retValue", JExpr._new(hashSetClass));

					JVar ontPropertyVar = methodBody.decl(jCodeModel._ref(Property.class), "predicate", jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(ontResource.toString()));

					JVar jenaModelVar = methodBody.decl(jCodeModel._ref(Model.class), "model", jCodeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));
					JVar stmtIteratorVar = methodBody.decl(jCodeModel._ref(StmtIterator.class), "stmtIt", jenaModelVar.invoke("listStatements").arg(JExpr.cast(jCodeModel._ref(Resource.class), JExpr._super().ref("individual"))).arg(ontPropertyVar).arg(JExpr.cast(jCodeModel._ref(RDFNode.class), JExpr._null())));

					JWhileLoop stmtItHasNextWhile = methodBody._while(stmtIteratorVar.invoke("hasNext"));
					JBlock stmtItHasNextWhileBlock = stmtItHasNextWhile.body();
					JVar stmtVar = stmtItHasNextWhileBlock.decl(jCodeModel._ref(Statement.class), "stmt", stmtIteratorVar.invoke("next"));
					JVar stmtObjectVar = stmtItHasNextWhileBlock.decl(jCodeModel._ref(RDFNode.class), "object", stmtVar.invoke("getObject"));

					JClass rangeClass = range.asJDefinedClass();

					if (range.getOntResource() != null) {

						OntResource rangeRes = range.getOntResource();
						AbstractOntologyCodeClass rangeConcreteClass = null;

						if (rangeRes.isURIResource()) {
							rangeConcreteClass = ontologyModel.getOntologyClass(range.getOntResource(), JenaOntologyCodeClass.class);
						} else {
							rangeConcreteClass = ontologyModel.getOntologyClass(range.getOntResource(), BooleanAnonClass.class);
						}

						if (rangeConcreteClass == null) {

							try {

								OntologyCodeInterface rangeInterface = ontologyModel.getOntologyClass(range.getOntResource(), BeanOntologyCodeInterface.class);

								if (rangeInterface != null) {
									rangeConcreteClass = ontologyModel.createOntologyClass(range.getOntResource(), JenaOntologyCodeClass.class);
									ontologyModel.createClassImplements((AbstractOntologyCodeClassImpl) rangeConcreteClass, rangeInterface);
								} else {
									
									if(ontResource.isDatatypeProperty()){
										rangeConcreteClass=ontologyModel.createOntologyClass(ontResource, BeanOntologyCodeInterface.class);
									}else{
										rangeConcreteClass = ontologyModel.getOntologyClass(range.getOntResource(), BooleanAnonClass.class);
									}

									if (rangeConcreteClass == null) {
										rangeConcreteClass = ontologyModel.createAnonClass(range.getOntResource().asClass());
									}
								}

							} catch (NotAvailableOntologyCodeEntityException e) {
								e.printStackTrace();
							}

							// ontologyModel.getClassMap().put(range.getOntResource(),
							// (OntologyCodeClass)rangeConcreteClass);
						}

						JVar retObj = null;
						if (range instanceof DatatypeCodeInterface) {
							JVar objectLiteralVar = stmtItHasNextWhileBlock.decl(jCodeModel.ref(Literal.class), "objectLiteral", JExpr.cast(jCodeModel.ref(Literal.class), stmtObjectVar));
							retObj = stmtItHasNextWhileBlock.decl(rangeClass, "obj", JExpr.cast(rangeClass, objectLiteralVar.invoke("getValue")));
						} else {
							retObj = stmtItHasNextWhileBlock.decl(rangeClass, "obj", JExpr._new(rangeConcreteClass.asJDefinedClass()).arg(stmtObjectVar));
						}
						stmtItHasNextWhileBlock.add(returnVar.invoke("add").arg(retObj));

						methodBody._return(returnVar);
					}
				}
			} else {
				if (owner instanceof OntologyCodeClass) {

					JBlock methodBody = jMethod.body();

					/*
					 * Add the code to set a variable for the URI representing
					 * the property and the type of the method.
					 */
					JVar ontPropertyVar = methodBody.decl(jCodeModel._ref(Property.class), "predicate", jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createProperty").arg(ontResource.toString()));
					JVar jenaModelVar = methodBody.decl(jCodeModel._ref(Model.class), "model", jCodeModel.ref(RuntimeJenaLizardContext.class).staticInvoke("getContext").invoke("getModel"));

					
					int paramCounter = 0;
					for (AbstractOntologyCodeClass domain : this.domain) {
						
						JForEach forEach = methodBody.forEach(domain.asJDefinedClass(), "object", jMethod.params().get(paramCounter));
						JBlock forEachBlock = forEach.body();

						JInvocation invocation = jenaModelVar.invoke("add").arg(JExpr.cast(jCodeModel._ref(Resource.class), JExpr._super().ref("individual"))).arg(ontPropertyVar);

						if (domain instanceof DatatypeCodeInterface) {
							JVar literalVar = forEachBlock.decl(jCodeModel._ref(Literal.class), "_literal_", jCodeModel.ref(ModelFactory.class).staticInvoke("createDefaultModel").invoke("createTypedLiteral").arg(forEach.var()));

							invocation.arg(literalVar);
						} else {
							invocation.arg(forEach.var().invoke("getIndividual"));
						}

						forEachBlock.add(invocation);

						paramCounter += 1;
					}
				}
			}
		}
	}

}
