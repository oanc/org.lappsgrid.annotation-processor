package org.lappsgrid.experimental.annotation.processing

import org.junit.After
import org.junit.AfterClass
import org.junit.Ignore
import org.lappsgrid.discriminator.Discriminator
import org.lappsgrid.discriminator.DiscriminatorRegistry
import org.lappsgrid.metadata.DataSourceMetadata
import org.lappsgrid.metadata.ServiceMetadata

import static org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
//import org.lappsgrid.metadata.ServiceMetadata

import javax.lang.model.element.Modifier
import javax.lang.model.element.NestingKind
import javax.tools.JavaCompiler
import javax.tools.JavaFileObject
import javax.tools.StandardJavaFileManager
import javax.tools.ToolProvider

/**
 * @author Keith Suderman
 */
@Ignore
class MetadataProcessorTest {

    JavaCompiler compiler

    @BeforeClass
    static void testSetup() {
        File directory = new File("src/main/resources/metadata")
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    @AfterClass
    static void testCleanup() {
        File directory = new File("src/main/resources/metadata")
        if (directory.exists()) {
            directory.delete()
        }
    }

    @Before
    void setup() {
        compiler = ToolProvider.getSystemJavaCompiler()
    }

    @After
    void cleanup() {
        compiler = null
    }

    Boolean compile(String src) {
        def units = [ new MemoryJavaFileObject("Test1", src) ]
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, null, null, units)
        task.processors = [ new MetadataProcessor() ]
        return task.call()
    }

    public ServiceMetadata getMetadata() {
        return getMetadata(new ServiceMetadataFactory())
    }

    public <T> T getMetadata(Factory<T> factory) {
        return getMetadata("test.Empty", factory)
    }

//    ServiceMetadata getMetadata(String className) {
//        File file = new File("src/main/resources/metadata/${className}.json")
//        if (!file.exists()) {
//            throw new IOException("Unable to locate metadata file at " + file.path)
//        }
//        ServiceMetadata metadata = new ServiceMetadata(file.text)
//        file.delete()
//        return metadata
//    }

    public <T> T getMetadata(String className, Factory<T> factory) {
        File file = new File("src/main/resources/metadata/${className}.json")
        if (!file.exists()) {
            throw new IOException("Unable to locate metadata file at " + file.path)
        }
        T metadata = factory.create(file.text)
//        file.delete()
        return metadata
    }

    @Test
    void testDefaults() {
        String src = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata class Empty {}
"""
        Discriminator any = DiscriminatorRegistry.getByName("any")
        compile(src)
        def factory = new ServiceMetadataFactory()
        ServiceMetadata metadata = getMetadata("test.Empty", factory)
        assertTrue "Expected ${any.uri} Found ${metadata.allow}", any.uri == metadata.allow
        assertTrue "test.Empty" == metadata.name

        XmlParser parser = new XmlParser()
        def project = parser.parse(new File("pom.xml"))
        assertTrue project.version.text() == metadata.version
        assertNull metadata.vendor
    }

    @Test
    void testDataSourceMetadata() {
        String src = """
package test;
import org.lappsgrid.experimental.annotations.DataSourceMetadata;
@DataSourceMetadata(
vendor="anc",
language="en"
)
class Source {}
"""
        compile(src)
        DataSourceMetadata metadata = getMetadata('test.Source', new DataSourceMetadataFactory())
        assertTrue metadata.language[0] == 'en'
        assertTrue metadata.vendor == 'anc'
    }

    @Test
    void testVendor() {
        String src = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata(vendor="anc") class Empty { };
"""
        compile(src)
        ServiceMetadata metadata = getMetadata()
        assertTrue("Expected anc Found ${metadata.vendor}", "anc" == metadata.vendor)
    }

    @Test
    void testEncoding() {
        String expected = "us-ascii"
        String src = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata(encoding="${expected}") class Empty { };
"""
        compile(src)
        ServiceMetadata metadata = getMetadata()
        assertTrue metadata.requires.encoding == expected
        assertTrue metadata.produces.encoding == expected
    }

    @Test
    void testProducesRequiresEncoding() {
        String src = """
package test;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
@ServiceMetadata(
    requires_encoding = "requires",
    produces_encoding = "produces"
)
class Empty { }
    """
        compile(src)
        ServiceMetadata metadata = getMetadata()
        assertTrue metadata.requires.encoding == "requires"
        assertTrue metadata.produces.encoding == "produces"
    }

    @Test
    void testApacheLicense() {
        String src = """
package test;
@org.lappsgrid.experimental.annotations.ServiceMetadata(
    license = "apache2"
)
class Empty {}
"""
        compile(src)
        ServiceMetadata metadata = getMetadata()
        assertTrue "Invalid license " + metadata.license, metadata.license == "http://vocab.lappsgrid.org/ns/license#apache-2.0"
    }

    @Test
    void testHttpLicense() {
        String src = """
package test;
@org.lappsgrid.experimental.annotations.ServiceMetadata(
    license = "http://anc.org"
)
class Empty {}
"""
        compile(src)
        ServiceMetadata metadata = getMetadata()
        assertTrue "Invalid license " + metadata.license, metadata.license == "http://anc.org"
    }

    @Test
    void testUnknownLicense() {
        String src = """
package test;
@org.lappsgrid.experimental.annotations.ServiceMetadata(
    license = "xxxx"
)
class Empty {}
"""
        compile(src)
        ServiceMetadata metadata = getMetadata(new ServiceMetadataFactory())
        assertTrue "Invalid license " + metadata.license, metadata.license == "xxxx"
    }
}

interface Factory<T> {
    T create(String text)
}

class ServiceMetadataFactory implements Factory<ServiceMetadata> {
    ServiceMetadata create(String json) {
        return new ServiceMetadata(json)
    }
}

class DataSourceMetadataFactory implements Factory<org.lappsgrid.metadata.DataSourceMetadata> {
    org.lappsgrid.metadata.DataSourceMetadata create(String json) {
        return new DataSourceMetadata(json)
    }
}