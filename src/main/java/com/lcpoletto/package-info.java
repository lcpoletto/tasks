/**
 * Main application package. Subpackages <code>notes</code> and
 * <code>tasks</code> hold the lambda functions, on a larger scale project it
 * would make more sense to have at least one of the following: dependency
 * injection, object factories or abstract superclasses to handle the
 * interaction with AWS services, that would enable us to control that
 * interation from a single entry point and reduce the code duplication.
 */
package com.lcpoletto;