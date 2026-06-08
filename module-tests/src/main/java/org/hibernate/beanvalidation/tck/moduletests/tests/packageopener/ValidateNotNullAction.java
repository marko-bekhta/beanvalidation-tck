/*
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.moduletests.tests.packageopener;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.beanvalidation.tck.moduletests.ModuleTestAction;

public class ValidateNotNullAction implements ModuleTestAction {

	@Override
	public void execute() throws Exception {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<TestBean>> violations = validator.validate( new TestBean() );
		if ( violations.isEmpty() ) {
			throw new AssertionError( "Expected at least one constraint violation for null 'name' field" );
		}
	}
}
