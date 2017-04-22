/**
 * 
 */
package com.lcpoletto.exceptions;

import static java.lang.String.format;

/**
 * Exception class which will be thrown when the object can't be found on the
 * persistence layer.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class ObjectNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -6665186190115162032L;

    /**
     * Main constructor which uses {@link String#format(String, Object...)} to
     * generate it's message.
     * 
     * <p>
     * Implementation note: I've added the <code>[class_name]:</code> as a
     * prefix of the error message because of this information on AWS
     * documentation:
     * </p>
     * 
     * <code>
     * "The error patterns are matched against the entire string of the
     * <strong>errorMessage property</strong> in the Lambda response"
     * </code>
     * 
     * @param format
     *            string format
     * @param args
     *            formar arguments
     */
    public ObjectNotFoundException(String format, Object... args) {
        super(format("[%s]: %s", ObjectNotFoundException.class.getSimpleName(), format(format, args)));
    }

}
