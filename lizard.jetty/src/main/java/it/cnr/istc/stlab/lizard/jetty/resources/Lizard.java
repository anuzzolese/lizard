package it.cnr.istc.stlab.lizard.jetty.resources;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import it.cnr.istc.stlab.lizard.commons.jena.RuntimeJenaLizardContext;

@Path("/lizard")
@Api(value = "/lizard")
public class Lizard {

	/**
	 * 
	 * @param request
	 *            a String serialization a Json Object that defines the ontologyURI to use to generate the API and the KB type. {"ontologyURI":"", "KBType":""}
	 * 
	 * @return The binary file containing the API
	 * @throws FileNotFoundException
	 */
	@POST
	@Path("generateOntologyAPI")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("application/x-java-applet")
	@ApiOperation(value = "Generate ontology API", notes = "Generates the API for managing the ontology.")
	public javax.ws.rs.core.Response generateOntologyApi(String request) throws FileNotFoundException {

		JSONObject json = new JSONObject(request);

		String ontologyURI = json.getString("ontologyURI");
		String KBType = json.getString("KBType");

		System.out.println("Ontology uri " + ontologyURI);
		System.out.println("KB type " + KBType);

		// TODO generate API
		String pathToJarFileToReturn = "";

		return Response.ok(new FileInputStream(pathToJarFileToReturn)).type("application/x-java-applet").build();
	}

	@GET
	@Path("resetContext")
	@ApiOperation(value = "Reset the context of RuntimeJenaLizard.", notes = "Reset the context of RuntimeJenaLizard.")
	public javax.ws.rs.core.Response refreshModel() throws FileNotFoundException {
		RuntimeJenaLizardContext.resetContext();
		return Response.ok().build();
	}
}
