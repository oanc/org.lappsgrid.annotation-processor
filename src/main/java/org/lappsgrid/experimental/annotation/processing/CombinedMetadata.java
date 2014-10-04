package org.lappsgrid.experimental.annotation.processing;

import org.lappsgrid.experimental.annotations.CommonMetadata;
import org.lappsgrid.experimental.annotations.ServiceMetadata;

/**
 * @author Keith Suderman
 */
public class CombinedMetadata
{
   private CommonMetadata common;
   private ServiceMetadata service;

   public CombinedMetadata(CommonMetadata parent, ServiceMetadata service)
   {
		if (service == null)
		{
			throw new  NullPointerException("ServiceMetadata must not be null.");
		}
//      if (common == null)
//      {
//         this.common = service;
//      }
//      else
//      {
//         this.common = common;
//      }
		if (parent != null)
		{
			this.common = parent;
		}
      this.service = service;
   }

   public String description() {
      if (hasValue(service.description())) {
         return service.description();
      }
		if (common == null)
		{
			return null;
		}
      return common.description();
   }
   public String version()
   {
      if (hasValue(service.version()))
      {
         return service.version();
      }
		if (common == null)
		{
			return null;
		}
      return common.version();
   }
   public String vendor()
   {
      if (hasValue(service.vendor()))
      {
         return service.vendor();
      }
		if (common == null)
		{
			return null;
		}
      return common.vendor();
   }
   public String allow()
   {
      if (hasValue(service.allow()))
      {
         return service.allow();
      }
		if (common == null)
		{
			return null;
		}
      return common.allow();
   }
   public String license()
   {
      if (hasValue(service.license()))
      {
//			System.out.println("Service has a value for license: " + service.license());
			return service.license();
      }
//		System.out.println("Using common's license: " + common.license());
		if (common == null)
		{
			return null;
		}
		return common.license();
   }

   public String inputEncoding()
   {
      if (hasValue(service.requires_encoding()))
      {
         return service.requires_encoding();
      }
      if (common != null && hasValue(common.requires_encoding()))
      {
         return common.requires_encoding();
      }
      if (hasValue(service.encoding()))
      {
         return service.encoding();
      }
		if (common == null)
		{
			return null;
		}
      return common.encoding();
   }
   public String outputEncoding()
   {
      if (hasValue(service.produces_encoding()))
      {
         return service.produces_encoding();
      }
      if (common != null && hasValue(common.produces_encoding()))
      {
         return common.produces_encoding();
      }
      if (hasValue(service.encoding()))
      {
         return service.encoding();
      }
		if (common == null)
		{
			return null;
		}
      return common.encoding();
   }
   public String[] inputLanguage()
   {
      if (hasValue(service.requires_language()))
      {
         return service.requires_language();
      }
      if (common != null && hasValue(common.requires_language()))
      {
         return common.requires_language();
      }
      if (hasValue(service.language()))
      {
         return service.language();
      }
		if (common == null)
		{
			return null;
		}
      return common.language();
   }
   public String[] outputLanguage()
   {
      if (hasValue(service.produces_language()))
      {
         return service.produces_language();
      }
      if (common != null && hasValue(common.produces_language()))
      {
         return common.produces_language();
      }
      if (hasValue(service.language()))
      {
         return service.language();
      }
		if (common == null)
		{
			return null;
		}
		return common.language();
   }

   public String[] inputFormat()
   {
		if (hasValue(service.requires_format()))
      {
			return service.requires_format();
      }
      if (common != null && hasValue(common.requires_format()))
      {
			return common.requires_format();
      }
      if (hasValue(service.format()))
      {
			return service.format();
      }
		if (common == null)
		{
			return null;
		}
		return common.format();
   }
   public String[] outputFormat()
   {
      if (hasValue(service.produces_format()))
      {
         return service.produces_format();
      }
      if (common != null && hasValue(common.produces_format()))
      {
         return common.produces_format();
      }
      if (hasValue(service.format()))
      {
         return service.format();
      }
		if (common == null)
		{
			return null;
		}
		return common.format();
   }
   public String[] requires()
   {
		if (hasValue(service.requires()))
      {
			return service.requires();
      }
		if (common == null)
		{
			return null;
		}
		return common.requires();
   }

   public String[] produces()
   {
		if (hasValue(service.produces()))
      {
			return service.produces();
      }
		if (common == null)
		{
			return null;
		}
		return common.produces();
   }

   private boolean hasValue(String input)
   {
      return input != null && input.length() > 0;
   }

   private boolean hasValue(String[] input)
   {
      return input != null && input.length > 0;
   }
}
