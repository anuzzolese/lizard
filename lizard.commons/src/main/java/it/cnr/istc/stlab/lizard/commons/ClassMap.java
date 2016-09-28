package it.cnr.istc.stlab.lizard.commons;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * 
 * @author Andrea Nuzzolese
 *
 */
public class ClassMap{

	private Map<Class<? extends Object>, Object> classMap;
	
	public ClassMap() {
		classMap = new HashMap<Class<? extends Object>, Object>();
	}
	
	public void put(Class<? extends Object> objectClass, Object object){
		classMap.put(objectClass, object);
	}
	
	public Object remove(Class<? extends Object> objectClass){
		return classMap.remove(objectClass);
	}
	
	public Object get(Class<? extends Object> objectClass){
		return classMap.get(objectClass);
	}
	
	public static void main(String[] args) {
		ClassMap classMap = new ClassMap();
		Object r = ModelFactory.createDefaultModel().createResource("http://www.foo.org/ciccio");
		classMap.put(r.getClass(), r);
		
		Resource res = (Resource) classMap.get(r.getClass());
		System.out.println("ciccio2 + " + res);
	}
}
