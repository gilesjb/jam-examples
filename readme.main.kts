#!/usr/bin/env kotlin -Xjvm-default=all
@file:Repository("https://raw.githubusercontent.com/gilesjb/jam-repo/refs/heads/main")
@file:DependsOn("org.copalis:jam:0.9.1")

// Uses https://github.com/homeport/termshot

interface Docs : FileProject {

    override fun buildPath() = "docs"

    fun capture(idx : Int, cmd : String) : String {
        val path = String.format("%s/%02d.png", buildPath("pics"), idx)
        exec("termshot", "-C", "90", "-cs", "--no-decoration", "--no-shadow", "-f", path, "--", cmd)
        return "<img src='$path' width='1000'>"
    }

    fun build(idx : Int, opts : String) = capture(idx, "./hello.main.kts " + opts)

    fun touch(idx : Int, src : String) = capture(idx, "touch ${sourceFile(src)}")

    fun markdown() : String {
        exec("./hello.main.kts", "clean")
        var i = 0

        return """
# Jam build tool

**Jam** is a library for writing command-line scripts in Java or Kotlin,
especially script for build automation.

### Jam scripts

Here is an example build script written in Kotlin. Kotlin scripts must have names that end with `.main.kts`.
This file is called `hello.main.kts`.

```kotlin
${read("../hello.main.kts")}
```

The script can be run directly from the command line.
It just requires Kotlin to be installed; the Jam library will be downloaded automatically.

Passing the `--help` option displays options:
${build(i++, "--help")}

### Build targets

Specifying `--targets` shows the build targets
${build(i++, "--targets")}
Targets are just project methods that have 0 arguments.
The target listing shows method defined by the script's `HelloWorld` interface and inherited from parent interfaces.

Let's run the `worldStr` method:
${build(i++, "worldStr")}
The output log shows that the method returned the value "World".
 
Another target method is `worldName`, which reads text from a file:
${build(i++, "worldName")}

### Result caching

If we look at the targets again we can see that both `worldStr` and `worldName` targets are tagged as **fresh**.
This means that their results are cached and up to date.
${build(i++, "--targets")}

Because its result is cached, if we run `worldName` again the result will be fetched from cache.
${build(i++, "worldName")}

### Dependency tracking

Jam is able to infer that the result of `worldName` depends on the contents of the file `src/world.txt`.
This means that if the last-modified time of that file changes, the cached result will be invalidated.

${touch(i++, "world.txt")}

Now the `worldName` target is shown as stale.
${build(i++, "--targets")}

Executing the target again it will be rebuilt.
${build(i++, "worldName")}

### Compiling code

The `JavaProject` interface provides a variety of methods for building and executing Java code.
This is demonstrated by the `runHello` target:
${build(i++, "runHello")}

Jam stores references to the compiled classes.
${build(i++, "runHello")}

But if there are modifications to source files,
${touch(i++, "HelloWorld.java")}

then Jam will recompile the classes.
${build(i++, "runHello")}
"""
    }

    fun readme() {
        write("../README.md", markdown())
    }
}

Project.run(Docs::class.java, Docs::readme, args)
