/**
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.tests.constraints.validatorresolution;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * @author Hardy Ferentschik
 */
public class MinMax {
	@Min(10l)
	@Max(20l)
	Number number;

	public MinMax(Number number) {
		this.number = number;
	}
}
