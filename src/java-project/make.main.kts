#!/usr/bin/env -S kotlin -Xjvm-default=all

@file:DependsOn("org.copalis:jam:0.9.1")

interface DemoProject : JavaProject {

    fun dependencies() = resolve("com.google.code.gson:gson:2.10.1")

    fun mainSources() = sourceFiles("main/**.java")

    fun mainClasses() = javac("classes/main", mainSources(), "-cp", classpath(dependencies()))

    fun testSources() = sourceFiles("test/**.java")

    fun testClasses() = javac("classes/test", testSources(),
            "-cp", classpath(mainClasses(), jUnitLib(), dependencies()))

    fun tests() = junit("test/report",
            "--scan-classpath", classpath(testClasses()),
            "-cp", classpath(testClasses(), testSources(), mainClasses(), dependencies()))

    fun docs() = javadoc("docs", "-Xdoclint:none",
            "-sourcepath", classpath(mainSources()),
            "-cp", classpath(dependencies()),
            "-subpackages", "org.copalis")

    fun jarfile() = jar("jam-demo.jar", mainClasses())

    fun build() : File {
        tests()
        docs()
        return jarfile()
    }
}

Project.run(DemoProject::class.java, DemoProject::build, args)
