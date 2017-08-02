# Lizard in a nutshell
Lizard automatically generates language-agnostic APIs for accessing knowledge bases without knowing of Semantic Web languages (such as RDF, OWL or SPARQL).
To generate the APIs of a KB Lizard only needs of the ontology that describes the schema of the information containted in the knowledge base.
Given as input an ontology, Lizard generates a Java library for accessing the target knowledge base.
The generated APIs expose the RDF triples as sets of resources and seamlessly integrates them into the Object Oriented paradigm.
Moreover, Lizard provides a RESTful layer that exposes Object Oriented paradigm by using the REST architectural style over HTTP.
The RESTful layer is described in [link Swagger](https://swagger.io/) notation.
The Swagger description enable API users to generate a RESTful client in [link over 40 different](https://swagger.io/swagger-codegen/) programming languages.
