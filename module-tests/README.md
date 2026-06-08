# Jakarta Validation TCK - Module Path Tests

JPMS module path tests for the Jakarta Validation TCK. These tests verify that
validation providers correctly use `ValidationPackageOpener` to support
portable `opens ... to jakarta.validation` directives in user modules.

## What this module does

In a JPMS environment, users should only need to open bean packages to
`jakarta.validation` — not to a specific provider module. The
`ValidationPackageOpener` API enables the validation module to relay those
opens to whichever provider was discovered at bootstrap time.

This test module verifies that providers implement this correctly by:

1. Compiling a synthetic "user app" module (with its own `module-info.java`)
   at test time using `javax.tools.JavaCompiler`
2. Forking a JVM with the module on `--module-path` alongside the API and
   provider JARs
3. Running validation inside the forked JVM and asserting on the result

## Test scenarios

| Test | Module descriptor | Expected outcome |
|------|-------------------|------------------|
| `QualifiedOpensPackageOpenerTest` | `opens pkg to jakarta.validation` | Validation succeeds (provider used the opener) |
| `NoOpensPackageOpenerTest` | No `opens` directive | Validation fails (provider cannot access bean fields) |
| `UnqualifiedOpensPackageOpenerTest` | `opens pkg` (to everyone) | Validation succeeds (baseline, no opener needed) |

## Prerequisites

* JDK 17+ (must be a full JDK, not a JRE — the system Java compiler is required)
* The `jakarta.validation-api` JAR (with `ValidationPackageOpener` support)
* A validation provider JAR and its dependencies

## How to run

### 1. Add the dependency

```xml
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>validation-tck-module-tests</artifactId>
    <version>4.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Configure Surefire

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <suiteXmlFiles>
            <suiteXmlFile>tck-module-tests.xml</suiteXmlFile>
        </suiteXmlFiles>
    </configuration>
</plugin>
```

The suite XML is included in the JAR at the root and is also published as a
separate artifact with classifier `suite`:

```xml
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>validation-tck-module-tests</artifactId>
    <version>4.0.0-SNAPSHOT</version>
    <type>xml</type>
    <classifier>suite</classifier>
</dependency>
```

### 3. Module path configuration

The forked JVM needs the API JAR and provider JARs on its `--module-path`.

**Option A: Automatic (default)**

If no system property is set, the framework uses all entries from
`java.class.path` as the module path. This works when all required JARs
are on the test classpath (the typical Maven Surefire case).

**Option B: Explicit**

Set the `validation.tck.module.path` system property to a path-separator-separated
list of JARs and/or directories:

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <systemPropertyVariables>
            <validation.tck.module.path>
                /path/to/jakarta.validation-api.jar:/path/to/provider.jar:/path/to/provider-deps/*
            </validation.tck.module.path>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

### 4. Run

```shell
mvn test
```

## Automatic modules

If your provider JAR does not have a `module-info.class`, it becomes an
[automatic module](https://docs.oracle.com/javase/specs/jls/se17/html/jls-7.html#jls-7.7.1)
on the module path. The module name is derived from the `Automatic-Module-Name`
manifest entry (if present) or from the JAR filename. This is fully supported —
`ServiceLoader` discovery and `ValidationPackageOpener` work with automatic
modules.

## Writing new test scenarios

Each test follows this pattern:

```java
public class MyNewTest extends AbstractModulePathTCKTest {

    @Test
    public void myScenario() {
        runModuleTest()
                .withModuleInfo("""
                        module test.user.app {
                            requires jakarta.validation;
                            opens %s to jakarta.validation;
                        }
                        """.formatted(MyBean.class.getPackageName()))
                .withClasses(MyBean.class, MyAction.class)
                .execute(MyAction.class)
                .assertSuccess();
    }
}
```

The action class implements `ModuleTestAction` and runs inside the forked JVM:

```java
public class MyAction implements ModuleTestAction {

    @Override
    public void execute() throws Exception {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        // ... validate beans and assert results
    }
}
```

### Assertion methods

- `assertSuccess()` — the action completed without throwing
- `assertFailure()` — the action threw an exception (useful for negative tests)
- `assertFailureContaining(ExceptionType.class)` — failed with a specific exception type

## Troubleshooting

**"No system Java compiler available"** — You are running with a JRE instead
of a JDK. The tests need `javax.tools.JavaCompiler` to compile `module-info.java`
at test time.

**Provider not found in forked JVM** — Make sure the provider JAR is on the
module path (either via classpath fallback or explicit system property) and
that it registers itself via `META-INF/services/jakarta.validation.spi.ValidationProvider`
(or via a `provides` directive in its `module-info.java`).

**"Classpath is empty"** — The `java.class.path` system property is empty and
`validation.tck.module.path` is not set. Set the system property explicitly.
