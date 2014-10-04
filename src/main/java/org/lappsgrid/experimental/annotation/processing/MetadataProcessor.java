package org.lappsgrid.experimental.annotation.processing;

import org.anc.io.UTF8Writer;
import org.lappsgrid.discriminator.Discriminator;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.experimental.annotations.CommonMetadata;
import org.lappsgrid.experimental.annotations.DataSourceMetadata;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
import org.lappsgrid.metadata.AnnotationType;
import org.lappsgrid.metadata.ContentType;
import org.lappsgrid.metadata.IOSpecification;
import org.w3c.dom.NodeList;
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
import javax.tools.Diagnostic;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Keith Suderman
 */
//@SupportedAnnotationTypes({"org.lappsgrid.experimental.annotations.ServiceMetadata",
//		  "org.lappsgrid.experimental.annotations.DataSourceMetadata"})
@SupportedAnnotationTypes({"org.lappsgrid.experimental.annotations.ServiceMetadata",
		  "org.lappsgrid.experimental.annotations.DataSourceMetadata"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MetadataProcessor extends AbstractProcessor
{
//   private Properties defaults = new Properties();

   public MetadataProcessor()
   {
   }

   private void log(String message)
   {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
   }

   private void debug(String message)
   {
      System.out.println(message);
   }

   private String toString(String[] list)
   {
		if (list == null) {
			return "";
		}
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

   private void log(CombinedMetadata meta)
   {
      debug("Dumping combined metadata.");
      debug("Vendor: " + meta.vendor());
      debug("Version: " + meta.version());
      debug("Description: " + meta.description());
      debug("Encoding: " + meta.inputEncoding() + " -> " + meta.outputEncoding());
      debug("Allow: " + meta.allow());
      debug("Format: " + meta.inputFormat() + " -> " + meta.outputFormat());
      debug("Language: " + meta.inputLanguage() + " -> " + meta.outputLanguage());
      debug("Produces: " + toString(meta.produces()));
      debug("Requires: " + toString(meta.requires()));
   }

   @Override
   public boolean process(Set<? extends TypeElement> annotations,
                          RoundEnvironment roundEnv)
	{
//      log("Running the MetaData annotation processor.");
		File root = new File("src/main/resources/metadata");
		if (!root.exists())
		{
			if (!root.mkdirs())
			{
				log("Could not create directory " + root.getPath());
				log("ServiceMetadata files will not be generated.");
				return false;
			}
		}
		processServiceMetadata(root, annotations, roundEnv);
		processDataSourceMetadata(root, annotations, roundEnv);
		return true;
	}

	protected void processServiceMetadata(File root, Set<? extends TypeElement> annotations,
													  RoundEnvironment roundEnv)
	{
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
//         TypeMirror parent = type.getSuperclass();
//         ServiceMetadata parentMetadata = parent.getClass().getAnnotation(ServiceMetadata.class);
         CommonMetadata common = type.getAnnotation(CommonMetadata.class);
         ServiceMetadata metadata = type.getAnnotation(ServiceMetadata.class);
			if (common != null)
			{
				System.out.println("parent class has metadata: " + common.toString());
			}
			if (metadata != null)
			{
				System.out.println("this class has metadata: " + className);
			}
         CombinedMetadata combined = new CombinedMetadata(common, metadata);
         File file = new File(root, className + ".json");
            debug("Generating ServiceMetadata for " + className);
//            log(combined);
            try
            {
               writeMetadata(file, className, combined);
            }
            catch (IOException e)
            {
               log(e.getMessage());
               e.printStackTrace();
            }
      }
   }

	public void processDataSourceMetadata(File root, Set<? extends TypeElement> annotations,
														  RoundEnvironment roundEnv) {
		log("Running the DataSourceMetadataProcessor");
		for (Element elem : roundEnv.getElementsAnnotatedWith(DataSourceMetadata.class))
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
			DataSourceMetadata metadata = type.getAnnotation(DataSourceMetadata.class);
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
	}

   private ContentType getContentType(String name)
   {
      Discriminator d = DiscriminatorRegistry.getByName(name);
      if (d == null)
      {
         return null;
      }
      return new ContentType(d.getUri());
   }

   private String get(String string)
   {
      if (string == null || string.length() == 0)
      {
         return null;
      }
      return string;
   }

   private String getValue(String key)
   {
      if (key == null || key.length() == 0)
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
      if (version != null && version.length() > 0)
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

   private <T> void addList(List<T> list, IOSpecification spec)
   {
      for (T item : list)
      {
//         log("Adding " + item.toString() + " to list");
         if (item instanceof ContentType)
         {
            spec.add((ContentType) item);
         }
         else if (item instanceof AnnotationType)
         {
            spec.add((AnnotationType) item);
         }
         else
         {
            spec.add((String) item);
         }
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
		List<String> formats = new ArrayList<String>();
		for (String format : annotation.format())
		{
			formats.add(getValue(format));
		}
		metadata.setFormat(formats);
//		metadata.setFormat(Arrays.asList(annotation.format()));

		UTF8Writer writer = null;
		try
		{
			writer = new UTF8Writer(file);
			writer.write(metadata.toPrettyJson());
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

	private void writeMetadata(File file, String className, CombinedMetadata combined) throws IOException
   {
      org.lappsgrid.metadata.ServiceMetadata metadata = new org.lappsgrid.metadata.ServiceMetadata();
      metadata.setName(className);
      metadata.setDescription(get(combined.description()));
      metadata.setVendor(get(combined.vendor()));
      metadata.setLicense(getValue(combined.license()));
      metadata.setAllow(getValue(combined.allow()));
//      log("Attempting to get version");
      metadata.setVersion(getVersion(combined.version()));

      // Object factories used when generating lists of things.
      Factory<ContentType> contentTypeFactory = new ContentTypeFactory();
      Factory<AnnotationType> annotationTypeFactory = new AnnotationTypeFactory();
      Factory<String> stringFactory = new StringFactory();

      IOSpecification requires = metadata.getRequires();
      log("Setting format");
      List<ContentType> formats = makeList(contentTypeFactory, combined.inputFormat());
		debug("Combined formats.");
		for (ContentType type : formats)
		{
			debug(type.toString());
		}
      addList(formats, requires);

		debug("Required formats.");
		for (ContentType type : requires.getFormat())
		{
			debug(type.toString());
		}

      String encoding = combined.inputEncoding();
      if (encoding != null && encoding.length() > 0)
      {
//         log("Setting encoding to " + encoding);
         requires.setEncoding(encoding);
      }

//      log("Setting languages");
      List<String> languages = makeList(stringFactory, combined.inputLanguage());
      addList(languages, requires);

//      log("Setting annotation types");
      List<AnnotationType> types = makeList(annotationTypeFactory, combined.requires());
      addList(types, requires);

      // Populate the produces IOSpecification
      IOSpecification produces = metadata.getProduces();
//      log("Setting formats.");
      formats = makeList(contentTypeFactory, combined.outputFormat());
      addList(formats, produces);

      encoding = combined.outputEncoding();
//      if (combined.outputEncoding().length() > 0)
//      {
////         log("Using outEncoding");
//         encoding = combined.outputEncoding();
//      }
      if (encoding != null && encoding.length() > 0)
      {
//         log("Setting encoding to " + encoding);
         produces.setEncoding(encoding);
      }

//      log("Setting languages");
      languages = makeList(stringFactory, combined.outputLanguage());
      addList(languages, produces);

//      log("Setting types");
      types = makeList(annotationTypeFactory, combined.produces());
      addList(types, produces);

      UTF8Writer writer = null;
      try
      {
         writer = new UTF8Writer(file);
         writer.write(metadata.toPrettyJson());
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

   private interface Factory<T>
   {
      T make(String uri);
   }

   private static class AnnotationTypeFactory implements Factory<AnnotationType>
   {
      public AnnotationType make(String uri)
      {
//			System.out.println("AnnotationTypeFactory.make");
//			System.out.println("Creating annotation type for " + uri);
			Discriminator d = DiscriminatorRegistry.getByName(uri);
         if (d == null)
         {
				d = DiscriminatorRegistry.getByUri(uri);
				if (d == null) {
//					System.out.println("Unknown type. Returning a dummy.");
					return new AnnotationType();
				}
         }
//			System.out.println("Creating annotation type for discriminator " + d.getUri());
			return new AnnotationType(d);
      }
   }

   private static class ContentTypeFactory implements Factory<ContentType>
   {
      public ContentType make(String uri)
      {
         Discriminator d = DiscriminatorRegistry.getByName(uri);
         if (d == null)
         {
				d = DiscriminatorRegistry.getByUri(uri);
				if (d == null)
				{
					//TODO This should return an error type of some sort.
					return new ContentType(uri);
//					return ContentType.TEXT;
				}
         }
         return new ContentType(d.getUri());
      }
   }

   private static class StringFactory implements Factory<String>
   {
      public String make(String uri)
      {
         return uri;
      }
   }

   private <T> List<T> makeList(Factory<T> factory, String[] array)
   {
      List<T> list = new ArrayList<>();
		if (array == null)
		{
			return list;
		}
      for (String string : array)
      {
         list.add(factory.make(string));
      }
      return list;
   }

   private <T> List<T> makeList(Factory<T> factory, String[] outArray, String outString)
   {
      List<T> list = new ArrayList<T>();
      if (outArray.length > 0)
      {
//         log("Making list from specific array");
         for (String term : outArray)
         {
//            log("Adding " + term);
            list.add(factory.make(term));
         }
      }
      else if (outString.length() > 0)
      {
//         log("Making list from specific value " + outString);
         list.add(factory.make(outString));
      }
//      log("List size: " + list.size());
      return list;
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
