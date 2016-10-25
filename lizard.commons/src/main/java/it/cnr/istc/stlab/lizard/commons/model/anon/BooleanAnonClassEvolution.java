package it.cnr.istc.stlab.lizard.commons.model.anon;

import it.cnr.istc.stlab.lizard.commons.inmemory.InMemoryLizardClass;
import it.cnr.istc.stlab.lizard.commons.model.AbstractOntologyCodeClass;

import java.util.HashSet;
import java.util.Set;

import com.sun.codemodel.JAnnotationArrayMember;

public abstract class BooleanAnonClassEvolution extends InMemoryLizardClass {
	
	protected JAnnotationArrayMember annotationArray;
	Set<AbstractOntologyCodeClass> members;
	
	public BooleanAnonClassEvolution() {
		super();
		this.members = new HashSet<AbstractOntologyCodeClass>();
	}
	
	public BooleanAnonClassEvolution(AbstractOntologyCodeClass...members) {
		this();
		for(AbstractOntologyCodeClass member : members)
			this.members.add(member);
	}

	public void addMember(AbstractOntologyCodeClass ontologyCodeClass){}
	
	@Override
    public int hashCode() {
    	StringBuilder sb = new StringBuilder();
    	for(AbstractOntologyCodeClass member : members){
    		sb.append(member.getOntResource().toString());
    	}
    	return sb.toString().hashCode();
    }
	
}
