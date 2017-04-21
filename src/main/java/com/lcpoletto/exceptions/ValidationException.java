/**
 * 
 */
package com.lcpoletto.exceptions;

/**
 * Exception class to represent a validation failure.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = -641540785468580102L;

    /**
     * Main constructor which uses {@link String#format(String, Object...)} to
     * generate it's message.
     * 
     * @param format
     * @param args
     */
    public ValidationException(String format, Object... args) {
        super(String.format(format, args));
    }
}
