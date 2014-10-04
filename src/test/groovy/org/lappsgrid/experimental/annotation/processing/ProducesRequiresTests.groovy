package org.lappsgrid.experimental.annotation.processing

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.lappsgrid.metadata.AnnotationType
import org.lappsgrid.metadata.ContentType
import org.lappsgrid.metadata.ServiceMetadata

import javax.tools.JavaCompiler
import javax.tools.ToolProvider

import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertTrue

/**
 * @author Keith Suderman
 */
@Ignore
class ProducesRequiresTests extends CompilerBase {
    @Test
    void testRequiresProduces() {
        String src = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata(
    requires = "token",
    produces = "sentence"
)
class Empty { }
"""
        compile(src)
        ServiceMetadata metadata = getMetadata()
        assertTrue metadata.requires.annotations.size() == 1
        AnnotationType actual = metadata.requires.annotations[0]
        String required = 'http://vocab.lappsgrid.org/Token'
        assertTrue "Found ${actual} Expected $required",  actual.uri == required
        assertTrue "Found ${metadata.requires.annotations}", metadata.requires.annotations.contains(AnnotationType.TOKEN)
        assertTrue metadata.produces.annotations.size() == 1
        actual = metadata.produces.annotations[0]
        assertTrue "Found ${actual}", metadata.produces.annotations.contains(AnnotationType.SENTENCE)
    }

    @Test
    void testRequiresUri() {
        String src = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata(
    requires = "http://vocab.lappsgrid.org/Token",
    produces = "http://vocab.lappsgrid.org/Sentence"
)
class Empty { }
"""
        compile(src)
        ServiceMetadata metadata = getMetadata()
        assertTrue metadata.requires.annotations.size() == 1
        AnnotationType actual = metadata.requires.annotations[0]
        String required = 'http://vocab.lappsgrid.org/Token'
        assertTrue "Found ${actual} Expected $required",  actual.uri == required
        assertTrue "Found ${metadata.requires.annotations}", metadata.requires.annotations.contains(AnnotationType.TOKEN)
        assertTrue metadata.produces.annotations.size() == 1
        actual = metadata.produces.annotations[0]
        assertTrue "Found ${actual}", metadata.produces.annotations.contains(AnnotationType.SENTENCE)
    }

    @Test
    void testRequiresFormats() {
        String src = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata(
    requires_format = {"text/plain", "application/xml"}
)
class Empty { }
"""
        compile(src)
        ServiceMetadata metadata = getMetadata();
        println metadata.toPrettyJson()
        assertTrue metadata.requires.format.size() == 2
        assertTrue metadata.requires.format[0] == ContentType.TEXT
        assertTrue metadata.requires.format[1].toString(), metadata.requires.format[1] == ContentType.XML
    }

    @Test
    void testRequiresSize() {
        String src = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata(
    requires = {"token", "sentence"}
)
class Empty { }
"""
        compile(src)
        ServiceMetadata metadata = getMetadata();
        println metadata.toPrettyJson()
        assertTrue metadata.requires.annotations.size() == 2
//        assertTrue metadata.requires.format[0] == ContentType.TEXT
//        assertTrue metadata.requires.format[1].toString(), metadata.requires.format[1] == ContentType.XML
    }

    @Test
    void testRequiresContents() {
        String src = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata(
    requires = {"token", "sentence"}
)
class Empty { }
"""
        compile(src)
        ServiceMetadata metadata = getMetadata();
        println metadata.toPrettyJson()
        assertTrue metadata.requires.annotations.size() == 2
        assertTrue metadata.requires.annotations[0] == AnnotationType.TOKEN
        assertTrue metadata.requires.annotations[1].toString(), metadata.requires.annotations[1] == AnnotationType.SENTENCE
    }

}
