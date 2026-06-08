/*
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.moduletests.tests.packageopener;

import org.hibernate.beanvalidation.tck.moduletests.AbstractModulePathTCKTest;
import org.testng.annotations.Test;

/**
 * Tests that validation fails when the user module does not open
 * any package. The provider cannot reflectively access the bean's
 * fields regardless of whether it uses the package opener.
 */
public class NoOpensPackageOpenerTest extends AbstractModulePathTCKTest {

	@Test
	public void validationFailsWhenPackageNotOpened() {
		runModuleTest()
				.withModuleInfo( """
						module test.user.app {
							requires jakarta.validation;
						}
						""" )
				.withClasses( TestBean.class, ValidateNotNullAction.class )
				.execute( ValidateNotNullAction.class )
				.assertFailure();
	}
}
