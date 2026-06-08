/*
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.moduletests;

import javax.tools.ToolProvider;

import org.testng.annotations.BeforeClass;

/**
 * Base class for all JPMS module path TCK tests.
 * Provides the entry point for the fluent builder API.
 */
public abstract class AbstractModulePathTCKTest {

	@BeforeClass
	public void verifyModulePathPrerequisites() {
		if ( ToolProvider.getSystemJavaCompiler() == null ) {
			throw new IllegalStateException(
					"Module path tests require a JDK (not JRE). "
							+ "No system Java compiler is available." );
		}

		ModulePathResolver.resolve();
	}

	/**
	 * Creates a new {@link ModuleTestBuilder} for configuring and executing
	 * a module path test scenario.
	 */
	protected ModuleTestBuilder runModuleTest() {
		return new ModuleTestBuilder();
	}
}
