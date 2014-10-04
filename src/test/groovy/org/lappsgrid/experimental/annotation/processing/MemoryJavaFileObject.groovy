package org.lappsgrid.experimental.annotation.processing

import javax.lang.model.element.Modifier
import javax.lang.model.element.NestingKind
import javax.tools.JavaFileObject

/**
 * @author Keith Suderman
 */

class MemoryJavaFileObject implements JavaFileObject {
    String name
    String contents;

    public MemoryJavaFileObject(String name, String contents) {
        this.name = name
        this.contents = contents
    }

    @Override
    JavaFileObject.Kind getKind() {
//        println "MemoryJavaFileObject.getKind"
        return JavaFileObject.Kind.SOURCE;
    }

    @Override
    boolean isNameCompatible(String simpleName, JavaFileObject.Kind kind) {
//        println "MemoryJavaFileObject.isNameCompatible ${simpleName}"
        return false
    }

    @Override
    NestingKind getNestingKind() {
//        println "MemoryJavaFileObject.getNestingKind"
        return NestingKind.TOP_LEVEL;
    }

    @Override
    Modifier getAccessLevel() {
//        println "MemoryJavaFileObject.getAccessLevel"
        return Modifier.PUBLIC
    }

    @Override
    URI toUri() {
//        println "MemoryJavaFileObject.toUri"
        return new URI("file:/tmp/test.Empty")
    }

    @Override
    String getName() {
//        println "MemoryJavaFileObject.getName"
        return name
    }

    @Override
    InputStream openInputStream() throws IOException {
//        println "MemoryJavaFileObject.openInputStream"
        return new ByteArrayInputStream(contents.bytes);
    }

    @Override
    OutputStream openOutputStream() throws IOException {
//        println "MemoryJavaFileObject.openOutputStream"
        return null
    }

    @Override
    Reader openReader(boolean ignoreEncodingErrors) throws IOException {
//        println "MemoryJavaFileObject.openReader"
        return new StringReader(contents);
    }

    @Override
    CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
//        println "MemoryJavaFileObject.getCharContent"
        return contents
    }

    @Override
    Writer openWriter() throws IOException {
//        println "MemoryJavaFileObject.openWriter"
        return null
    }

    @Override
    long getLastModified() {
//        println "MemoryJavaFileObject.getLastModified"
        return 0
    }

    @Override
    boolean delete() {
//        println "MemoryJavaFileObject.delete"
        return false
    }
}