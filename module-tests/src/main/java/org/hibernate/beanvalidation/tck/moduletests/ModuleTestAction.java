/*
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.moduletests;

/**
 * An action executed inside a forked JVM on the module path.
 * Implementations must have a public no-arg constructor.
 * <p>
 * If the action completes without throwing, the test is considered successful.
 * If it throws any {@link Throwable}, the test is considered failed.
 */
@FunctionalInterface
public interface ModuleTestAction {

	void execute() throws Exception;
}
