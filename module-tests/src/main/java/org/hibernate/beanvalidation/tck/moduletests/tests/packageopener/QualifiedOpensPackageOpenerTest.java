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
 * specifically to {@code jakarta.validation}.
 * <p>
 * This is the core package opener test: the provider must use
 * {@code ValidationPackageOpener} to relay the opens to itself.
 * Without the opener, the provider cannot reflectively access the
 * bean's fields and validation fails.
 */
public class QualifiedOpensPackageOpenerTest extends AbstractModulePathTCKTest {

	@Test
	public void canValidateBeanWhenPackageOpenedToValidationModule() {
		runModuleTest()
				.withModuleInfo( """
						module test.user.app {
							requires jakarta.validation;
							opens %s to jakarta.validation;
						}
						""".formatted( TestBean.class.getPackageName() ) )
				.withClasses( TestBean.class, ValidateNotNullAction.class )
				.execute( ValidateNotNullAction.class )
				.assertSuccess();
	}
}
