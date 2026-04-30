FastCollect is a library of Kotlin collections for holding primitive values (Int, Long, Float, Double, etc...) while
attempting to be as memory and CPU efficient as possible. The library attempts to follow Kotlin API norms and idioms for
dealing with collections as closely as possible.

## Code Generation

This library uses code generation from templates so that it is unnecessary to maintain multiple copies of almost
identical code. The GenerateCollections gradle task is responsible for taking as input the template files within
src/commonMain/templates and generating output into src/commonGenerated. Changes should thus always be made to the
template files rather than the generated output.

## Benchmarking

The benchmark subproject is responsible for running JVM benchmarks using JMH. The full benchmark suite will usually also
run benchmarks on fastutil and JDK collections. For local benchmarking during development it usually wastes less time to
filter benchmarks to only the relevant methods.

Use "./gradlew jmh" to run all JMH benchmarks.
Use "./gradlew jmh -Pjmh.includes='<regex>" to run only benchmarks which match the regular expression given by <regex>.

## JIT-ASM

The jitAsm subproject is responsible for outputting JIT ASM for interesting methods within the library to allow for
detailed analysis of the output byte code and a deeper understanding of performance.

Use "./gradlew jitAsm" or similar to output JVM assembly for analysis.
