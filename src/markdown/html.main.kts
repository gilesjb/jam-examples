#!/usr/bin/env kotlin -Xjvm-default=all

@file:DependsOn("org.copalis:jam:0.9.1")
@file:DependsOn("org.commonmark:commonmark:0.22.0")

interface MarkdownBuild : FileProject {
    
    fun markdownFiles() = sourceFiles("*.md")
    
    fun convertFile(input: File): File {
        val parser = org.commonmark.parser.Parser.builder().build()
        val renderer = org.commonmark.renderer.html.HtmlRenderer.builder().build()
        
        val dest = input.relativeTo(File(sourcePath())).path.replace(".md", ".html")
        return write(dest, renderer.render(parser.parse(input.readText())))
    }

    fun htmlFiles() = markdownFiles().map { convertFile(it) }
}

Project.run(MarkdownBuild::class.java, MarkdownBuild::htmlFiles, args)
