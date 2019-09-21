package org.apache.commons.lang3.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;

public class ConstructorUtils {
   public ConstructorUtils() {
   }

   public static <T> T invokeConstructor(Class<T> cls, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      if (args == null) {
         args = ArrayUtils.EMPTY_OBJECT_ARRAY;
      }

      Class<?>[] parameterTypes = new Class[args.length];

      for(int i = 0; i < args.length; ++i) {
         parameterTypes[i] = args[i].getClass();
      }

      return invokeConstructor(cls, args, parameterTypes);
   }

   public static <T> T invokeConstructor(Class<T> cls, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      if (parameterTypes == null) {
         parameterTypes = ArrayUtils.EMPTY_CLASS_ARRAY;
      }

      if (args == null) {
         args = ArrayUtils.EMPTY_OBJECT_ARRAY;
      }

      Constructor<T> ctor = getMatchingAccessibleConstructor(cls, parameterTypes);
      if (ctor == null) {
         throw new NoSuchMethodException("No such accessible constructor on object: " + cls.getName());
      } else {
         return ctor.newInstance(args);
      }
   }

   public static <T> T invokeExactConstructor(Class<T> cls, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      if (args == null) {
         args = ArrayUtils.EMPTY_OBJECT_ARRAY;
      }

      int arguments = args.length;
      Class<?>[] parameterTypes = new Class[arguments];

      for(int i = 0; i < arguments; ++i) {
         parameterTypes[i] = args[i].getClass();
      }

      return invokeExactConstructor(cls, args, parameterTypes);
   }

   public static <T> T invokeExactConstructor(Class<T> cls, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      if (args == null) {
         args = ArrayUtils.EMPTY_OBJECT_ARRAY;
      }

      if (parameterTypes == null) {
         parameterTypes = ArrayUtils.EMPTY_CLASS_ARRAY;
      }

      Constructor<T> ctor = getAccessibleConstructor(cls, parameterTypes);
      if (ctor == null) {
         throw new NoSuchMethodException("No such accessible constructor on object: " + cls.getName());
      } else {
         return ctor.newInstance(args);
      }
   }

   public static <T> Constructor<T> getAccessibleConstructor(Class<T> cls, Class<?>... parameterTypes) {
      try {
         return getAccessibleConstructor(cls.getConstructor(parameterTypes));
      } catch (NoSuchMethodException var3) {
         return null;
      }
   }

   public static <T> Constructor<T> getAccessibleConstructor(Constructor<T> ctor) {
      return MemberUtils.isAccessible(ctor) && Modifier.isPublic(ctor.getDeclaringClass().getModifiers()) ? ctor : null;
   }

   public static <T> Constructor<T> getMatchingAccessibleConstructor(Class<T> cls, Class<?>... parameterTypes) {
      Constructor result;
      try {
         result = cls.getConstructor(parameterTypes);
         MemberUtils.setAccessibleWorkaround(result);
         return result;
      } catch (NoSuchMethodException var9) {
         result = null;
         Constructor<?>[] ctors = cls.getConstructors();
         Constructor[] arr$ = ctors;
         int len$ = ctors.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Constructor<?> ctor = arr$[i$];
            if (ClassUtils.isAssignable(parameterTypes, ctor.getParameterTypes(), true)) {
               ctor = getAccessibleConstructor(ctor);
               if (ctor != null) {
                  MemberUtils.setAccessibleWorkaround(ctor);
                  if (result == null || MemberUtils.compareParameterTypes(ctor.getParameterTypes(), result.getParameterTypes(), parameterTypes) < 0) {
                     result = ctor;
                  }
               }
            }
         }

         return result;
      }
   }
}
