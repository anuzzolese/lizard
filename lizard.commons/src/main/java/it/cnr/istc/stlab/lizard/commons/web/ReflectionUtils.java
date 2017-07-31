package it.cnr.istc.stlab.lizard.commons.web;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.XSDDuration;

public class ReflectionUtils {

	public static Set<Class<?>> getAllExtendedOrImplementedTypesRecursively(Class<?> clazz) {
		List<Class<?>> res = new ArrayList<>();
		do {
			res.add(clazz);

			// First, add all the interfaces implemented by this class
			Class<?>[] interfaces = clazz.getInterfaces();
			if (interfaces.length > 0) {
				res.addAll(Arrays.asList(interfaces));
				for (Class<?> interfaze : interfaces) {
					res.addAll(getAllExtendedOrImplementedTypesRecursively(interfaze));
				}
			}

			// Add the super class
			Class<?> superClass = clazz.getSuperclass();

			// Interfaces does not have java,lang.Object as superclass, they have null, so break the cycle and return
			if (superClass == null) {
				break;
			}

			// Now inspect the superclass
			clazz = superClass;
		} while (!"java.lang.Object".equals(clazz.getCanonicalName()));

		return new HashSet<Class<?>>(res);
	}

	public static Object instatiateDatatype(String value, Class<?> c) {
		if (c.equals(String.class)) {
			return value;
		} else if (c.equals(URI.class)) {
			try {
				return new URI(value);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} else if (c.equals(Double.class)) {
			return Double.parseDouble(value);
		} else if (c.equals(Double.class)) {
			return Integer.parseInt(value);
		} else if (c.equals(Float.class)) {
			return Float.parseFloat(value);
		} else if (c.equals(XSDDateTime.class)) {
			return ((XSDDateTime) TypeMapper.getInstance().getTypeByClass(XSDDateTime.class).parse(value));
		} else if (c.equals(BigInteger.class)) {
			return new BigInteger(value);
		} else if (c.equals(BigDecimal.class)) {
			return new BigDecimal(value);
		} else if (c.equals(Boolean.class)) {
			return Boolean.parseBoolean(value);
		} else if (c.equals(Short.class)) {
			return Short.parseShort(value);
		} else if (c.equals(Long.class)) {
			return Long.parseLong(value);
		} else if (c.equals(XSDDuration.class)) {
			return ((XSDDuration) TypeMapper.getInstance().getTypeByClass(XSDDuration.class).parse(value));
		} else if (c.equals(Byte.class)) {
			return Byte.parseByte(value);
		}

		throw new InstantiationException();
	}

}
