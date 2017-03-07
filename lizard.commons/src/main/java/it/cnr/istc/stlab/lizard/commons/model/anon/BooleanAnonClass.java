package it.cnr.istc.stlab.lizard.commons.model.anon;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.LizardInterface;
import it.cnr.istc.stlab.lizard.commons.annotations.UnionOf;
import it.cnr.istc.stlab.lizard.commons.exception.NotAMemberException;
import it.cnr.istc.stlab.lizard.commons.inmemory.InMemoryLizardAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.OntologyCodeClass;

public abstract class BooleanAnonClass extends OntologyCodeClass {

	private static Logger logger = LoggerFactory.getLogger(BooleanAnonClass.class);

	protected JAnnotationArrayMember annotationArray;
	Set<AbstractOntologyCodeClass> members;

	protected String classNameSuffix;

	private BooleanAnonClass() {
		super();
		this.members = new HashSet<AbstractOntologyCodeClass>();
	}

	private BooleanAnonClass(AbstractOntologyCodeClass... members) {
		this();
		for (AbstractOntologyCodeClass member : members) {
			this.members.add(member);
		}
	}

	protected BooleanAnonClass(String classNameSuffix, Class<?> booleanClass, String id, OntResource ontClass, JCodeModel codeModel, AbstractOntologyCodeClass... members) {
		this(members);
		super.jCodeModel = codeModel;
		super.ontResource = ontClass;

		try {
			String packageName = codeModel.rootPackage().name();
			codeModel._package(packageName);

			super.entityName = Constants.ANON_PACKAGE + "." + classNameSuffix + id;

			super.jClass = codeModel._class(super.entityName);
			logger.debug("Creating anon class " + super.jClass.fullName());

			((JDefinedClass) super.jClass)._extends(InMemoryLizardAnonClass.class);
//			((JDefinedClass) super.jClass)._implements(codeModel.ref("org.w3._2002._07.owl.Thing"));

			JMethod unionConstructor = ((JDefinedClass) super.jClass).constructor(JMod.PUBLIC);
			JVar ind = unionConstructor.param(RDFNode.class, "individual");

			JVar idVar = ((JDefinedClass) super.jClass).field(JMod.PRIVATE, String.class, "id");

			JExpression expression = codeModel.ref(ModelFactory.class).staticInvoke("createOntologyModel").invoke("createOntResource").arg(ontClass.getId().getLabelString());
			unionConstructor.body().invoke("super").arg(ind).arg(expression);
			unionConstructor.body().assign(JExpr._this().ref(idVar), ind.invoke("asResource").invoke("getURI"));

			JMethod asUnionMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, super.jClass, "asIndividualOf");

			asUnionMethod = asUnionMethod._throws(NotAMemberException.class);

			JVar unionMethodParam = asUnionMethod.param(LizardInterface.class, "individual");
			JBlock unionMethodBody = asUnionMethod.body();

			/*
			 * Declare a variable that allows to access the UnionOf annotation of the class.
			 */
			JVar booleanAnnotationVar = unionMethodBody.decl(codeModel._ref(UnionOf.class), "unionOfAnnotation", JExpr.invoke("getClass").invoke("getAnnotation").arg(codeModel.ref(booleanClass).dotclass()));

			/*
			 * Fetch the list of valid members of the union from class annotation. The list is provided as an array of Class<? extends LizardInterface>.
			 */
			JType classType = codeModel.ref(Class.class).narrow(codeModel.ref(LizardInterface.class).wildcard()).array();
			JVar validMembersVar = unionMethodBody.decl(classType, "validMembers", booleanAnnotationVar.invoke("classes"));

			JVar validBooleanVar = unionMethodBody.decl(codeModel._ref(boolean.class), "valid", JExpr.FALSE);
			JForLoop forLoop = unionMethodBody._for();
			JVar iForLoopVar = forLoop.init(codeModel._ref(int.class), "i", JExpr.lit(0));
			forLoop.test(iForLoopVar.lt(validMembersVar.ref("length")).cand(validBooleanVar.not()));
			forLoop.update(iForLoopVar.incr());

			JConditional ifIsInstance = forLoop.body()._if(((JExpression) validMembersVar.component(iForLoopVar)).invoke("isInstance").arg(unionMethodParam));
			ifIsInstance._then().assign(validBooleanVar, JExpr.TRUE);

			JConditional ifValid = unionMethodBody._if(validBooleanVar);
			// ifValid._then()._return(JExpr._new(super.jClass).arg(unionMethodParam.invoke("getIndividual")).arg(unionMethodParam.invoke("getClassResource")).arg(unionMethodParam.invoke("getPropertyMap")));
			ifValid._then()._return(JExpr._new(super.jClass).arg(unionMethodParam.invoke("getIndividual")));
			ifValid._else()._throw(JExpr._new(codeModel.ref(NotAMemberException.class)).arg("Individual of cannot be part of this union class"));

			if (members != null) {
				for (AbstractOntologyCodeClass member : members) {
					addMember(member);

					/*
					 * Adding methods to transform the individual object to member objects
					 */

					JMethod asBeanOfClassMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, member.asJDefinedClass(), "as" + member.asJDefinedClass().name());
					JBlock asBeanOfClassBody = asBeanOfClassMethod.body();

					// TODO fix the quick and dirty solution
					JClass importedClass = codeModel.ref(member.getPackageName() + ".jena." + member.asJDefinedClass().name() + "Jena");
					asBeanOfClassBody._return(JExpr._new(importedClass).arg(JExpr._super().ref("individual")).invoke("asMicroBean"));
				}

				JClass thingInterfaceClass = codeModel.ref("org.w3._2002._07.owl.Thing");
				JMethod asBeanOfClassMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, thingInterfaceClass, "asThing");
				JBlock asBeanOfClassBody = asBeanOfClassMethod.body();

				// TODO fix the quick and dirty solution
				JClass importedClass = codeModel.ref("org.w3._2002._07.owl.jena.ThingJena");
				asBeanOfClassBody._return(JExpr._new(importedClass).arg(JExpr._super().ref("individual")).invoke("asMicroBean"));
			}

			/*
			 * Create fields: "id", "isCompleted"
			 */

			((JDefinedClass) super.jClass).field(JMod.PRIVATE, Boolean.class, "isCompleted");
			/*
			 * Create get and set method for "id" and "isCompleted"
			 */
			JMethod setIdMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, jCodeModel.VOID, "setId");
			setIdMethod.body().assign(JExpr._this().ref("id"), setIdMethod.param(String.class, "id"));

			JMethod getIdMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, String.class, "getId");
			getIdMethod.body()._return(JExpr._this().ref("id"));

			JMethod setIsCompletedMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, jCodeModel.VOID, "setIsCompleted");
			setIsCompletedMethod.body().assign(JExpr._this().ref("isCompleted"), setIsCompletedMethod.param(Boolean.class, "isCompleted"));

			JMethod getIsCompletedMethod = ((JDefinedClass) super.jClass).method(JMod.PUBLIC, Boolean.class, "getIsCompleted");
			getIsCompletedMethod.body()._return(JExpr._this().ref("isCompleted"));

		} catch (JClassAlreadyExistsException e) {
			e.printStackTrace();
		}

	}

	protected abstract void addMember(AbstractOntologyCodeClass ontologyCodeClass);

	@Override
	public int hashCode() {
		StringBuilder sb = new StringBuilder();
		for (AbstractOntologyCodeClass member : members) {
			sb.append(member.getOntResource().toString());
		}
		return sb.toString().hashCode();
	}

}
