/*
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.moduletests;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the module path for the forked JVM.
 * <p>
 * The module path must include the {@code jakarta.validation-api} JAR (the API module)
 * and the provider JAR(s) with their transitive dependencies.
 * <p>
 * Discovery strategy:
 * <ol>
 *   <li>System property {@value #MODULE_PATH_PROPERTY}: path-separator-separated list
 *       of JARs or directories. If set, this is used directly.</li>
 *   <li>Fallback: all entries from {@code java.class.path} are placed on the module path.
 *       JARs without {@code module-info.class} become automatic modules.</li>
 * </ol>
 */
class ModulePathResolver {

	static final String MODULE_PATH_PROPERTY = "validation.tck.module.path";

	static List<Path> resolve() {
		String explicit = System.getProperty( MODULE_PATH_PROPERTY );
		if ( explicit != null && !explicit.isBlank() ) {
			return parseExplicitModulePath( explicit );
		}
		return discoverFromClasspath();
	}

	private static List<Path> parseExplicitModulePath(String value) {
		List<Path> result = new ArrayList<>();
		for ( String entry : value.split( File.pathSeparator ) ) {
			String trimmed = entry.trim();
			if ( !trimmed.isEmpty() ) {
				Path p = Path.of( trimmed );
				if ( !Files.exists( p ) ) {
					throw new ModuleAssemblyException(
							"Path specified in " + MODULE_PATH_PROPERTY + " does not exist: " + trimmed );
				}
				result.add( p );
			}
		}
		if ( result.isEmpty() ) {
			throw new ModuleAssemblyException(
					MODULE_PATH_PROPERTY + " is set but contains no valid paths" );
		}
		return result;
	}

	private static List<Path> discoverFromClasspath() {
		List<Path> result = new ArrayList<>();
		String classpath = System.getProperty( "java.class.path", "" );
		String[] entries = classpath.split( File.pathSeparator );

		for ( String entry : entries ) {
			String trimmed = entry.trim();
			if ( trimmed.isEmpty() ) {
				continue;
			}
			Path p = Path.of( trimmed );
			if ( Files.exists( p ) ) {
				result.add( p );
			}
		}

		if ( result.isEmpty() ) {
			throw new ModuleAssemblyException(
					"Classpath is empty and " + MODULE_PATH_PROPERTY + " is not set." );
		}

		return result;
	}
}
