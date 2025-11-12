#!/usr/bin/env kotlin -Xjvm-default=all
@file:Repository("https://raw.githubusercontent.com/gilesjb/jam-repo/refs/heads/main")
@file:DependsOn("org.copalis:jam:0.9.1")

interface HelloWorld : JavaProject {

    fun worldStr() = "World"

    fun worldName() = read("world.txt")

    fun greet(place : String) = println("Hello, ${place}!")

    fun helloJava() = sourceFiles("HelloWorld.java")

    fun helloClasses() = classpath(javac("classes", helloJava()))

    fun runHello() {
        java("-cp", helloClasses(), "HelloWorld")
    }

    fun printHellos() {
        greet(worldStr())
        greet(worldName())
        runHello()
    }
}

Project.run(HelloWorld::class.java, HelloWorld::printHellos, args)
