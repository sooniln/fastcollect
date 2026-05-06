FastCollect is a library of Kotlin collections for holding primitive values (Int, Long, Float, Double, etc...) while
attempting to be as memory and CPU efficient as possible. The library attempts to follow Kotlin API norms and idioms for
dealing with collections as closely as possible.

## Code Generation

This library uses code generation from templates so that it is unnecessary to maintain multiple copies of almost
identical code. The GenerateCollections gradle task is responsible for taking as input the template files within
src/commonMain/templates and generating output into src/commonGenerated. Changes should thus always be made to the
template files rather than the generated output.

## Testing

Prefer to run only JVM tests for speed (unless there is a good reason to run tests on other platforms).

* Use "./gradlew jvmTest" to run all tests on the JVM platform.

## Benchmarking

The benchmark subproject is used for general benchmarks across multiple platforms and there is little reason to use it
normally. Local benchmarking for development should use tasks from the jmh subproject which run JVM benchmarks using
JMH. It's also usually more efficient to filter benchmarks to only the relevant classes and/or methods.

* Use "./gradlew jmh" to run all JVM JMH benchmarks.
* Use "./gradlew jmh -PjmhIncludes='<regex>'" to run only JVM JMH benchmarks which match the regular expression given
  by <regex>.

All JMH benchmark results are stored in JSON format in a timestamped file in the jmh/benchmark-results directory.

## JIT-ASM

The jitAsm subproject holds harnesses that can be run to output JIT ASM for interesting methods to allow for detailed
analysis of the assembly code and a deeper understanding of real performance.
