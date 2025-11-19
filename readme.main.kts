#!/usr/bin/env kotlin -Xjvm-default=all
@file:Repository("https://raw.githubusercontent.com/gilesjb/jam-repo/refs/heads/main")
@file:DependsOn("org.copalis:jam:0.9.1")

// Uses https://github.com/homeport/termshot

interface Docs : FileProject {

    override fun buildPath() = "docs"

    fun build(idx : Int, opts : String) : String {
        val path = String.format("%s/%02d.png", buildPath("pics"), idx)
        exec("termshot", "-c", "--no-decoration", "--no-shadow", "-f", path, "--", "./hello.main.kts " + opts)
        return "![Title]($path)"
    }

    fun touch(idx : Int, src : String) : String {
        val file = sourceFile(src).toString()
        exec("touch", file)
        return "touch $file"
    }

    fun markdown() : String {
        exec("./hello.main.kts", "clean")
        var i = 0

        return """
# Jam build tool

**Jam** is a library for writing command-line scripts in Java or Kotlin,
especially script for build automation.

### Jam scripts

Here is an example build script. The file name is `hello.main.kts`.

```kotlin
${read("../hello.main.kts")}
```

The script can be run directly from the command line, for example with the `--help` option:
${build(i++, "--help")}

### Build targets

Specifying `--targets` shows
${build(i++, "--targets")}
         
Targets are just project methods that have 0 arguments, for example `worldStr()`.
${build(i++, "worldStr")}
The output log shows that the method returned the value "World".
 
Another target method is `worldName()`.
${build(i++, "worldName")}

### Result caching

If we look at the targets again we can see that both `worldStr` and `worldName` targets are listed as **fresh**.
This means that their results are cached and up to date.
${build(i++, "--targets")}

Because its result is cached, if we run `worldName` again the result will be fetched from cache.
${build(i++, "worldName")}

### Dependency tracking

Jam is able to infer that the result of `worldName` depends on the contents of the file `src/world.txt`.
This means that if the last-modified time of that file changes, the cached result will be invalidated.

```
${touch(i++, "world.txt")}
```

Now the `worldName` target is shown as stale.
${build(i++, "--targets")}

Executing the target again, it is rebuilt.
${build(i++, "worldName")}

### Compiling code

${build(i++, "runHello")}

Similarly to the previous example, the compiled classes are cached
${build(i++, "runHello")}

```
${touch(i++, "HelloWorld.java")}
```

${build(i++, "runHello")}
"""
    }

    fun readme() {
        write("../README.md", markdown())
    }
}

Project.run(Docs::class.java, Docs::readme, args)
