package org.lappsgrid.experimental.annotation.processing

import org.junit.Ignore
import org.junit.Test
import org.lappsgrid.metadata.ContentType
import org.lappsgrid.metadata.ServiceMetadata

import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertTrue

/**
 * @author Keith Suderman
 */
@Ignore
class FormatTests extends CompilerBase {

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

    //"application/xml; profile=http://gate.ac.uk"
    @Test
    void testGateFormat() {
        String string = "application/xml; profile=http://gate.ac.uk"
        String src = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata(
    requires_format = "$string"
)
class Empty { }
"""
        ContentType expected = new ContentType(string)
        compile(src)
        ServiceMetadata metadata = getMetadata();
//        println metadata.toPrettyJson()
        assertTrue metadata.requires.format.size() == 1
        assertTrue metadata.requires.format[0] == expected
    }
    @Test
    void testGateFormatArray() {
        String gate = "application/xml; profile=http://gate.ac.uk"
        String graf = "application/xml; profile=http://graf.tc37sc4.org"
        String src = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata(
    requires_format = {"$gate", "$graf"}
)
class Empty { }
"""
        ContentType expectedGate = new ContentType(gate)
        ContentType expectedGraf = new ContentType(graf)
        compile(src)
        ServiceMetadata metadata = getMetadata();
        println metadata.toPrettyJson()
        assertTrue metadata.requires.format.size() == 2
        assertTrue metadata.requires.format[0] == expectedGate
        assertTrue metadata.requires.format[1] == expectedGraf
    }
}
