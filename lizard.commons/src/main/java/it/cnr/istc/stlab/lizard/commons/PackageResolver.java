package it.cnr.istc.stlab.lizard.commons;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.ontology.OntResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Andrea Nuzzolese
 *
 */

public class PackageResolver {

	private static Logger logger = LoggerFactory.getLogger(PackageResolver.class);

	public static String resolve(OntResource ontResource) throws URISyntaxException {
		URI ontologyURI;
		if (ontResource.isURIResource()) {
			ontologyURI = new URI(ontResource.getNameSpace());

		} else {
			ontologyURI = new URI(ontResource.getOntModel().getNsPrefixURI(""));
		}

		return resolve(ontologyURI);

	}

	public static String resolve(URI ontologyURI) {
		return resolveGroupId(ontologyURI) + "." + resolveArtifactId(ontologyURI);
	}

	public static String resolveGroupId(URI ontologyURI) {
		String host = ontologyURI.getHost();
		host = host.replaceAll("^www\\.", "");

		String[] hostParts = host.split("\\.");
		StringBuilder sb = new StringBuilder();
		for (String hostPart : hostParts) {
			if (sb.length() > 0)
				sb.insert(0, ".");
			hostPart = normalize(hostPart);
			sb.insert(0, hostPart);
		}

		return sb.toString();
	}

	public static String resolveArtifactId(URI ontologyURI) {

		StringBuilder sb = new StringBuilder();
		String path = ontologyURI.getPath();
		String[] pathParts = path.split("/");
		int count = 0;
		for (String pathPart : pathParts) {
			if (sb.length() > 0 && count > 0)
				sb.append(".");
			pathPart = normalize(pathPart);
			count += 1;
			if (count == pathParts.length) {
				int index = pathPart.indexOf(".");
				if (index > 0)
					pathPart = pathPart.substring(0, index);
			}

			Pattern pattern = Pattern.compile("^([0-9]+)");
			Matcher matcher = pattern.matcher(pathPart);
			pathPart = matcher.replaceAll("_$1");
			sb.append(pathPart);
		}

		return sb.toString();
	}

	private static String normalize(String packageString) {
		return packageString.replaceAll("\\.|-", "_");
	}

	public static String packageNameToUrlPath(String packageName) {
		return packageName.replaceAll("\\.", "_");
	}

	public static String urlPathToPackageName(String packageName) {

		logger.trace("Input {}", packageName);

		String a = packageName.replaceAll("_", ".");
		if (a.matches(".*\\.\\..*")) {
			a = a.replaceAll("\\.\\.", "._");
			return a;
		}

		String[] s = a.split("\\.");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length - 2; i++) {
			sb.append(s[i]);
			sb.append(".");
		}
		sb.append(s[s.length - 2]);
		if (s[s.length - 1].equals("owl"))
			sb.append("_");
		else
			sb.append(".");
		sb.append(s[s.length - 1]);
		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(urlPathToPackageName("org_w3__2002__07_owl"));
	}

}
