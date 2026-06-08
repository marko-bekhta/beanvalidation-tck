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
 * Tests that validation works when the user module opens a package
 * to everyone (unqualified opens). This is the baseline scenario
 * that should work even without the package opener.
 */
public class UnqualifiedOpensPackageOpenerTest extends AbstractModulePathTCKTest {

	@Test
	public void canValidateBeanWhenPackageOpenedUnqualified() {
		runModuleTest()
				.withModuleInfo( """
						module test.user.app {
							requires jakarta.validation;
							opens %s;
						}
						""".formatted( TestBean.class.getPackageName() ) )
				.withClasses( TestBean.class, ValidateNotNullAction.class )
				.execute( ValidateNotNullAction.class )
				.assertSuccess();
	}
}
