package it.cnr.istc.stlab.lizard.commons;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.ontology.OntResource;

import it.cnr.istc.stlab.lizard.commons.annotations.ObjectPropertyAnnotation;
import it.cnr.istc.stlab.lizard.commons.model.types.OntologyCodeMethodType;

public class PropertyMap {

	private Map<OntResource,ClassMap> propertyMap;
	
	public PropertyMap() {
		propertyMap = new HashMap<OntResource,ClassMap>();
	}
	
	public void put(OntResource ontResource, Object object){
		ClassMap classMap = propertyMap.get(ontResource);
		if(classMap == null){
			classMap = new ClassMap();
			propertyMap.put(ontResource, classMap);
		}
		classMap.put(object.getClass(), object);;
	}
	
	public Object remove(OntResource ontResource, Class<? extends Object> objectClass){
		Object obj = null;
		ClassMap classMap = propertyMap.get(ontResource);
		if(classMap != null){
			obj = classMap.remove(objectClass);
		}
		return obj;
	}
	
	public Object get(OntResource ontResource, Class<? extends Object> objectClass){
		Object obj = null;
		ClassMap classMap = propertyMap.get(ontResource);
		if(classMap != null){
			obj = classMap.get(objectClass);
		}
		return obj;
	}
	
	public boolean hasProperty(OntResource ontResource){
		return propertyMap.containsKey(ontResource);
	}
	
	@ObjectPropertyAnnotation(method=OntologyCodeMethodType.Get, uri="http://www.foo.org/")
	public void test(){
		Method method;
		try {
			
			method = new Object(){}.getClass().getEnclosingMethod();
			System.out.println(method);
			ObjectPropertyAnnotation objectPropertyAnnotation = method.getAnnotation(ObjectPropertyAnnotation.class);
			
			System.out.println("Method: " + objectPropertyAnnotation.method());
			System.out.println("Uri: " + objectPropertyAnnotation.uri());
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		PropertyMap pm = new PropertyMap();
		pm.test();
	}
	
}
