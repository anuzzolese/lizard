package it.cnr.istc.stlab.lizard.commons.web;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cnr.istc.stlab.lizard.commons.LizardInterface;
import it.cnr.istc.stlab.lizard.commons.PackageResolver;
import it.cnr.istc.stlab.lizard.commons.inmemory.RestInterface;
import it.cnr.istc.stlab.lizard.commons.jena.RuntimeJenaLizardContext;

@Path("/{ontology}")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = Object.class, property = { "javax.ws.rs=true" })
public class RestImpl implements RestInterface {

	private static Logger logger = LoggerFactory.getLogger(RestImpl.class);

	private static String extractClassName(String className) {
		StringBuilder sb = new StringBuilder();
		String[] a = className.split("_");
		for (int i = 1; i < a.length; i++) {
			sb.append(a[i].substring(0, 1).toUpperCase());
			sb.append(a[i].substring(1));
		}
		return sb.toString();
	}

	private static String getAbsoluteJenaClassName(String ontology, String className) {
		className = extractClassName(className);
		String absoluteJenaClassName = PackageResolver.urlPathToPackageName(ontology) + ".jena." + className + "Jena";
		logger.debug("Requesting jena classname ontology: {}, class: {} result {}", ontology, className, absoluteJenaClassName);
		return absoluteJenaClassName;
	}

	private static String getAbsoluteInterfaceName(String ontology, String className) {
		className = extractClassName(className);
		String absoluteInterfaceName = PackageResolver.urlPathToPackageName(ontology) + "." + className;
		logger.debug("Requesting interface name ontology: {}, class: {} result {}", ontology, className, absoluteInterfaceName);
		return absoluteInterfaceName;
	}

	@POST
	@Path("/{class_name}")
	public Response create(@PathParam("ontology") String ontology, @PathParam("class_name") String className, @QueryParam("iri") String id) {
		logger.trace("Create {} {} {}", ontology, className, id);

		String absoluteJenaClassName = getAbsoluteJenaClassName(ontology, className);
		try {
			// Create a new object
			Class.forName(absoluteJenaClassName).getConstructor(String.class).newInstance(id);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | java.lang.InstantiationException e) {
			e.printStackTrace();
		}
		Response.ResponseBuilder _responseBuilder = Response.ok();
		return _responseBuilder.build();
	}

	@GET
	@Path("/swagger.json")
	public Response getSwaggerDescription(@PathParam("ontology") String ontology) {
		logger.trace("Get swagger descrition {}", ontology);
		Response.ResponseBuilder responseBuilder = null;

		File f = new File(RuntimeJenaLizardContext.getContext().getConf().getSwaggerApiDescriptionFolder() + "/" + ontology + "/swagger.json");

		if (!f.exists()) {
			responseBuilder = Response.status(Response.Status.NOT_FOUND);
		} else {
			responseBuilder = Response.ok(f);
		}

		return responseBuilder.build();
	}

	// @GET
	// @Path("/{class_name}")
	// public Response getAll(@PathParam("ontology") String ontology, @PathParam("class_name") String className) {
	// logger.trace("Get all {} {}", ontology, className);
	// Response.ResponseBuilder responseBuilder = null;
	// String interfaceClassName = getAbsoluteInterfaceName(ontology, className);
	// String jenaClassName = getAbsoluteJenaClassName(ontology, className);
	// try {
	// Class<?> interfaceClass = Class.forName(interfaceClassName);
	// Class<?> jenaClass = Class.forName(jenaClassName);
	// Method getAllMethod = interfaceClass.getMethod("getAll", (Class<?>[]) null);
	// Method asMicroBeanMethod = jenaClass.getMethod("asMicroBean", (Class<?>[]) null);
	// @SuppressWarnings("unchecked")
	// Set<Object> _kbSet = (Set<Object>) getAllMethod.invoke(null, (Object[]) null);
	// Set<Object> _retSet = new HashSet<Object>();
	// logger.trace("Class name {} {}", jenaClass.getCanonicalName(), interfaceClass.getCanonicalName());
	// if (_kbSet != null) {
	// for (Object _obj : _kbSet) {
	// logger.trace("Object retrieved");
	// _retSet.add(asMicroBeanMethod.invoke(jenaClass.cast(_obj), (Object[]) null));
	// }
	// responseBuilder = Response.ok(_retSet.toArray(new Object[_retSet.size()]));
	// } else {
	// logger.trace("null");
	// responseBuilder = Response.status(Response.Status.NOT_FOUND);
	// }
	// } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
	// e.printStackTrace();
	// }
	// return responseBuilder.build();
	// }

