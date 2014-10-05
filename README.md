org.lappsgrid.annotation-processor
==================================

Annotation processor used to generate the JSON metadata for LAPPS services.

See the [LAPPS Annotations Project](https://github.com/oanc/org.lappsgrid.annotations)
for information on annotating Java classes with the LAPPS annotations to generate metadata in JSON.

## Maven

```xml
<groupId>org.lappsgrid.experimental</groupId>
<artifactId>annotation-processor</artifactId>
<version>1.0.0-SNAPSHOT</version>
```

## Compiler Configuration

The Java compiler must be configured to invoke the LAPPS annotation 
processor during compilation.

```xml
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessors>
            <annotationProcessor>org.lappsgrid.experimental.annotation.processing.MetadataProcessor</annotationProcessor>
        </annotationProcessors>
    </configuration>
</plugin>
```
