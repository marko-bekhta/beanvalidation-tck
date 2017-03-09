/**
 * Bean Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.tests.validation;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecVersion;
import org.testng.annotations.Test;

import org.hibernate.beanvalidation.tck.tests.BaseValidatorTest;
import org.hibernate.beanvalidation.tck.util.shrinkwrap.WebArchiveBuilder;

import static org.hibernate.beanvalidation.tck.util.TestUtil.assertCorrectConstraintTypes;
import static org.hibernate.beanvalidation.tck.util.TestUtil.webArchiveBuilder;

/**
 * @author Gunnar Morling
 */
@SpecVersion(spec = "beanvalidation", version = "2.0.0")
public class GetterDefinitionTest extends BaseValidatorTest {

	@Deployment
	public static WebArchive createTestArchive() {
		return webArchiveBuilder()
				.withTestClass( GetterDefinitionTest.class )
				.withClasses( Shipment.class )
				.build();
	}

	@Test
	@SpecAssertion(section = "4.1", id = "d")
	public void testGetterMethod() {
		Shipment shipment = new Shipment();

		Set<ConstraintViolation<Shipment>> constraintViolations = getValidator().validateProperty( shipment, "id" );
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
	}

	@Test
	@SpecAssertion(section = "4.1", id = "d")
	public void testBooleanGetterMethod() {
		Shipment shipment = new Shipment();

		Set<ConstraintViolation<Shipment>> constraintViolations = getValidator().validateProperty( shipment, "shipped" );
		assertCorrectConstraintTypes( constraintViolations, AssertTrue.class );
	}
}
