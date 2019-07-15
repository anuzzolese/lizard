#### Conversion Datatype to Java Class

- The mapping between Datatype and Java Class is implemented in it.cnr.istc.stlab.lizard.commons.model.datatype.DatatypeCodeInterface.
- It relies on Apache Jena's TypeMapper that associates a RDF Datatype to a Java Class.
- When an datatype is not associated with any Java Class, the default xsd:string is used as default datatype.

#### Issues When Parsing Ontologies
- Classes not defined as owl:Class (e.g. those specified as rdfs:Class) cannot be used as OntClass. In order to overcome this issue we set strictMode of OntModel as false in  it.cnr.istc.stlab.lizard.core.OntologyProjectGenerationRecipe.


#### Check
- Resources that are identified as both properties and classes. Check it.cnr.istc.stlab.lizard.core.model.RestOntologyCodeModel. So far the strategy is to generate a java class for any java resource that is identified as Class (even if it is also a property).


#### Fixings
- Fixed add individuals in JenaOntologyCodeClass.
