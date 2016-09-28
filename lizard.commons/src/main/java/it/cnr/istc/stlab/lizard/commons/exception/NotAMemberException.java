package it.cnr.istc.stlab.lizard.commons.exception;

public class NotAMemberException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -528848749157326001L;
	
	private String message;

	public NotAMemberException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
	
}
