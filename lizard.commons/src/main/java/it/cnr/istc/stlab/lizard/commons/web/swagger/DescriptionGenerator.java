package it.cnr.istc.stlab.lizard.commons.web.swagger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ontology.OntResource;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.converter.ModelConverters;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import it.cnr.istc.stlab.lizard.commons.Constants;
import it.cnr.istc.stlab.lizard.commons.PrefixRegistry;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeMethod;
import it.cnr.istc.stlab.lizard.commons.model.anon.BooleanAnonClass;
import it.cnr.istc.stlab.lizard.commons.model.datatype.DatatypeCodeInterface;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;
import jersey.repackaged.com.google.common.collect.Lists;

public class DescriptionGenerator {

	private static Logger logger = LoggerFactory.getLogger(DescriptionGenerator.class);

	private String apiDescription, apiVersion, apiTitle, contactName, contanctEmail, licenseName, licenseUrl;
	private String host, basePath;
	private Collection<AbstractOntologyCodeClass> classes;

	public void setApiDescription(String apiDescription) {
		this.apiDescription = apiDescription;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public void setApiTitle(String apiTitle) {
		this.apiTitle = apiTitle;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public void setContanctEmail(String contanctEmail) {
		this.contanctEmail = contanctEmail;
	}

	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}

	public void setLicenseUrl(String licenseUrl) {
		this.licenseUrl = licenseUrl;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void setClasses(Collection<AbstractOntologyCodeClass> collection) {
		this.classes = collection;
	}

	private List<Tag> getTags() {
		List<Tag> tags = new ArrayList<Tag>();

		classes.forEach(c -> {
			Tag t = new Tag();
			t.setName(getPath(c.getOntResource()).substring(1));
			tags.add(t);
		});

		return tags;
	}

	private static String getJavaClassName(OntResource ontResource) {
		return Constants.getJavaName(ontResource.getLocalName());
	}

	private String getPath(OntResource ontResource) {
		String localName = Constants.getJavaName(ontResource.getLocalName());
		StringBuilder sb = new StringBuilder();
		char[] chars = localName.toCharArray();
		boolean start = true;
		for (char c : chars) {
			if (Character.isUpperCase(c) && !start)
				sb.append("_");

			sb.append(Character.toLowerCase(c));

			if (start)
				start = !start;
		}

		String namespace = ontResource.getNameSpace();
		String prefix = ontResource.getOntModel().getNsURIPrefix(namespace);
		if (prefix == null)
			prefix = PrefixRegistry.getInstance().getNsPrefix(namespace);
		if (prefix == null)
			prefix = PrefixRegistry.getInstance().createNsPrefix(namespace);

		String path = null;
		if (prefix.isEmpty())
			path = "/" + sb.toString();
		else
			path = "/" + prefix + "_" + sb.toString();

		return path;

	}

	public Swagger generateSwaggerDescription() {

		Set<AbstractOntologyCodeClass> klasses = new HashSet<>(classes);

		classes.forEach(c -> {
			expand(c, klasses);
		});
		classes = klasses;

		Swagger swagger = new Swagger();
		Info info = new Info();
		info.setDescription(apiDescription);
		info.setVersion(apiVersion);
		info.setTitle(apiTitle);
		Contact contact = new Contact();
		contact.name(contactName);
		contact.email(contanctEmail);
		info.setContact(contact);
		License license = new License().name(licenseName);
		license.setUrl(licenseUrl);
		info.setLicense(license);
		swagger.setInfo(info);
		swagger.setHost(host);
		swagger.setBasePath(basePath);
		swagger.setTags(getTags());
		swagger.setPaths(getPaths());
		swagger.setDefinitions(getDefinitions());
		swagger.setSchemes(Lists.newArrayList(Scheme.HTTP));

		return swagger;
	}

	private Map<String, Model> getDefinitions() {
		Map<String, Model> definitions = new HashMap<String, Model>();
		classes.forEach(clazz -> {

			logger.debug(clazz.getOntResource().getLocalName());

			String javaName = getJavaClassName(clazz.getOntResource());
			Model classModel = new ModelImpl();
			definitions.put(javaName, classModel);

			Map<String, Property> classProperties = new HashMap<>();
			classProperties.put("id", new StringProperty());
			BooleanProperty isCompletedProperty = new BooleanProperty();
			isCompletedProperty._default(false);
			classProperties.put("isCompleted", isCompletedProperty);

			clazz.getMethods().forEach(m -> {
				logger.debug(clazz.getOntResource().getLocalName() + " " + m.getOntResource().getLocalName());
				if (m.getMethodType() == OntologyCodeMethodType.GET) {
					logger.debug("Adding method");
					ArrayProperty methodProperty = new ArrayProperty();
					methodProperty.setUniqueItems(true);
					classProperties.put(m.getEntityName(), methodProperty);
					if (m.getRange() instanceof BooleanAnonClass) {
						methodProperty.items(new RefProperty("#/definitions/Thing"));
					} else if (m.getRange() instanceof DatatypeCodeInterface) {
						if (m.getRange().getOntResource().getURI().equals("http://www.w3.org/2000/01/rdf-schema#Literal")) {
							methodProperty.items(ModelConverters.getInstance().readAsProperty(String.class));
						} else {
							Class<?> c = TypeMapper.getInstance().getTypeByName(m.getRange().getOntResource().getURI()).getJavaClass();
							definitions.putAll(ModelConverters.getInstance().readAll(c));
							methodProperty.items(ModelConverters.getInstance().readAsProperty(c));
						}
					} else {
						methodProperty.items(new RefProperty("#/definitions/" + getJavaClassName(m.getRange().getOntResource())));
					}
				}
			});

			HashSet<String> classNames = new HashSet<>();
			classProperties.forEach((k, v) -> {
				if (classNames.contains(k)) {
					System.out.println("WARNING: Two or more classes have the same local name! Swagger definitions may not be as expected!");
				}
				classNames.add(k);
				logger.debug(k);
			});

			classModel.setProperties(classProperties);

		});
		return definitions;
	}

	private static Property ontologyCodeClassToProperty(AbstractOntologyCodeClass klass) {
		if (klass instanceof BooleanAnonClass) {
			return new RefProperty("#/definitions/Thing");
		} else if (klass instanceof DatatypeCodeInterface) {
			if (klass.getOntResource().getURI().equals("http://www.w3.org/2000/01/rdf-schema#Literal")) {
				return ModelConverters.getInstance().readAsProperty(String.class);
			} else {
				Class<?> c = TypeMapper.getInstance().getTypeByName(klass.getOntResource().getURI()).getJavaClass();
				return ModelConverters.getInstance().readAsProperty(c);
			}
		} else {
			return new RefProperty("#/definitions/" + getJavaClassName(klass.getOntResource()));
		}
	}

	private void expand(AbstractOntologyCodeClass toScan, Collection<AbstractOntologyCodeClass> klasses) {
		Set<AbstractOntologyCodeClass> toAdd = new HashSet<>();
		klasses.forEach(klass -> {
			klass.getMethods().forEach(method -> {
				if (method.getMethodType() == OntologyCodeMethodType.GET) {
					if (!(method.getRange() instanceof BooleanAnonClass) && !(method.getRange() instanceof DatatypeCodeInterface)) {
						toAdd.add(method.getRange());
					}
				}
			});
		});

		toAdd.forEach(c -> {
			if (!klasses.contains(c)) {
				klasses.add(c);
				expand(c, klasses);
			}
		});

	}

	private Map<String, Path> getPaths() {
		Map<String, Path> result = new HashMap<String, Path>();
		classes.forEach(clazz -> {
			String path = getPath(clazz.getOntResource());
			String tag = getPath(clazz.getOntResource()).substring(1);
			result.put(path + "/create", getCreatePath(clazz, tag));
			result.put(path + "/getAll", getGetAllPath(clazz, tag));
			result.put(path + "/getById", getByIdPath(clazz, tag));
			clazz.getMethods().forEach(method -> {
				if (method.getMethodType().equals(OntologyCodeMethodType.SET)) {

					String methodName = "set" + method.getEntityName().substring(0, 1).toUpperCase() + method.getEntityName().substring(1);
					result.put(path + "/entity/" + methodName, getSetPath(clazz, method, methodName, tag));

				} else if (method.getMethodType().equals(OntologyCodeMethodType.GET)) {
					{
						String getByMethodName = "getBy" + method.getEntityName().substring(0, 1).toUpperCase() + method.getEntityName().substring(1);
						result.put(path + "/" + getByMethodName, getGetByPath(clazz, method, getByMethodName, tag));
					}
					{
						String getMethodName = "get" + method.getEntityName().substring(0, 1).toUpperCase() + method.getEntityName().substring(1);
						result.put(path + "/entity/" + getMethodName, getGetPath(clazz, method, getMethodName, tag));
					}
				} else if (method.getMethodType().equals(OntologyCodeMethodType.ADD_ALL)) {
					{
						String addMethodName = "add" + method.getEntityName().substring(0, 1).toUpperCase() + method.getEntityName().substring(1);
						result.put(path + "/entity/" + addMethodName, getAddPath(clazz, method, addMethodName, tag));
					}
				}

			});
		});
		return result;
	}

	private Path getGetPath(AbstractOntologyCodeClass clazz, AbstractOntologyCodeMethod method, String getMethodName, String tag) {
		String operationId = "get_" + getPath(clazz.getOntResource()).substring(1) + "_" + getPath(method.getOntResource()).substring(1);
		Path p = new Path();
		Operation op = new Operation();
		op.setTags(Lists.newArrayList(tag));
		op.setSummary("Get the " + method.getEntityName() + " of the individual identified by the iri passed as parameter.");
		op.setDescription("Get the " + method.getEntityName() + " of the individual identified by the iri passed as parameter.");
		op.setOperationId(operationId);
		op.setProduces(Lists.newArrayList("application/json"));
		List<Parameter> parameters = new ArrayList<Parameter>();
		{
			QueryParameter p1 = new QueryParameter();
			p1.setName("id");
			p1.setIn("query");
			p1.setDescription("id");
			p1.setType("string");
			p1.setRequired(true);
			parameters.add(p1);
		}

		op.setParameters(parameters);
		p.set("get", op);
		Map<String, Response> responses = new HashMap<>();

		{
			Response r1 = new Response();
			r1.setDescription("Not found");
			responses.put("404", r1);
		}
		{
			Response r2 = new Response();
			r2.setDescription("successful operation");
			responses.put("200", r2);
			ArrayProperty schema = new ArrayProperty();
			r2.setSchema(schema);
			schema.setItems(ontologyCodeClassToProperty(method.getRange()));
		}
		op.setResponses(responses);
		return p;
	}

	private Path getGetByPath(AbstractOntologyCodeClass clazz, AbstractOntologyCodeMethod method, String methodName, String tag) {
		String classJavaName = getJavaClassName(clazz.getOntResource());
		String operationId = "get_by_" + getPath(clazz.getOntResource()).substring(1) + "_" + getPath(method.getOntResource()).substring(1);
		Path p = new Path();
		Operation op = new Operation();
		op.setTags(Lists.newArrayList(tag));
		op.setSummary("Get individuals of type " + classJavaName + " that have the property " + method.getEntityName() + " set up.");
		op.setDescription("Get individuals of type " + classJavaName + " that have the property " + method.getEntityName() + " set up.");
		op.setOperationId(operationId);
		op.setProduces(Lists.newArrayList("application/json"));
		List<Parameter> parameters = new ArrayList<Parameter>();
		{
			QueryParameter p1 = new QueryParameter();
			p1.setName(getPath(method.getOntResource()).substring(1));
			p1.setIn("query");
			p1.setDescription(method.getEntityName());
			p1.setType("string");
			p1.setRequired(false);
			parameters.add(p1);
		}
		op.setParameters(parameters);
		p.set("get", op);
		Map<String, Response> responses = new HashMap<>();

		{
			Response r1 = new Response();
			r1.setDescription("Not found");
			responses.put("404", r1);
		}
		{
			Response r2 = new Response();
			r2.setDescription("successful operation");
			responses.put("200", r2);
			ArrayProperty schema = new ArrayProperty();
			r2.setSchema(schema);
			RefProperty rp = new RefProperty();
			rp.set$ref("#/definitions/" + classJavaName);
			schema.setItems(rp);
		}
		op.setResponses(responses);
		return p;
	}

	private Path getSetPath(AbstractOntologyCodeClass clazz, AbstractOntologyCodeMethod method, String methodName, String tag) {
		String classJavaName = getJavaClassName(clazz.getOntResource());
		String operationId = "set_" + getPath(clazz.getOntResource()).substring(1) + "_" + getPath(method.getOntResource()).substring(1);
		Path p = new Path();
		Operation op = new Operation();
		op.setTags(Lists.newArrayList(tag));
		op.setSummary("Set the " + method.getEntityName() + " of the object identified the the iri passed as parameter.");
		op.setDescription("Set the " + methodName + " of the object identified the the iri passed as parameter.");
		op.setOperationId(operationId);
		op.setProduces(Lists.newArrayList("application/json"));
		List<Parameter> parameters = new ArrayList<Parameter>();
		{
			QueryParameter p1 = new QueryParameter();
			p1.setName("id");
			p1.setIn("query");
			p1.setDescription("id");
			p1.setType("string");
			p1.setRequired(true);
			parameters.add(p1);
		}
		{
			QueryParameter p2 = new QueryParameter();
			p2.setName("value");
			p2.setIn("query");
			p2.setDescription("value to be set");
			p2.setType("string");
			p2.setRequired(true);
			parameters.add(p2);
		}
		op.setParameters(parameters);
		p.set("post", op);
		Map<String, Response> responses = new HashMap<>();

		{
			Response r1 = new Response();
			r1.setDescription("Not found");
			responses.put("404", r1);
		}
		{
			Response r2 = new Response();
			r2.setDescription("successful operation");
			responses.put("200", r2);
			ArrayProperty schema = new ArrayProperty();
			r2.setSchema(schema);
			RefProperty rp = new RefProperty();
			rp.set$ref("#/definitions/" + classJavaName);
			schema.setItems(rp);
		}
		op.setResponses(responses);
		return p;
	}
	
	private Path getAddPath(AbstractOntologyCodeClass clazz, AbstractOntologyCodeMethod method, String methodName, String tag) {
		String classJavaName = getJavaClassName(clazz.getOntResource());
		String operationId = "add_" + getPath(clazz.getOntResource()).substring(1) + "_" + getPath(method.getOntResource()).substring(1);
		Path p = new Path();
		Operation op = new Operation();
		op.setTags(Lists.newArrayList(tag));
		op.setSummary("Add the " + method.getEntityName() + " of the object identified the the iri passed as parameter.");
		op.setDescription("Add the " + methodName + " of the object identified the the iri passed as parameter.");
		op.setOperationId(operationId);
		op.setProduces(Lists.newArrayList("application/json"));
		List<Parameter> parameters = new ArrayList<Parameter>();
		{
			QueryParameter p1 = new QueryParameter();
			p1.setName("id");
			p1.setIn("query");
			p1.setDescription("id");
			p1.setType("string");
			p1.setRequired(true);
			parameters.add(p1);
		}
		{
			QueryParameter p2 = new QueryParameter();
			p2.setName("value");
			p2.setIn("query");
			p2.setDescription("value to be set");
			p2.setType("string");
			p2.setRequired(true);
			parameters.add(p2);
		}
		op.setParameters(parameters);
		p.set("post", op);
		Map<String, Response> responses = new HashMap<>();

		{
			Response r1 = new Response();
			r1.setDescription("Not found");
			responses.put("404", r1);
		}
		{
			Response r2 = new Response();
			r2.setDescription("successful operation");
			responses.put("200", r2);
			ArrayProperty schema = new ArrayProperty();
			r2.setSchema(schema);
			RefProperty rp = new RefProperty();
			rp.set$ref("#/definitions/" + classJavaName);
			schema.setItems(rp);
		}
		op.setResponses(responses);
		return p;
	}

	private Path getByIdPath(AbstractOntologyCodeClass clazz, String tag) {
		String localName = getLocalName(clazz);
		Path p = new Path();
		Operation op = new Operation();
		op.setTags(Lists.newArrayList(tag));
		op.setSummary("Get a " + localName + " by id.");
		op.setDescription("Get a " + localName + " by id.");
		// e.g. get_question_by_id
		op.setOperationId("get_" + localName + "_by_id");
		op.setProduces(Lists.newArrayList("application/json"));
		List<Parameter> parameters = new ArrayList<Parameter>();
		QueryParameter p1 = new QueryParameter();
		p1.setName("id");
		p1.setIn("query");
		p1.setDescription("id");
		p1.setType("string");
		p1.setRequired(true);
		parameters.add(p1);
		op.setParameters(parameters);
		p.set("get", op);
		Map<String, Response> responses = new HashMap<>();
		Response r1 = new Response();
		r1.setDescription("successful operation");
		responses.put("200", r1);
		ArrayProperty schema = new ArrayProperty();
		r1.setSchema(schema);
		RefProperty rp = new RefProperty();
		rp.set$ref("#/definitions/" + getJavaClassName(clazz.getOntResource()));
		schema.setItems(rp);
		op.setResponses(responses);
		return p;
	}

	private Path getCreatePath(AbstractOntologyCodeClass occ, String tag) {
		String localName = getLocalName(occ);
		Path p = new Path();
		Operation op = new Operation();
		op.setTags(Lists.newArrayList(tag));
		op.setSummary("Create a new " + localName);
		op.setDescription("Create a new " + localName);
		op.setOperationId("create_" + localName);
		op.setProduces(Lists.newArrayList("application/json"));
		List<Parameter> parameters = new ArrayList<Parameter>();
		QueryParameter p1 = new QueryParameter();
		p1.setName("id");
		p1.setIn("query");
		p1.setDescription("id");
		p1.setType("string");
		p1.setRequired(true);
		parameters.add(p1);
		op.setParameters(parameters);
		p.set("post", op);
		Map<String, Response> responses = new HashMap<>();
		Response r1 = new Response();
		r1.setDescription("successful operation");
		responses.put("default", r1);
		op.setResponses(responses);
		return p;
	}

	private Path getGetAllPath(AbstractOntologyCodeClass occ, String tag) {
		String localName = getLocalName(occ);
		Path p = new Path();
		Operation op = new Operation();
		op.setTags(Lists.newArrayList(tag));
		op.setSummary("Retrieve all individuals of type " + localName);
		op.setDescription("Retrieve all individuals of type " + localName);
		op.setOperationId("getAll" + localName);
		op.setProduces(Lists.newArrayList("application/json"));
		List<Parameter> parameters = new ArrayList<Parameter>();
		op.setParameters(parameters);
		p.set("get", op);
		Map<String, Response> responses = new HashMap<>();
		Response r1 = new Response();
		r1.setDescription("successful operation");
		responses.put("200", r1);
		ArrayProperty schema = new ArrayProperty();
		r1.setSchema(schema);
		RefProperty rp = new RefProperty();
		rp.set$ref("#/definitions/" + getJavaClassName(occ.getOntResource()));
		schema.setItems(rp);
		op.setResponses(responses);
		return p;
	}

	private String getLocalName(AbstractOntologyCodeClass clazz) {
		String localName = Constants.getJavaName(clazz.getOntResource().getLocalName());
		StringBuilder sb = new StringBuilder();
		char[] chars = localName.toCharArray();
		boolean start = true;
		for (char c : chars) {
			if (Character.isUpperCase(c) && !start)
				sb.append("_");
			sb.append(Character.toLowerCase(c));
			if (start)
				start = !start;
		}
		return sb.toString();
	}

	public String generateSwaggerJSONStringDescription() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {
			String jsonInString = mapper.writeValueAsString(generateSwaggerDescription());
			JSONObject obj = new JSONObject(jsonInString);
			return obj.toString(4);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	// public static void main(String[] args) {
	// DescriptionGenerator dg = new DescriptionGenerator();
	// dg.setApiDescription("Rest services defined in package org.w3id.ppdb.ontology.web for accessing the corresponding ontology.");
	// dg.setApiVersion("0.99");
	// dg.setApiTitle("org.w3id.ppdb.ontology.web");
	// dg.setContactName("STLab");
	// dg.setContanctEmail("stlab@cnr.it");
	// dg.setLicenseName("Apache 2.0");
	// dg.setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html");
	// dg.setHost("localhost:8585");
	// dg.setBasePath("/org_w3id_ppdb_ontology");
	// System.out.println(dg.generateSwaggerJSONStringDescription());
	// }

}
