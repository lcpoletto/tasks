/**
 * 
 */
package com.lcpoletto;

import org.apache.log4j.Logger;

/**
 * Class which holds helper and utility methods that might be used by multiple
 * others.
 * 
 * TODO: maybe there is a better way to do this with java 8? default methods on
 * interfaces?
 * 
 * @author Luis Carlos Poletto
 *
 */
public class Utils {

    private static final Logger logger = Logger.getLogger(Utils.class);

    private static final String DEFAULT_EMAIL_FROM = "noreply@lcpoletto.com";

    /**
     * Avoids instantiation.
     */
    private Utils() {
    }

    /**
     * Helper method which will try to read configuration from environment
     * variables.
     * 
     * @return configured email source or default value
     */
    public static String getMailFrom() {
        final String result = System.getenv().getOrDefault("TASKS_MAIL_FROM", DEFAULT_EMAIL_FROM);
        logger.trace(String.format("TASKS_MAIL_FROM: %s", result));
        return result;
    }

}
