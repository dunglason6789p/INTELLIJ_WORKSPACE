package com.google.gson;

import java.lang.reflect.Field;
import java.util.Locale;

public enum FieldNamingPolicy implements FieldNamingStrategy {
   IDENTITY {
      public String translateName(Field f) {
         return f.getName();
      }
   },
   UPPER_CAMEL_CASE {
      public String translateName(Field f) {
         return upperCaseFirstLetter(f.getName());
      }
   },
   UPPER_CAMEL_CASE_WITH_SPACES {
      public String translateName(Field f) {
         return upperCaseFirstLetter(separateCamelCase(f.getName(), " "));
      }
   },
   LOWER_CASE_WITH_UNDERSCORES {
      public String translateName(Field f) {
         return separateCamelCase(f.getName(), "_").toLowerCase(Locale.ENGLISH);
      }
   },
   LOWER_CASE_WITH_DASHES {
      public String translateName(Field f) {
         return separateCamelCase(f.getName(), "-").toLowerCase(Locale.ENGLISH);
      }
   };

   private FieldNamingPolicy() {
   }

   static String separateCamelCase(String name, String separator) {
      StringBuilder translation = new StringBuilder();
      int i = 0;

      for(int length = name.length(); i < length; ++i) {
         char character = name.charAt(i);
         if (Character.isUpperCase(character) && translation.length() != 0) {
            translation.append(separator);
         }

         translation.append(character);
      }

      return translation.toString();
   }

   static String upperCaseFirstLetter(String name) {
      StringBuilder fieldNameBuilder = new StringBuilder();
      int index = 0;
      char firstCharacter = name.charAt(index);

      for(int length = name.length(); index < length - 1 && !Character.isLetter(firstCharacter); firstCharacter = name.charAt(index)) {
         fieldNameBuilder.append(firstCharacter);
         ++index;
      }

      if (!Character.isUpperCase(firstCharacter)) {
         char var10000 = Character.toUpperCase(firstCharacter);
         ++index;
         String modifiedTarget = modifyString(var10000, name, index);
         return fieldNameBuilder.append(modifiedTarget).toString();
      } else {
         return name;
      }
   }

   private static String modifyString(char firstCharacter, String srcString, int indexOfSubstring) {
      return indexOfSubstring < srcString.length() ? firstCharacter + srcString.substring(indexOfSubstring) : String.valueOf(firstCharacter);
   }
}
