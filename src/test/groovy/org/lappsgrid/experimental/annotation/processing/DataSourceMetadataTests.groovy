package org.lappsgrid.experimental.annotation.processing

import org.junit.Ignore
import org.junit.Test
import org.lappsgrid.metadata.ContentType

import static org.junit.Assert.*
import org.lappsgrid.metadata.DataSourceMetadata

/**
 * @author Keith Suderman
 */
@Ignore
class DataSourceMetadataTests extends CompilerBase {

    @Test
    void formatTest() {
        String src = """
package test;
import org.lappsgrid.experimental.annotations.*;
@DataSourceMetadata(
    format = "lapps"
)
class Source {}
"""
        compile(src, 'Source')
        DataSourceMetadata metadata = getDataSourceMetadata("test.Source")
        int expected = 1
        int actual = metadata.format.size()
        assertTrue "Expected $expected Actual $actual", expected == actual
        assertTrue ContentType.LAPPS == new ContentType(metadata.format[0])
    }

}
