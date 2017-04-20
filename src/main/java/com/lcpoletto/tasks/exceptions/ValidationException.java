/**
 * 
 */
package com.lcpoletto.tasks.exceptions;

/**
 * Exception class to represent a validation failure.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class ValidationException extends Exception {

	private static final long serialVersionUID = -641540785468580102L;

	public ValidationException(String message) {
		super(message);
	}
}
