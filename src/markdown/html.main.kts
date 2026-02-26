#!/usr/bin/env kotlin -Xjvm-default=all

@file:DependsOn("org.copalis:jam:0.9.1")
@file:DependsOn("org.commonmark:commonmark:0.22.0")

interface MarkdownBuild : FileProject {
    
    fun markdown() = sourceFile("README.md")
    
    fun html() = convertMarkdown("README.html", markdown())
    
    fun convertMarkdown(output: String, input: File): File {
        val parser = org.commonmark.parser.Parser.builder().build()
        val renderer = org.commonmark.renderer.html.HtmlRenderer.builder().build()
        
        return write(output, renderer.render(parser.parse(input.readText())))
    }
}

Project.run(MarkdownBuild::class.java, MarkdownBuild::html, args)
