package org.lappsgrid.experimental.annotation.processing

import org.junit.Ignore
import org.junit.Test
import org.lappsgrid.metadata.AnnotationType

import static org.junit.Assert.*

import org.lappsgrid.metadata.ContentType
import org.lappsgrid.metadata.ServiceMetadata

/**
 * @author Keith Suderman
 */
@Ignore
class InheritanceTests extends CompilerBase {
    @Test
    void testBaseProducesGate() {
        String src = """
package test;
import org.lappsgrid.experimental.annotations.*;
@CommonMetadata(produces_format="gate")
class Base {}

class Empty extends Base {}
"""
        compile src
        ServiceMetadata metadata = getMetadata()
        assertTrue metadata.produces.format.contains(ContentType.GATE)
    }

    @Test
    void testBothProduce() {
        String src = """
package test;
import org.lappsgrid.experimental.annotations.*;
@CommonMetadata(produces_format="gate")
class Base {}

@ServiceMetadata(produces="token")
class Empty extends Base {}
"""
        compile src
        ServiceMetadata metadata = getMetadata()
        assertTrue metadata.produces.format.contains(ContentType.GATE)
        assertTrue metadata.produces.annotations.contains(AnnotationType.TOKEN)
    }}
