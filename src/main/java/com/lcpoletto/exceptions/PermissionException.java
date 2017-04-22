package com.lcpoletto.exceptions;

import static java.lang.String.format;

/**
 * Exception class that will be thrown when there is a permission issue with the
 * action being executed.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class PermissionException extends RuntimeException {

    private static final long serialVersionUID = -4915930921128038387L;

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
     * TODO: Configure this as a 403 on API gateway
     * 
     * @param format
     *            string format
     * @param args
     *            formar arguments
     */
    public PermissionException(String format, Object... args) {
        super(format("[%s]: %s", PermissionException.class.getSimpleName(), format(format, args)));
    }

}
