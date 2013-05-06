# jvmargs-validator #
## A Java VM command-line arguments validator implemented in Scala ##

The validator exploits Java's `-XX:+PrintFlagsFinal` flag to build a symbol tree of all 700 or so legal `-XX:` parameters and their types, including diagnostic or experimental parameters (if the user unlocks them in the evaluated command line).

The following kinds of parameters are currently supported:

1. Virtually all -XX: parameters
2. -Xmx / -Xms, including sizes (e.g. `-Xmx1g`)
3. -Dfoo=bar -- system properties with or without values.

Usage:

Clone the library, then build and publish it locally:

```bash
git clone https://github.com/hunam/jvmargs-validator.git
cd jvmargs-validator
sbt publish-local
```

In your own build.sbt:

```scala
libraryDependencies += "com.github.hunam"
```

Source usage:

```scala
import com.github.hunam.jvmargs.ArgsValidator

val validations = ArgsValidator("java", "-XX:+UseCompressedOops -Xmx100m -Dfoo.bar -XX:YakShaving=1000 -XX:MaxHeapSize=infinite")
val failures = validations filter (!_.successful)
failures foreach println
```

Prints:

```
[1.5] failure: unknown parameter

-XX:YakShaving=1000
    ^
[1.17] failure: 32-bit unsigned integer expected

-XX:MaxHeapSize=infinite
                ^
```

Known issues: many.

1. The positioning information (e.g., `[1.17]`) stands relative to the beginning of every argument
2. Many `-X` arguments aren't supported
3. `-classpath`, `-version`, and other standard parameters haven't been implemented yet
4. Sometimes, validation failures provide errors that *could* be better

-Nadav.



