package org.lappsgrid.experimental.annotation.processing

import org.junit.Ignore
import org.lappsgrid.discriminator.DiscriminatorRegistry
import org.lappsgrid.discriminator.Uri

import static org.junit.Assert.*
import org.junit.Test
import org.lappsgrid.metadata.ServiceMetadata

/**
 * @author Keith Suderman
 */
@Ignore
class LicenseTests extends CompilerBase {

    @Ignore
    void apacheLicenseTests() {
        String source = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata(
    license = "apache2"
)
class Empty { }
"""
        compile(source)
        ServiceMetadata metadata = getMetadata()
        assertNotNull metadata.license
        assertTrue metadata.license == DiscriminatorRegistry.getUri("apache2")
    }

    @Test
    void testInheritedLicenseEmptyNotAnnotated() {
        String source = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata(
    license = "apache2"
)
class Base { }

class Empty extends Base {}
"""
        compile(source)
        ServiceMetadata metadata = getMetadata()
        assertNotNull metadata.license
        assertTrue metadata.license == DiscriminatorRegistry.getUri("apache2")
    }

    @Test
    void testInheritedLicenseEmptyAnnotated() {
        String source = """
package test;
import org.lappsgrid.experimental.annotations.*;
@CommonMetadata(
    vendor = "anc",
    license = "apache2"
)
class Base { }
@ServiceMetadata(requires_format="gate")
class Empty extends Base {}
"""
        compile(source)
        ServiceMetadata metadata = getMetadata()
        assertNotNull metadata.license
        assertTrue metadata.license == DiscriminatorRegistry.getUri("apache2")
    }
}