	@GET
	@Path("/{class_name}")
	public Response getById(@PathParam("ontology") String ontology, @PathParam("class_name") String className, @QueryParam("iri") String id) {
		logger.trace("Get by id {} {} {}", ontology, className, id);
		Response.ResponseBuilder responseBuilder = null;
		String interfaceClassName = getAbsoluteInterfaceName(ontology, className);
		String jenaClassName = getAbsoluteJenaClassName(ontology, className);
		try {
			Class<?> interfaceClass = Class.forName(interfaceClassName);
			Class<?> jenaClass = Class.forName(jenaClassName);
			Method asMicroBeanMethod = jenaClass.getMethod("asMicroBean", (Class<?>[]) null);
			if (id != null) {
				Method getMethod = interfaceClass.getMethod("get", String.class);
				Object entity = getMethod.invoke(null, id);
				if (entity != null) {
					logger.trace("Found");
					Set<Object> retSet = new HashSet<Object>();
					retSet.add(asMicroBeanMethod.invoke(jenaClass.cast(entity), (Object[]) null));
					responseBuilder = Response.ok(retSet.toArray(new Object[retSet.size()]));
				} else {
					logger.trace("Not found");
					responseBuilder = Response.status(Response.Status.NOT_FOUND);
				}
			} else {
				Method getAllMethod = interfaceClass.getMethod("getAll", (Class<?>[]) null);
				@SuppressWarnings("unchecked")
				Set<Object> _kbSet = (Set<Object>) getAllMethod.invoke(null, (Object[]) null);
				Set<Object> _retSet = new HashSet<Object>();
				logger.trace("Class name {} {}", jenaClass.getCanonicalName(), interfaceClass.getCanonicalName());
				if (_kbSet != null) {
					for (Object _obj : _kbSet) {
						logger.trace("Object retrieved");
						_retSet.add(asMicroBeanMethod.invoke(jenaClass.cast(_obj), (Object[]) null));
					}
					responseBuilder = Response.ok(_retSet.toArray(new Object[_retSet.size()]));
				} else {
					logger.trace("null");
					responseBuilder = Response.status(Response.Status.NOT_FOUND);
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return responseBuilder.build();
	}

	private static Object getObjectFromStringValueCollectionsParameters(String value, Method method) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, java.lang.InstantiationException {
		Type[] parameters = method.getGenericParameterTypes();
		// Lizard methods defines only one parameters Set<T> for set methods
		ParameterizedType type = (ParameterizedType) parameters[0];
		Type typeArgument = type.getActualTypeArguments()[0];
		Class<?> typeArgClass = (Class<?>) typeArgument;
		return getValueFromType(value, typeArgClass);
	}

	private static Object getObjectFromStringValueSimpleParameters(String value, Method method) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, java.lang.InstantiationException {
		return getValueFromType(value, method.getParameters()[0].getType());
	}

	private static Object getValueFromType(String value, Class<?> type) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, java.lang.InstantiationException {
		boolean lizardInterface = false;
		for (Class<?> i : type.getInterfaces()) {
			if (i.equals(LizardInterface.class)) {
				lizardInterface = true;
			}
		}
		if (lizardInterface) {
			// The property is an object property
			String[] packages = type.getCanonicalName().split("\\.");
			String[] newPackages = new String[packages.length + 1];
			for (int i = 0; i < packages.length - 1; i++) {
				newPackages[i] = packages[i];
			}
			newPackages[packages.length - 1] = "jena";
			newPackages[newPackages.length - 1] = type.getSimpleName() + "Jena";
			logger.trace("Value lizard jena class {}", StringUtils.join(newPackages, "."));
			Class<?> jenaClassValueClass = Class.forName(StringUtils.join(newPackages, "."));
			return jenaClassValueClass.getConstructor(String.class).newInstance(value);
		} else {
			return ReflectionUtils.instatiateDatatype(value, type);
		}
	}

	@PUT
	@Path("/{class_name}/{property}")
	public Response setEntityProperty(@PathParam("ontology") String ontology, @PathParam("class_name") String className, @PathParam("property") String property, @QueryParam("iri") String id, @QueryParam("value") String value) {
		logger.trace("Set {} {} {} {} {}", ontology, className, property, id, value);
		Response.ResponseBuilder responseBuilder = null;
		String interfaceClassName = getAbsoluteInterfaceName(ontology, className);
		String jenaClassName = getAbsoluteJenaClassName(ontology, className);
		try {
			Class<?> interfaceClass = Class.forName(interfaceClassName);
			Class<?> jenaClass = Class.forName(jenaClassName);
			Method asMicroBeanMethod = jenaClass.getMethod("asMicroBean", (Class<?>[]) null);
			Method getMethod = interfaceClass.getMethod("get", String.class);
			Method[] methods = interfaceClass.getMethods();
			List<Method> setMethods = new ArrayList<Method>();
			for (Method m : methods) {
				if (m.getName().equals("set" + property)) {
					setMethods.add(m);
				}
			}
			Object entity = getMethod.invoke(null, id);
			Set<Object> toAdd = new HashSet<Object>();
			if (entity != null) {
				logger.trace("Found");
				for (Method m : setMethods) {
					toAdd.add(getObjectFromStringValueCollectionsParameters(value, m));
					m.invoke(entity, toAdd);
				}
				Set<Object> retSet = new HashSet<Object>();
				retSet.add(asMicroBeanMethod.invoke(jenaClass.cast(entity), (Object[]) null));
				responseBuilder = Response.ok(retSet.toArray(new Object[retSet.size()]));
			} else {
				logger.trace("Not found");
				responseBuilder = Response.status(Response.Status.NOT_FOUND);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | java.lang.InstantiationException e) {
			e.printStackTrace();
		}
		return responseBuilder.build();
	}

	@POST
	@Path("/{class_name}/{property}")
	public Response addEntityProperty(@PathParam("ontology") String ontology, @PathParam("class_name") String className, @PathParam("property") String property, @QueryParam("iri") String id, @QueryParam("value") String value) {
		logger.trace("Set {} {} {} {} {}", ontology, className, property, id, value);
		Response.ResponseBuilder responseBuilder = null;
		String interfaceClassName = getAbsoluteInterfaceName(ontology, className);
		String jenaClassName = getAbsoluteJenaClassName(ontology, className);
		try {
			Class<?> interfaceClass = Class.forName(interfaceClassName);
			Class<?> jenaClass = Class.forName(jenaClassName);
			Method asMicroBeanMethod = jenaClass.getMethod("asMicroBean", (Class<?>[]) null);
			Method getMethod = interfaceClass.getMethod("get", String.class);
			Method[] methods = interfaceClass.getMethods();
			List<Method> addAllMethods = new ArrayList<Method>();
			for (Method m : methods) {
				if (m.getName().equals("addAll" + property)) {
					addAllMethods.add(m);
				}
			}
			Object entity = getMethod.invoke(null, id);
			Set<Object> toAdd = new HashSet<Object>();
			if (entity != null) {
				logger.trace("Found");
				for (Method m : addAllMethods) {
					toAdd.add(getObjectFromStringValueCollectionsParameters(value, m));
					m.invoke(entity, toAdd);
				}
				Set<Object> retSet = new HashSet<Object>();
				retSet.add(asMicroBeanMethod.invoke(jenaClass.cast(entity), (Object[]) null));
				responseBuilder = Response.ok(retSet.toArray(new Object[retSet.size()]));
			} else {
				logger.trace("Not found");
				responseBuilder = Response.status(Response.Status.NOT_FOUND);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | java.lang.InstantiationException e) {
			e.printStackTrace();
		}
		return responseBuilder.build();
	}
	
	@DELETE
	@Path("/{class_name}/{property}")
	public Response removeProperty(@PathParam("ontology") String ontology, @PathParam("class_name") String className, @PathParam("property") String property, @QueryParam("iri") String id, @QueryParam("value") String value) {
		logger.trace("Remove {} {} {} {} {}", ontology, className, property, id, value);
		Response.ResponseBuilder responseBuilder = null;
		String interfaceClassName = getAbsoluteInterfaceName(ontology, className);
		String jenaClassName = getAbsoluteJenaClassName(ontology, className);
		try {
			Class<?> interfaceClass = Class.forName(interfaceClassName);
			Class<?> jenaClass = Class.forName(jenaClassName);
			Method asMicroBeanMethod = jenaClass.getMethod("asMicroBean", (Class<?>[]) null);
			Method getMethod = interfaceClass.getMethod("get", String.class);
			Method[] methods = interfaceClass.getMethods();
			List<Method> removeAllMethods = new ArrayList<Method>();
			for (Method m : methods) {
				if (m.getName().equals("removeAll" + property)) {
					removeAllMethods.add(m);
				}
			}
			Object entity = getMethod.invoke(null, id);
			Set<Object> toRemove = new HashSet<Object>();
			if (entity != null) {
				logger.trace("Found");
				for (Method m : removeAllMethods) {
					toRemove.add(getObjectFromStringValueCollectionsParameters(value, m));
					m.invoke(entity, toRemove);
				}
				Set<Object> retSet = new HashSet<Object>();
				retSet.add(asMicroBeanMethod.invoke(jenaClass.cast(entity), (Object[]) null));
				responseBuilder = Response.ok(retSet.toArray(new Object[retSet.size()]));
			} else {
				logger.trace("Not found");
				responseBuilder = Response.status(Response.Status.NOT_FOUND);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | java.lang.InstantiationException e) {
			e.printStackTrace();
		}
		return responseBuilder.build();
	}

	@GET
	@Path("/{class_name}/having/{property}")
	public Response getByProperty(@PathParam("ontology") String ontology, @PathParam("class_name") String className, @PathParam("property") String property, @QueryParam("value") String constraint) {
		logger.trace("Get by {} {} {} constraint {}", ontology, className, property, constraint);
		Response.ResponseBuilder responseBuilder = null;
		Set<Object> kbSet = new HashSet<Object>();
		String interfaceClassName = getAbsoluteInterfaceName(ontology, className);
		String jenaClassName = getAbsoluteJenaClassName(ontology, className);
		try {
			Class<?> jenaClass = Class.forName(jenaClassName);
			Class<?> interfaceClass = Class.forName(interfaceClassName);
			if (constraint != null) {
				Method[] methods = interfaceClass.getMethods();
				List<Method> getByMethods = new ArrayList<Method>();
				String getByMethodName = "getBy" + property.substring(0, 1).toUpperCase() + property.substring(1);
				for (Method m : methods) {
					if (m.getName().equals(getByMethodName) && m.getParameterTypes().length == 1) {
						getByMethods.add(m);
					}
				}
				for (Method getByMethod : getByMethods) {
					Object constraintObject = getObjectFromStringValueSimpleParameters(constraint, getByMethod);
					@SuppressWarnings("unchecked")
					Set<Object> entities = (Set<Object>) getByMethod.invoke(null, constraintObject);
					for (Object entity : entities) {
						Method asMicroBeanMethod = entity.getClass().getMethod("asMicroBean", (Class<?>[]) null);
						kbSet.add(asMicroBeanMethod.invoke(jenaClass.cast(entity), (Object[]) null));
					}
				}
			} else {
				Method[] methods = interfaceClass.getMethods();
				List<Method> getByMethods = new ArrayList<Method>();
				String getByMethodName = "getBy" + property.substring(0, 1).toUpperCase() + property.substring(1);
				for (Method m : methods) {
					if (m.getName().equals(getByMethodName) && m.getParameterTypes().length == 0) {
						getByMethods.add(m);
					}
				}
				for (Method getByMethod : getByMethods) {
					@SuppressWarnings("unchecked")
					Set<Object> entities = (Set<Object>) getByMethod.invoke(null, (Object[]) null);
					for (Object entity : entities) {
						Method asMicroBeanMethod = entity.getClass().getMethod("asMicroBean", (Class<?>[]) null);
						kbSet.add(asMicroBeanMethod.invoke(jenaClass.cast(entity), (Object[]) null));
					}
				}
			}

			if (kbSet.isEmpty()) {
				logger.trace("Not found");
				responseBuilder = Response.status(Response.Status.NOT_FOUND);
			} else {
				responseBuilder = Response.ok(kbSet.toArray(new Object[kbSet.size()]));
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | java.lang.InstantiationException e) {
			e.printStackTrace();
		}
		return responseBuilder.build();
	}

	@GET
	@Path("/{class_name}/{property}")
	public Response getProperty(@PathParam("ontology") String ontology, @PathParam("class_name") String className, @PathParam("property") String property, @QueryParam("iri") String id) {
		logger.trace("Get by {} {} {} entity {}", ontology, className, property, id);
		Response.ResponseBuilder responseBuilder = null;
		String interfaceClassName = getAbsoluteInterfaceName(ontology, className);
		try {
			Class<?> interfaceClass = Class.forName(interfaceClassName);
			Method getMethod = interfaceClass.getMethod("get", String.class);
			Object entity = getMethod.invoke(null, id);
			if (entity != null) {
				logger.trace("Found");
				String getMethodName = "get" + property.substring(0, 1).toUpperCase() + property.substring(1);
				Method getPropertyMethod = interfaceClass.getMethod(getMethodName, (Class<?>[]) null);
				Set<?> result = (Set<?>) getPropertyMethod.invoke(entity, (Object[]) null);
				responseBuilder = Response.ok(result.toArray(new Object[result.size()]));
			} else {
				logger.trace("Not found");
				responseBuilder = Response.status(Response.Status.NOT_FOUND);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return responseBuilder.build();
	}

	

}
