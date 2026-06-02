FastCollect is a library of Kotlin collections for holding primitive values (Int, Long, Float, Double, etc...) while
attempting to be as memory and CPU efficient as possible. The library attempts to follow Kotlin API norms and idioms for
dealing with collections as closely as possible.

## Code Generation

This library uses code generation from templates so that it is unnecessary to maintain multiple copies of almost
identical code. The GenerateCollections gradle task is responsible for taking as input the template files within
src/commonMain/templates and generating output into src/commonGenerated - it is generally executed automatically as a
dependency of other compile tasks. Changes should thus always be made to the template files rather than the generated
output.

## Testing

Prefer to run only JVM tests for speed (unless there is a good reason to run tests on other platforms).

* Use "./gradlew jvmTest" to run all tests on the JVM platform.

## Benchmarking

Local benchmarking for development should use tasks from the jmh subproject which run JVM benchmarks using JMH. It's
also usually more efficient to filter benchmarks to only the relevant classes and/or methods.

Use the "jmh" task to run all JMH benchmarks. Flags to control JMH:
* -PjmhIncludes='<regex>' to run only benchmarks which match the regular expression
* -P-i=<number> to set the number of iterations
* -P-wi=<number> to set the number of warmup iterations

Previous JMH benchmark results are stored in JSON format in a timestamped file in the jmh/benchmark-results directory.
