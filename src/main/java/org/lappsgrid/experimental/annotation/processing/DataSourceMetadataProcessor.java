package org.lappsgrid.experimental.annotation.processing;

import org.anc.io.UTF8Writer;
import org.lappsgrid.discriminator.Discriminator;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.experimental.annotations.DataSourceMetadata;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.serialization.Serializer;
import org.xml.sax.InputSource;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Keith Suderman
 */
@SupportedAnnotationTypes("org.lappsgrid.experimental.annotations.DataSourceMetadata")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class DataSourceMetadataProcessor //extends AbstractProcessor
{
//   private Properties defaults = new Properties();

	public DataSourceMetadataProcessor()
	{
		System.out.println("Creating DataSourceMetadataProcessor");
	}

	private void log(String message)
	{
//		System.out.println(message);
//		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
	}

	private void debug(String message)
	{
//      System.out.println(message);
	}

	private String toString(String[] list)
	{
		if (list.length == 0)
		{
			return "[]";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("[ ");
		int i = 0;
		if (i < list.length)
		{
			buffer.append(list[i++]);
		}
		while (i < list.length)
		{
			buffer.append(", ");
			buffer.append(list[i++]);
		}
		buffer.append("]");
		return buffer.toString();
	}

	public boolean processDataSourceMetadata(File root, Set<? extends TypeElement> annotations,
								  RoundEnvironment roundEnv) {
		log("Running the DataSourceMetadataProcessor");
		for (Element elem : roundEnv.getElementsAnnotatedWith(ServiceMetadata.class))
		{
			if (elem.getKind() != ElementKind.CLASS || elem.getModifiers().contains(Modifier.ABSTRACT)) {
				// We are only interested in concrete classes. The Metadata annotation
				// can only be applied to Types (classes), but we only generate the
				// metadata file for non-abstract classes.
				debug("Skipping " + elem.getSimpleName());
				continue;
			}
			TypeElement type = (TypeElement) elem;
			String className = type.getQualifiedName().toString();
			TypeMirror parent = type.getSuperclass();
			DataSourceMetadata metadata = parent.getClass().getAnnotation(DataSourceMetadata.class);
			File file = new File(root, className + ".json");
			debug("Generating ServiceMetadata for " + className);
			try
			{
				writeDataSourceMetadata(file, className, metadata);
			}
			catch (IOException e)
			{
				log(e.getMessage());
				e.printStackTrace();
			}
		}
		return true;
	}

//	private ContentType getContentType(String name)
//	{
//		Discriminator d = DiscriminatorRegistry.getByName(name);
//		if (d == null)
//		{
//			return null;
//		}
//		return new ContentType(d.getUri());
//	}

	private String get(String string)
	{
		if (string.length() == 0)
		{
			return null;
		}
		return string;
	}

	private String getValue(String key)
	{
		if (key.length() == 0)
		{
			return null;
		}
		if (key.startsWith("http"))
		{
			return key;
		}
		Discriminator discriminator = DiscriminatorRegistry.getByName(key);
		if (discriminator != null)
		{
			return discriminator.getUri();
		}

		return key;
	}

	/**
	 * Attempts to find the version number.  If a version number
	 * was supplied in the annotation that value is used. Otherwise the
	 * processor looks for a file named VERSION in the project root directory.
	 * If a VERSION file can not be found, or can not be parsed, the processor
	 * attempts to parse the version from the pom file.
	 * <p>
	 * Returns <code>null</code> if the version number can not be
	 * determined.
	 *
	 * @param version The version specified in the annotation or an empty
	 *                string if the version was not specified in the
	 *                annotation.
	 * @return The version number if it can be determined, null otherwise.
	 */
	private String getVersion(String version)
	{
		if (version.length() > 0)
		{
			debug("Using version specified in the annotation: " + version);
			return version;
		}
		File file = new File("VERSION");
		if (file.exists())
		{
			debug("Attempting to parse VERSION file");
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = reader.readLine();
				reader.close();
				debug("Using version from VERSION file: " + line);
				return line;
			}
			catch (IOException ignore)
			{
				// Fall through and try the pom.xml file.
			}
		}
		debug("Attempting to get version from POM.");
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new MavenNamespaceContext());
		String expression = "/maven:project/maven:version/text()";
		String result = null;
		try
		{
			XPathExpression xpr = xpath.compile(expression);
			InputSource source = new InputSource(new FileReader("pom.xml"));
			String s = xpr.evaluate(source);
			debug("Version is " + s);
			return s;
		}
		catch (IOException e)
		{
			log("IOException: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (XPathExpressionException ignored)
		{
			log("Error parsing version: " + ignored.getMessage());
			ignored.printStackTrace();
			return null;
		}
	}

	private void writeDataSourceMetadata(File file, String className, DataSourceMetadata annotation) throws IOException
	{
		org.lappsgrid.metadata.DataSourceMetadata metadata = new org.lappsgrid.metadata.DataSourceMetadata();
		metadata.setName(className);
		metadata.setDescription(get(annotation.description()));
		metadata.setVendor(get(annotation.vendor()));
		metadata.setLicense(getValue(annotation.license()));
		metadata.setAllow(getValue(annotation.allow()));
//      log("Attempting to get version");
		metadata.setVersion(getVersion(annotation.version()));
		metadata.setEncoding(annotation.encoding());
		metadata.setLanguage(Arrays.asList(annotation.language()));
		metadata.setFormat(Arrays.asList(annotation.format()));

		UTF8Writer writer = null;
		try
		{
			writer = new UTF8Writer(file);
			writer.write(Serializer.toPrettyJson(metadata));
			log("Wrote " + file.getPath());
		}
		finally
		{
			if (writer != null)
			{
				writer.close();
			}
		}
	}

	class MavenNamespaceContext implements NamespaceContext
	{

		@Override
		public String getNamespaceURI(String prefix)
		{
			debug("Getting namespace for prefix " + prefix);
			if ("maven".equals(prefix))
			{
				debug("Return MAVEN URL.");
				return "http://maven.apache.org/POM/4.0.0";
			}
			return null;
		}

		@Override
		public String getPrefix(String namespaceURI)
		{
			return null;
		}

		@Override
		public Iterator getPrefixes(String namespaceURI)
		{
			return null;
		}
	}
}
