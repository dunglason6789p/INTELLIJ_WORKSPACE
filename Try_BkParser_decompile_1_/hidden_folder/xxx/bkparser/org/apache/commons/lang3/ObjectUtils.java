package org.apache.commons.lang3;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.lang3.exception.CloneFailedException;

public class ObjectUtils {
   public static final ObjectUtils.Null NULL = new ObjectUtils.Null();

   public ObjectUtils() {
   }

   public static <T> T defaultIfNull(T object, T defaultValue) {
      return object != null ? object : defaultValue;
   }

   public static <T> T firstNonNull(T... values) {
      if (values != null) {
         Object[] arr$ = values;
         int len$ = values.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            T val = arr$[i$];
            if (val != null) {
               return val;
            }
         }
      }

      return null;
   }

   public static boolean equals(Object object1, Object object2) {
      if (object1 == object2) {
         return true;
      } else {
         return object1 != null && object2 != null ? object1.equals(object2) : false;
      }
   }

   public static boolean notEqual(Object object1, Object object2) {
      return !equals(object1, object2);
   }

   public static int hashCode(Object obj) {
      return obj == null ? 0 : obj.hashCode();
   }

   public static int hashCodeMulti(Object... objects) {
      int hash = 1;
      if (objects != null) {
         Object[] arr$ = objects;
         int len$ = objects.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Object object = arr$[i$];
            hash = hash * 31 + hashCode(object);
         }
      }

      return hash;
   }

   public static String identityToString(Object object) {
      if (object == null) {
         return null;
      } else {
         StringBuffer buffer = new StringBuffer();
         identityToString(buffer, object);
         return buffer.toString();
      }
   }

   public static void identityToString(StringBuffer buffer, Object object) {
      if (object == null) {
         throw new NullPointerException("Cannot get the toString of a null identity");
      } else {
         buffer.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
      }
   }

   public static String toString(Object obj) {
      return obj == null ? "" : obj.toString();
   }

   public static String toString(Object obj, String nullStr) {
      return obj == null ? nullStr : obj.toString();
   }

   public static <T extends Comparable<? super T>> T min(T... values) {
      T result = null;
      if (values != null) {
         Comparable[] arr$ = values;
         int len$ = values.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            T value = arr$[i$];
            if (compare(value, result, true) < 0) {
               result = value;
            }
         }
      }

      return result;
   }

   public static <T extends Comparable<? super T>> T max(T... values) {
      T result = null;
      if (values != null) {
         Comparable[] arr$ = values;
         int len$ = values.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            T value = arr$[i$];
            if (compare(value, result, false) > 0) {
               result = value;
            }
         }
      }

      return result;
   }

   public static <T extends Comparable<? super T>> int compare(T c1, T c2) {
      return compare(c1, c2, false);
   }

   public static <T extends Comparable<? super T>> int compare(T c1, T c2, boolean nullGreater) {
      if (c1 == c2) {
         return 0;
      } else if (c1 == null) {
         return nullGreater ? 1 : -1;
      } else if (c2 == null) {
         return nullGreater ? -1 : 1;
      } else {
         return c1.compareTo(c2);
      }
   }

   public static <T> T clone(T obj) {
      if (!(obj instanceof Cloneable)) {
         return null;
      } else {
         Object result;
         if (obj.getClass().isArray()) {
            Class<?> componentType = obj.getClass().getComponentType();
            if (!componentType.isPrimitive()) {
               result = ((Object[])((Object[])obj)).clone();
            } else {
               int length = Array.getLength(obj);
               result = Array.newInstance(componentType, length);

               while(length-- > 0) {
                  Array.set(result, length, Array.get(obj, length));
               }
            }
         } else {
            try {
               Method clone = obj.getClass().getMethod("clone");
               result = clone.invoke(obj);
            } catch (NoSuchMethodException var4) {
               throw new CloneFailedException("Cloneable type " + obj.getClass().getName() + " has no clone method", var4);
            } catch (IllegalAccessException var5) {
               throw new CloneFailedException("Cannot clone Cloneable type " + obj.getClass().getName(), var5);
            } catch (InvocationTargetException var6) {
               throw new CloneFailedException("Exception cloning Cloneable type " + obj.getClass().getName(), var6.getCause());
            }
         }

         return result;
      }
   }

   public static <T> T cloneIfPossible(T obj) {
      T clone = clone(obj);
      return clone == null ? obj : clone;
   }

   public static class Null implements Serializable {
      private static final long serialVersionUID = 7092611880189329093L;

      Null() {
      }

      private Object readResolve() {
         return ObjectUtils.NULL;
      }
   }
}
