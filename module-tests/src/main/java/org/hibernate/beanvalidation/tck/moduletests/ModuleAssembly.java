/*
 * Jakarta Validation TCK
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.beanvalidation.tck.moduletests;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Assembles a synthetic JPMS module directory by copying {@code .class} files
 * and compiling a {@code module-info.java} source string.
 */
class ModuleAssembly {

	static Path assemble(Path basePath, List<Class<?>> classes, String moduleInfoSource,
			List<Path> modulePath) {
		try {
			Files.createDirectories( basePath );

			for ( Class<?> clazz : classes ) {
				copyClassAndInnerClasses( basePath, clazz );
			}

			compileModuleInfo( basePath, moduleInfoSource, modulePath );

			return basePath;
		}
		catch (ModuleAssemblyException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ModuleAssemblyException( "Failed to assemble module at " + basePath, e );
		}
	}

	private static void copyClassAndInnerClasses(Path basePath, Class<?> clazz) throws IOException {
		copyClassFile( basePath, clazz );

		String resourcePath = clazz.getName().replace( '.', '/' ) + ".class";
		String resourceDir = resourcePath.substring( 0, resourcePath.lastIndexOf( '/' ) );
		String simpleNamePrefix = clazz.getSimpleName() + "$";

		ClassLoader cl = clazz.getClassLoader() != null ? clazz.getClassLoader() : ClassLoader.getSystemClassLoader();
		URL dirUrl = cl.getResource( resourceDir );
		if ( dirUrl == null ) {
			return;
		}

		try {
			if ( "file".equals( dirUrl.getProtocol() ) ) {
				copyInnerClassesFromDirectory( basePath, resourceDir, simpleNamePrefix, Path.of( dirUrl.toURI() ) );
			}
			else if ( "jar".equals( dirUrl.getProtocol() ) ) {
				copyInnerClassesFromJar( basePath, resourceDir, simpleNamePrefix, dirUrl );
			}
		}
		catch (URISyntaxException e) {
			throw new IOException( "Invalid URL for resource directory: " + dirUrl, e );
		}
	}

	private static void copyInnerClassesFromDirectory(Path basePath, String resourceDir,
			String simpleNamePrefix, Path dir) throws IOException {
		try ( Stream<Path> stream = Files.list( dir ) ) {
			stream.filter( p -> {
				String name = p.getFileName().toString();
				return name.startsWith( simpleNamePrefix ) && name.endsWith( ".class" );
			} ).forEach( p -> {
				try {
					String innerResourcePath = resourceDir + "/" + p.getFileName();
					Path target = basePath.resolve( innerResourcePath );
					Files.createDirectories( target.getParent() );
					Files.copy( p, target, StandardCopyOption.REPLACE_EXISTING );
				}
				catch (IOException e) {
					throw new RuntimeException( e );
				}
			} );
		}
	}

	private static void copyInnerClassesFromJar(Path basePath, String resourceDir,
			String simpleNamePrefix, URL dirUrl) throws IOException {
		String jarUrlStr = dirUrl.toString();
		// jar:file:/path/to/file.jar!/some/package
		int bangIdx = jarUrlStr.indexOf( '!' );
		if ( bangIdx < 0 ) {
			return;
		}
		String jarFileUri = jarUrlStr.substring( 4, bangIdx );
		try ( FileSystem jarFs = FileSystems.newFileSystem( URI.create( "jar:" + jarFileUri ), java.util.Map.of() ) ) {
			Path packageDir = jarFs.getPath( resourceDir );
			if ( !Files.isDirectory( packageDir ) ) {
				return;
			}
			try ( Stream<Path> stream = Files.list( packageDir ) ) {
				stream.filter( p -> {
					String name = p.getFileName().toString();
					return name.startsWith( simpleNamePrefix ) && name.endsWith( ".class" );
				} ).forEach( p -> {
					try {
						String innerResourcePath = resourceDir + "/" + p.getFileName();
						Path target = basePath.resolve( innerResourcePath );
						Files.createDirectories( target.getParent() );
						Files.copy( p, target, StandardCopyOption.REPLACE_EXISTING );
					}
					catch (IOException e) {
						throw new RuntimeException( e );
					}
				} );
			}
		}
	}

