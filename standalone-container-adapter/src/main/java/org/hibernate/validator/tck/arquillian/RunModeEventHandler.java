/*
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.tck.arquillian;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.junit5.extension.RunModeEvent;

public class RunModeEventHandler {

	public void handleEvent(@Observes(precedence = -1) RunModeEvent event) {
		event.setRunAsClient( true );
	}
}
