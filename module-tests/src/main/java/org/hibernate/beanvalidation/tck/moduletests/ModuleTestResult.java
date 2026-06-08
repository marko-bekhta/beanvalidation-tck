/*
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.moduletests;

/**
 * Captures the result of a forked JVM module test execution.
 */
public class ModuleTestResult {

	private final int exitCode;
	private final String stdout;
	private final String stderr;
	private final boolean success;
	private final String failureMessage;

	ModuleTestResult(int exitCode, String stdout, String stderr) {
		this.exitCode = exitCode;
		this.stdout = stdout;
		this.stderr = stderr;
		this.success = exitCode == 0 && stdout.contains( ModuleTestRunner.SUCCESS_PREFIX );
		if ( !success ) {
			this.failureMessage = extractFailureMessage( stdout, stderr );
		}
		else {
			this.failureMessage = null;
		}
	}

	/**
	 * Asserts that the forked JVM test succeeded.
	 * Throws {@link AssertionError} with diagnostic output if it failed.
	 */
	public ModuleTestResult assertSuccess() {
		if ( !success ) {
			throw new AssertionError(
					"Module path test failed (exit code " + exitCode + "):\n"
							+ failureMessage + "\n\n--- stdout ---\n" + stdout
							+ "\n--- stderr ---\n" + stderr
			);
		}
		return this;
	}

	/**
	 * Asserts that the forked JVM test failed.
	 * Useful for negative tests (e.g., no opens directive).
	 */
	public ModuleTestResult assertFailure() {
		if ( success ) {
			throw new AssertionError(
					"Module path test was expected to fail but succeeded.\n"
							+ "--- stdout ---\n" + stdout
			);
		}
		return this;
	}

	/**
	 * Asserts failure and that the failure output contains the given exception type.
	 */
	public ModuleTestResult assertFailureContaining(Class<? extends Throwable> exceptionType) {
		assertFailure();
		if ( !stdout.contains( exceptionType.getName() ) && !stderr.contains( exceptionType.getName() ) ) {
			throw new AssertionError(
					"Module path test failed as expected, but output does not contain "
							+ exceptionType.getName() + ".\n--- stdout ---\n" + stdout
							+ "\n--- stderr ---\n" + stderr
			);
		}
		return this;
	}

	public int getExitCode() {
		return exitCode;
	}

	public String getStdout() {
		return stdout;
	}

	public String getStderr() {
		return stderr;
	}

	public boolean isSuccess() {
		return success;
	}

	private static String extractFailureMessage(String stdout, String stderr) {
		for ( String line : stdout.split( "\n" ) ) {
			if ( line.startsWith( ModuleTestRunner.FAILURE_PREFIX ) ) {
				return line.substring( ModuleTestRunner.FAILURE_PREFIX.length() );
			}
		}
		return "No structured failure message. stderr: " + stderr;
	}
}
