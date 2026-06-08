/*
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.moduletests;

/**
 * Thrown when module assembly fails (class file not found, compilation error, etc.).
 */
public class ModuleAssemblyException extends RuntimeException {

	public ModuleAssemblyException(String message) {
		super( message );
	}

	public ModuleAssemblyException(String message, Throwable cause) {
		super( message, cause );
	}
}