	private static void copyClassFile(Path basePath, Class<?> clazz) throws IOException {
		String resourcePath = clazz.getName().replace( '.', '/' ) + ".class";

		ClassLoader cl = clazz.getClassLoader() != null ? clazz.getClassLoader() : ClassLoader.getSystemClassLoader();
		URL url = cl.getResource( resourcePath );
		if ( url == null ) {
			throw new IOException( "Cannot find .class file for " + clazz.getName()
					+ " on classpath. Resource path: " + resourcePath );
		}

		Path targetFile = basePath.resolve( resourcePath );
		Files.createDirectories( targetFile.getParent() );

		try ( InputStream is = url.openStream() ) {
			Files.copy( is, targetFile, StandardCopyOption.REPLACE_EXISTING );
		}
	}

	private static void compileModuleInfo(Path basePath, String moduleInfoSource,
			List<Path> modulePath) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if ( compiler == null ) {
			throw new ModuleAssemblyException(
					"No system Java compiler available. Ensure the JDK (not JRE) is being used." );
		}

		Path moduleInfoFile;
		try {
			moduleInfoFile = basePath.resolve( "module-info.java" );
			Files.writeString( moduleInfoFile, moduleInfoSource );
		}
		catch (IOException e) {
			throw new ModuleAssemblyException( "Failed to write module-info.java", e );
		}

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		try ( StandardJavaFileManager fileManager = compiler.getStandardFileManager( diagnostics, null, null ) ) {
			Iterable<? extends JavaFileObject> compilationUnits =
					fileManager.getJavaFileObjects( moduleInfoFile.toFile() );

			String modulePathStr = modulePath.stream()
					.map( Path::toString )
					.reduce( (a, b) -> a + System.getProperty( "path.separator" ) + b )
					.orElse( "" );

			List<String> options = new java.util.ArrayList<>();
			if ( !modulePathStr.isEmpty() ) {
				options.add( "--module-path" );
				options.add( modulePathStr );
			}
			options.add( "--patch-module" );
			options.add( extractModuleName( moduleInfoSource ) + "=" + basePath );
			options.add( "-d" );
			options.add( basePath.toString() );

			JavaCompiler.CompilationTask task = compiler.getTask(
					null, fileManager, diagnostics, options, null, compilationUnits );

			boolean compiled = task.call();
			if ( !compiled ) {
				StringBuilder sb = new StringBuilder( "module-info.java compilation failed:\n" );
				sb.append( "Source:\n" ).append( moduleInfoSource ).append( "\n\n" );
				diagnostics.getDiagnostics().forEach( d ->
						sb.append( d.getKind() ).append( ": " ).append( d.getMessage( null ) ).append( "\n" ) );
				throw new ModuleAssemblyException( sb.toString() );
			}

			Files.deleteIfExists( moduleInfoFile );
		}
		catch (ModuleAssemblyException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ModuleAssemblyException( "module-info.java compilation failed", e );
		}
	}

	private static String extractModuleName(String moduleInfoSource) {
		// Simple extraction: find "module <name> {" pattern
		String trimmed = moduleInfoSource.trim();
		int moduleIdx = trimmed.indexOf( "module " );
		if ( moduleIdx < 0 ) {
			throw new ModuleAssemblyException( "Cannot find 'module' keyword in module-info source" );
		}
		String afterModule = trimmed.substring( moduleIdx + 7 ).trim();
		int braceIdx = afterModule.indexOf( '{' );
		if ( braceIdx < 0 ) {
			throw new ModuleAssemblyException( "Cannot find '{' after module name in module-info source" );
		}
		return afterModule.substring( 0, braceIdx ).trim();
	}
}
