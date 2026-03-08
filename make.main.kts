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
        exec(fibProj, "./fibonacci.main.kts", "clean")
        val javaProj = ProcessBuilder().directory(File("src/java-project")).inheritIO()
        exec(javaProj, "./make.main.kts", "clean")
        val htmlProj = ProcessBuilder().directory(File("src/markdown")).inheritIO()
        exec(htmlProj, "./html.main.kts", "clean")
        var i = 0

        return """
# Jam build tool

Jam is a JVM build tool which lets you write build scripts in plain Kotlin or Java. 
Build targets are just methods. 
Jam uses a dependency-tracking dynamic proxy to memoize method calls, giving you incremental builds automatically — no explicit dependency declarations required.
Jam supports incremental builds by memoizing (caching) method calls with a dependency-tracking dynamic proxy.

## An example Jam script

This is what a Jam build script for a simple Java project looks like:

```kotlin
${read("java-project/make.main.kts")}
```

Some things to note:

- **The shebang line** — `#!/usr/bin/env -S kotlin -Xjvm-default=all` means the script can be executed directly from the command line like a shell script. The `-Xjvm-default=all` flag is required for Jam's proxy mechanism to work with interface default methods.
- **`@file:DependsOn`** — uses Kotlin's built-in scripting mechanism for declaring dependencies to trigger an automatic download of the Jam library, so no manual installation is needed.
- **`DemoProject`** — the user-defined project, which must be an interface so that Jam can proxy its methods.
- **`JavaProject`** — a built-in Jam interface that provides standard methods for Java builds: `javac`, `junit`, `javadoc`, `jar`, `sourceFiles`, `classpath`, and Maven dependency resolution via `resolve`.
- **`dependencies()`** — a zero-parameter method, which makes it a *target* — something that can be invoked directly from the command line.
- **`testClasses()` being called twice in `tests()`** — this does *not* cause double compilation, because the second call is served from Jam's memoization cache.
- **`build()`** — the default target passed to `Project.run`, but any other target can be invoked by name from the command line.
- **`Project.run`** — this is where the script calls into Jam's build controller, which creates a proxied implementation of the project interface.

The build controller also handles command-line arguments like `--help`

${build(i++, javaProj, "./make.main.kts --help")}

## Try Jam out

If you have Kotlin installed you can easily try out this script.

```kotlin
${read("fibonacci/fibonacci.main.kts")}
```

Save the code to a file called `fibonacci.main.kts` and make it executable with `chmod +x fibonacci.main.kts`.

Then type `./fibonacci.main.kts --targets` to see what build targets it exposes.

${build(i++, fibProj, "./fibonacci.main.kts --targets")}

Note that the `fib` method is *not* listed as a target.
Jam project interfaces can contain methods with parameters, but only methods with 0 parameters are targets.

The default target is `fib10`. Let's run that.

${build(i++, fibProj, "./fibonacci.main.kts")}

The console log shows the method call tree

* `[compute]` means a method was executed
* `[current]` means a cached return value was reused

Because of the method memoization `fib` was only executed 11 times.

Let's see what happens if we run the script again.

${build(i++, fibProj, "./fibonacci.main.kts")}

This time no methods were executed because Jam reused the memoizer cache.
We can examine the contents of the cache by using the `--cache` option.

${build(i++, fibProj, "./fibonacci.main.kts --cache")}

Try the other command-line options,
and also see what happens when you execute the `fib50` target.

## Mutable resources

We've seen how Jam caches return values across runs.
If a return value is a reference to a mutable resource like a file,
Jam can detect if the resource has been modified since the last run and mark the cached value as *stale*.
The next time a build target that depends on that resource is executed, 
Jam will re-execute the functions that depend on it.

This feature gives Jam build scripts the ability to automatically detect when source files have been changed,
and rebuild the artifacts that depend on them.

Let's see it in action. Here's a script that scans for Markdown files in the source directory (`src` by default)
and writes their HTML equivalents to the build directory:

```kotlin
${read("markdown/html.main.kts")}
```

Running a clean build
${build(i++, htmlProj, "./html.main.kts")}

Running the build again
${build(i++, htmlProj, "./html.main.kts")}
As expected, the build target is shown as `[current]`.

Let's simulate modifying one of the source files using the `touch` command
${build(i++, htmlProj, "touch src/ABOUT.md")}

and run the build again
${build(i++, htmlProj, "./html.main.kts")}
This time, the build target is shown as `[refresh]`.

* `[refresh]` means a method was executed because it depended on a resource that was modified since the last run

The log shows that `convertFile` was executed just for the file that was modified.

## More about Jam

If you want to write your own Jam scripts,
check out the [Project JavaDocs](https://gilesjb.github.io/jam/package-summary.html).

## Building Jam

Jam is able to build itself - see [Jam's own build script](src/scripts/JamProject.java),
depending only on JDK version 17 or later.

1. Clone the Jam repo
2. Type `./setup`

The built Jam jar will be in the `build` directory.

To view the JavaDocs type `./make-jam viewDocs`.
"""
    }

    fun readme() {
        clean()
        write("README.md", markdown())
    }
}

Project.run(Docs::class.java, Docs::readme, args)
