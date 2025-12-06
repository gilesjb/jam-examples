#!/usr/bin/env kotlin -Xjvm-default=all
@file:Repository("https://raw.githubusercontent.com/gilesjb/jam-repo/refs/heads/main")
@file:DependsOn("org.copalis:jam:0.9.1")

// Uses https://github.com/homeport/termshot

interface Docs : FileProject {

    override fun buildPath() = "docs"

    fun capture(idx : Int, cmd : String) : String {
        val tmp = String.format("%s/%02d.png", buildPath("tmp"), idx)
        val path = String.format("%s/%02d.png", buildPath("pics"), idx)
        exec("termshot", "-C", "90", "-cs", "--no-decoration", "--no-shadow", "-f", tmp, "--", cmd)
        exec("magick", tmp, "-strip", "-colors", "16", "+dither", "-alpha", "off", path)
        return "<img src='$path' width='1000'>"
    }

    fun build(idx : Int, opts : String) = capture(idx, "./hello.main.kts " + opts)

    fun touch(idx : Int, src : String) = capture(idx, "touch src/${src}")

    fun markdown() : String {
        exec("./hello.main.kts", "clean")
        var i = 0

        return """
# Jam build tool

**Jam** is a build automation library.
It lets you write build scripts in plain Kotlin or Java.

* Build *targets* are just methods/functions
* *Dependencies* between targets are inferred by Jam's dynamic method proxy, which monitors method parameters and return values for references to source files.

There are 3 parts to Jam:

1. A build controller that handles command-line arguments
2. A dynamic method proxy that memoizes result values and tracks dependencies
3. A library of predefined build targets and utility functions for compiling code

## How does it work?

Jam's memoizer intercepts method calls and caches return values, including references to build artifacts. 
Later method calls with the same parameters are served from cache instead of the method being executed again.
The cache is also persisted to disk so that Jam can remember the project state between builds.
Jam also records methods' dependencies on external mutable resources like source files;
If those resources change, Jam knows that build artifacts derived from them are stale and marks cache entries referring to them as stale.

## Jam in action

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
The target listing shows method defined by the script's `HelloWorld` interface and inherited from its parent interfaces.

Let's run the `worldStr` target:
${build(i++, "worldStr")}
The output log shows that `worldStr()` was executed and returned the value "World".
 
Another target is `worldName`
${build(i++, "worldName")}
Now the output log shows that `worldName()` was executed, and it in turn called `read("world.text")`

### Result caching

If we look at the targets again we can see that both `worldStr` and `worldName` targets are tagged as **fresh**.
This means that their results are cached and up to date.
${build(i++, "--targets")}

The cache contents can be viewed with the `--cache` option.
${build(i++, "--cache")}
(Notice that the results cache is stored in a hidden file, and its name is derived from the project name.)

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

## Status

Jam is a work in progress but its Project libraries provide all the functions you need to

* Compile Java code
* Download Maven packages
* Run unit tests
* Generate JavaDoc

In fact, Jam is able to build itself - see [Jam's own build script](src/scripts/JamProject.java).

## Building Jam

The only build dependency of Jam is a JDK version 17 or later.

1. Clone the Jam repo
2. Type `./setup`

The `setup` shell script creates a copy of the build script called `make-jam` which it uses to build the project.

To view the JavaDocs type `./make-jam viewDocs`.

"""
    }

    fun readme() {
        write("../README.md", markdown())
    }
}

Project.run(Docs::class.java, Docs::readme, args)
