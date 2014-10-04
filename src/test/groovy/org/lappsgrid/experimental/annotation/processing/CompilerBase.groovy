package org.lappsgrid.experimental.annotation.processing

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.lappsgrid.metadata.DataSourceMetadata
import org.lappsgrid.metadata.ServiceMetadata

import javax.tools.JavaCompiler
import javax.tools.ToolProvider

/**
 * @author Keith Suderman
 */
class CompilerBase {
    protected JavaCompiler compiler

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
        compile(src, "Empty")
    }

    Boolean compile(String src, String className) {
        def units = [ new MemoryJavaFileObject(className, src) ]
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, null, null, units)
        task.processors = [ new MetadataProcessor() ]
        return task.call()
    }


    public ServiceMetadata getMetadata() {
        File file = new File("src/main/resources/metadata/test.Empty.json")
        if (!file.exists()) {
            throw new IOException("Unable to locate metadata file at " + file.path)
        }
        return new ServiceMetadata(file.text)
    }

    public DataSourceMetadata getDataSourceMetadata(String className) {
        File file = new File("src/main/resources/metadata/${className}.json")
        if (!file.exists()) {
            throw new IOException("Unable to locate metadata file at " + file.path)
        }
        return new DataSourceMetadata(file.text)
    }

}
