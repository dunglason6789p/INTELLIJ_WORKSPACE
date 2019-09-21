package org.apache.commons.lang3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnumUtils {
   public EnumUtils() {
   }

   public static <E extends Enum<E>> Map<String, E> getEnumMap(Class<E> enumClass) {
      Map<String, E> map = new LinkedHashMap();
      Enum[] arr$ = (Enum[])enumClass.getEnumConstants();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         E e = arr$[i$];
         map.put(e.name(), e);
      }

      return map;
   }

   public static <E extends Enum<E>> List<E> getEnumList(Class<E> enumClass) {
      return new ArrayList(Arrays.asList(enumClass.getEnumConstants()));
   }

   public static <E extends Enum<E>> boolean isValidEnum(Class<E> enumClass, String enumName) {
      if (enumName == null) {
         return false;
      } else {
         try {
            Enum.valueOf(enumClass, enumName);
            return true;
         } catch (IllegalArgumentException var3) {
            return false;
         }
      }
   }

   public static <E extends Enum<E>> E getEnum(Class<E> enumClass, String enumName) {
      if (enumName == null) {
         return null;
      } else {
         try {
            return Enum.valueOf(enumClass, enumName);
         } catch (IllegalArgumentException var3) {
            return null;
         }
      }
   }
}
