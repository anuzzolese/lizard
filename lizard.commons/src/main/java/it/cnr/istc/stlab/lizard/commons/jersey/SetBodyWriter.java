package it.cnr.istc.stlab.lizard.commons.jersey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class SetBodyWriter implements MessageBodyWriter<HashSet<?>> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true;
	}

	@Override
	public long getSize(HashSet<?> t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeTo(HashSet<?> t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		
		JSONArray setArray = new JSONArray();
		
		t.forEach(bean -> {
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				JAXBContext context = JAXBContext.newInstance(bean.getClass());
				context.createMarshaller().marshal(bean, stream);
				JSONObject obj = new JSONObject(new String(stream.toByteArray()));
				setArray.put(obj);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		entityStream.write(setArray.toString().getBytes());
	}
	
	
}
