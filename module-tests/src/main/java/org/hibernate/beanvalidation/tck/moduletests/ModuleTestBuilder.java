/*
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.moduletests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder for configuring a single module path test scenario.
 * Created via {@link AbstractModulePathTCKTest#runModuleTest()}.
 */
public class ModuleTestBuilder {

	private String moduleInfoSource;
	private final List<Class<?>> classes = new ArrayList<>();
	private Class<? extends ModuleTestAction> actionClass;
	private String moduleName = "test.user.app";

	ModuleTestBuilder() {
	}

	/**
	 * Sets the {@code module-info.java} source code for the synthetic user module.
	 */
	public ModuleTestBuilder withModuleInfo(String moduleInfoSource) {
		this.moduleInfoSource = moduleInfoSource;
		return this;
	}

	/**
	 * Adds classes whose {@code .class} files will be copied into the synthetic module.
	 * These classes must be on the TCK's classpath.
	 */
	public ModuleTestBuilder withClasses(Class<?>... classes) {
		Collections.addAll( this.classes, classes );
		return this;
	}

	/**
	 * Overrides the default module name {@code "test.user.app"}.
	 */
	public ModuleTestBuilder withModuleName(String moduleName) {
		this.moduleName = moduleName;
		return this;
	}

	/**
	 * Specifies the action class to execute and triggers the build + fork.
	 * Returns a {@link ModuleTestResult} for assertions.
	 */
	public ModuleTestResult execute(Class<? extends ModuleTestAction> actionClass) {
		this.actionClass = actionClass;

		if ( !this.classes.contains( actionClass ) ) {
			this.classes.add( actionClass );
		}
		if ( !this.classes.contains( ModuleTestAction.class ) ) {
			this.classes.add( ModuleTestAction.class );
		}
		if ( !this.classes.contains( ModuleTestRunner.class ) ) {
			this.classes.add( ModuleTestRunner.class );
		}

		return new ForkedJvmLauncher().launch( this );
	}

	String getModuleInfoSource() {
		return moduleInfoSource;
	}

	List<Class<?>> getClasses() {
		return classes;
	}

	Class<? extends ModuleTestAction> getActionClass() {
		return actionClass;
	}

	String getModuleName() {
		return moduleName;
	}
}
