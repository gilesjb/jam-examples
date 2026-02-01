#!/usr/bin/env kotlin -Xjvm-default=all -cp ../jam/build/jam-0.9.1.jar

// Uses https://github.com/homeport/termshot

interface Docs : FileProject {

    fun absPath(path : String) = java.nio.file.Path.of(buildPath(path)).toAbsolutePath()

    fun capture(idx : Int, pb : ProcessBuilder, cmd : String) : String {
        val tmp = String.format("%s/%02d.png", absPath("tmp"), idx)
        val path = String.format("%s/%02d.png", absPath("docs/pics"), idx)
        exec(pb, "termshot", "-C", "90", "-cs", "--no-decoration", "--no-shadow", "-f", tmp, "--", cmd)
        exec(pb, "magick", tmp, "-strip", "-colors", "16", "+dither", "-alpha", "Off", path)
        return String.format("<img src='docs/pics/%02d.png' width='1000'>", idx)
    }

    fun build(idx : Int, pb : ProcessBuilder, cmd : String) = capture(idx, pb, cmd)

    fun touch(idx : Int, pb : ProcessBuilder, src : String) = capture(idx, pb, "touch src/${src}")

    fun markdown() : String {
        val fibProj = ProcessBuilder().directory(File("src/fibonacci")).inheritIO()
        val helloProj = ProcessBuilder().directory(File("src/hello-world")).inheritIO()
        exec(fibProj, "./fibonacci.main.kts", "clean")
        exec(helloProj, "./hello.main.kts", "clean")
        var i = 0

        return """
# Jam build tool

**Jam** is a build automation library that allows you to write build scripts in plain Kotlin or Java.

## An introduction to Jam

Prerequisites: Kotlin installed.

Create a file called `fibonacci.main.kts` and paste the following code into it:

```kotlin
${read("fibonacci/fibonacci.main.kts")}
```

and make it executable with `chmod +x fibonacci.main.kts`

Now run it:
${build(i++, fibProj, "./fibonacci.main.kts")}

What happened here?

* The first line of console output shows the `fib10()` method being executed. This is because the script specified `Fibonacci::fib10` as the default target method.
* Following lines of console output show all the other Project method calls that were made: `fib 10` means `fib(10)` was called.
* The return value of the target method is displayed as `Result: 55`.

### Memoization

The script uses a recursive implementation of the Fibonacci sequence. 
Normally, A call to `fib(10)` would result in a total of 177 recursive method calls,
but the console log shows far fewer method calls being made.
The discrepancy is because Jam *memoizes* Project method calls.
saving return values for later use.

* `[compute]` means a method was executed
* `[current]` means a cached return value was reused

The recursive calls are shown in a tree-like trace: `fib(10)` → `fib(9)` → `fib(8)` → ... down to `fib(0)` and `fib(1)`.
Then, on the way back up, it says `[current]` — this means Jam is reusing previously computed results.

Run the script again:
${build(i++, fibProj, "./fibonacci.main.kts")}

This time no methods were executed! The results was fetched straight from cache.
Speaking of the cache, it can be viewed by running the script with the `--cache` option.

${build(i++, fibProj, "./fibonacci.main.kts --cache")}

## Command line options

Here is the full set of command line options:
${build(i++, fibProj, "./fibonacci.main.kts --help")}

The most useful option is `--targets`.
${build(i++, fibProj, "./fibonacci.main.kts --targets")}

The `Fibonacci` project interface defines one target: `fib10`.
It also inherits a `clean` target from the `Project` interface it extends.
You can probably guess what the `clean` target does.

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
${read("hello-world/hello.main.kts")}
```

The script can be run directly from the command line.
It just requires Kotlin to be installed; the Jam library will be downloaded automatically.

Passing the `--help` option displays options:
${build(i++, helloProj, "./hello.main.kts --help")}

### Build targets

Specifying `--targets` shows the build targets
${build(i++, helloProj, "./hello.main.kts --targets")}
Targets are just project methods that have 0 arguments.
The target listing shows method defined by the script's `HelloWorld` interface and inherited from its parent interfaces.

Let's run the `worldStr` target:
${build(i++, helloProj, "./hello.main.kts worldStr")}
The output log shows that `worldStr()` was executed and returned the value "World".
 
Another target is `worldName`
${build(i++, helloProj, "./hello.main.kts worldName")}
Now the output log shows that `worldName()` was executed, and it in turn called `read("world.text")`

### Result caching

If we look at the targets again we can see that both `worldStr` and `worldName` targets are tagged as **fresh**.
This means that their results are cached and up to date.
${build(i++, helloProj, "./hello.main.kts --targets")}

The cache contents can be viewed with the `--cache` option.
${build(i++, helloProj, "./hello.main.kts --cache")}
(Notice that the results cache is stored in a hidden file, and its name is derived from the project name.)

Because its result is cached, if we run `worldName` again the result will be fetched from cache.
${build(i++, helloProj, "./hello.main.kts worldName")}

### Dependency tracking

Jam is able to infer that the result of `worldName` depends on the contents of the file `src/world.txt`.
This means that if the last-modified time of that file changes, the cached result will be invalidated.

${touch(i++, helloProj, "world.txt")}

Now the `worldName` target is shown as stale.
${build(i++, helloProj, "./hello.main.kts --targets")}

Executing the target again it will be rebuilt.
${build(i++, helloProj, "./hello.main.kts worldName")}

### Compiling code

The `JavaProject` interface provides a variety of methods for building and executing Java code.
This is demonstrated by the `runHello` target:
${build(i++, helloProj, "./hello.main.kts runHello")}

Jam stores references to the compiled classes.
${build(i++, helloProj, "./hello.main.kts runHello")}

But if there are modifications to source files,
${touch(i++, helloProj, "HelloWorld.java")}

then Jam will recompile the classes.
${build(i++, helloProj, "./hello.main.kts runHello")}

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
        clean()
        write("README.md", markdown())
    }
}

Project.run(Docs::class.java, Docs::readme, args)
