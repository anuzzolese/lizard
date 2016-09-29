package it.cnr.istc.stlab.lizard.jetty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

@Path("/lizard")
public class Lizard {

	/**
	 * 
	 * @param request
	 *            a String serialization a Json Object that defines the
	 *            ontologyURI to use to generate the API and the KB type.
	 *            {"ontologyURI":"", "KBType":""}
	 * 
	 * @return The binary file containing the API
	 * @throws FileNotFoundException
	 */

	@POST
	@Path("generateOntologyAPI")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("application/x-java-applet")
	public javax.ws.rs.core.Response generateOntologyApi(String request)
			throws FileNotFoundException {

		JSONObject json = new JSONObject(request);

		String ontologyURI = json.getString("ontologyURI");
		String KBType = json.getString("KBType");

		System.out.println("Ontology uri " + ontologyURI);
		System.out.println("KB type " + KBType);

		// TODO generate API
		String pathToJarFile = "/Users/lgu/Dropbox/spider-resources/fn2wn/fn2w3instances/st2wn/st2wn30-core.ttl";

		return Response.ok(new FileInputStream(pathToJarFile))
				.type("application/x-java-applet").build();
	}
}
