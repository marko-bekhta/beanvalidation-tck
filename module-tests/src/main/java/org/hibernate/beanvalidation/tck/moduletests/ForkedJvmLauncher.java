/*
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.moduletests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Launches a forked JVM with the synthetic user module on the module path.
 */
class ForkedJvmLauncher {

	private static final long TIMEOUT_SECONDS = 60;

	ModuleTestResult launch(ModuleTestBuilder builder) {
		Path tempDir = null;
		try {
			tempDir = Files.createTempDirectory( "validation-tck-module-" );
			Path moduleDir = tempDir.resolve( builder.getModuleName() );

			List<Path> externalModulePath = ModulePathResolver.resolve();

			ModuleAssembly.assemble( moduleDir, builder.getClasses(),
					builder.getModuleInfoSource(), externalModulePath );

			List<Path> fullModulePath = new ArrayList<>( externalModulePath );
			fullModulePath.add( moduleDir );

			List<String> command = buildCommand( fullModulePath, builder );

			ProcessBuilder pb = new ProcessBuilder( command );
			pb.redirectErrorStream( false );
			Process process = pb.start();

			CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync( () -> {
				try ( var reader = process.inputReader() ) {
					return reader.lines().collect( Collectors.joining( "\n" ) );
				}
				catch (IOException e) {
					return "ERROR reading stdout: " + e.getMessage();
				}
			} );
			CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync( () -> {
				try ( var reader = process.errorReader() ) {
					return reader.lines().collect( Collectors.joining( "\n" ) );
				}
				catch (IOException e) {
					return "ERROR reading stderr: " + e.getMessage();
				}
			} );

			boolean finished = process.waitFor( TIMEOUT_SECONDS, TimeUnit.SECONDS );
			if ( !finished ) {
				process.destroyForcibly();
				String stdout = stdoutFuture.get( 5, TimeUnit.SECONDS );
				String stderr = stderrFuture.get( 5, TimeUnit.SECONDS );
				return new ModuleTestResult( -1,
						stdout + "\nTIMEOUT: Process did not complete within " + TIMEOUT_SECONDS + "s",
						stderr );
			}

			String stdout = stdoutFuture.get( 5, TimeUnit.SECONDS );
			String stderr = stderrFuture.get( 5, TimeUnit.SECONDS );

			return new ModuleTestResult( process.exitValue(), stdout, stderr );
		}
		catch (Exception e) {
			return new ModuleTestResult( -1, "",
					"ForkedJvmLauncher failed: " + e.getClass().getName() + ": " + e.getMessage() );
		}
		finally {
			if ( tempDir != null ) {
				deleteRecursively( tempDir );
			}
		}
	}

	private List<String> buildCommand(List<Path> modulePath, ModuleTestBuilder builder) {
		String javaHome = System.getProperty( "java.home" );
		String javaExe = Path.of( javaHome, "bin", "java" ).toString();

		String modulePathStr = modulePath.stream()
				.map( Path::toString )
				.collect( Collectors.joining( System.getProperty( "path.separator" ) ) );

		String mainModule = builder.getModuleName() + "/"
				+ ModuleTestRunner.class.getName();
		String actionClassName = builder.getActionClass().getName();

		List<String> cmd = new ArrayList<>();
		cmd.add( javaExe );
		cmd.add( "--module-path" );
		cmd.add( modulePathStr );
		cmd.add( "--module" );
		cmd.add( mainModule );
		cmd.add( actionClassName );

		return cmd;
	}

	private static void deleteRecursively(Path path) {
		try {
			if ( Files.isDirectory( path ) ) {
				try ( Stream<Path> entries = Files.list( path ) ) {
					entries.forEach( ForkedJvmLauncher::deleteRecursively );
				}
			}
			Files.deleteIfExists( path );
		}
		catch (IOException ignore) {
			// Best effort cleanup
		}
	}
}
