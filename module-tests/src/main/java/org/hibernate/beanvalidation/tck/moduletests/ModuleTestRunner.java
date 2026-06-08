/*
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.moduletests;

/**
 * Entry point for the forked JVM. Invoked as:
 * {@code --module test.user.app/org.hibernate.beanvalidation.tck.moduletests.ModuleTestRunner <actionClassName>}
 * <p>
 * This class is copied into the synthetic user module alongside the action classes.
 */
public class ModuleTestRunner {

	public static final String SUCCESS_PREFIX = "MODULE_TEST_SUCCESS";
	public static final String FAILURE_PREFIX = "MODULE_TEST_FAILURE";

	public static void main(String[] args) {
		if ( args.length != 1 ) {
			System.out.println( FAILURE_PREFIX + ": Expected exactly one argument (action class name)" );
			System.exit( 1 );
		}

		String actionClassName = args[0];
		try {
			Class<?> actionClass = Class.forName( actionClassName );
			ModuleTestAction action = (ModuleTestAction) actionClass.getDeclaredConstructor().newInstance();
			action.execute();
			System.out.println( SUCCESS_PREFIX );
			System.exit( 0 );
		}
		catch (Throwable t) {
			System.out.println( FAILURE_PREFIX + ": " + t.getClass().getName() + ": " + t.getMessage() );
			t.printStackTrace( System.out );
			System.exit( 1 );
		}
	}
}
