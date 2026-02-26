#!/usr/bin/env -S kotlin -Xjvm-default=all

@file:DependsOn("org.copalis:jam:0.9.1")

interface Fibonacci : Project {

    fun fib(x : Long) : Long = if (x < 2) x else fib(x - 1) + fib(x - 2)

    fun fib10() = fib(10)
    fun fib50() = fib(50)
}

Project.run(Fibonacci::class.java, Fibonacci::fib10, args)
