#!/usr/bin/env kotlin -Xjvm-default=all
@file:Repository("https://raw.githubusercontent.com/gilesjb/jam-repo/refs/heads/main")
@file:DependsOn("org.copalis:jam:0.9.1")

interface Fibonacci : Project {

    fun fib(x : Long) : Long = if (x < 2) x else fib(x - 1) + fib(x - 2)

    fun fib5() = fib(5)

    fun fib10() = fib(10)
}

Project.run(Fibonacci::class.java, Fibonacci::fib5, args)
