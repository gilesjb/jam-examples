#!/usr/bin/env kotlin -Xjvm-default=all
@file:Repository("https://raw.githubusercontent.com/gilesjb/jam-repo/refs/heads/main")
@file:DependsOn("org.copalis:jam:0.9.1")

interface HelloWorld : JavaProject {
    fun hello() = "Hello, World!"

    fun printHello() {
        println(hello())
    }

    fun helloFile() = read("hello.txt")

    fun printFile() {
        println(helloFile())
    }

    fun helloClasses() = javac("classes", sourceFiles("HelloWorld.java"))

    fun runHello() {
        java("-cp", classpath(helloClasses()), "HelloWorld")
    }

    fun printHellos() {
        printHello()
        printFile()
        runHello()
    }
}

Project.run(HelloWorld::class.java, HelloWorld::printHellos, args)
