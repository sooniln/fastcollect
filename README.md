# FastCollect
A library for high-performance primitive collections in the JVM/Kotlin ecosystem.

Note that contrary to many source control guidelines and common practice, this project checks generated code directly
into the repository. While this is less than ideal in terms of the build pipeline, this project has public APIs composed
of generated code, and it is important for clients that the actual code (rather than just the generation templates) is
viewable, searchable, and parseable within the repository itself.


avoid val m: Map<Int, Int> = Int2IntMap

map keys/value/entries don't support writing

iterator vs fast iterator
