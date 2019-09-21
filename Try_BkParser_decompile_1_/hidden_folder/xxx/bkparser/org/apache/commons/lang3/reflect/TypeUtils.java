package org.apache.commons.lang3.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.commons.lang3.ClassUtils;

public class TypeUtils {
   public TypeUtils() {
   }

   public static boolean isAssignable(Type type, Type toType) {
      return isAssignable(type, (Type)toType, (Map)null);
   }

   private static boolean isAssignable(Type type, Type toType, Map<TypeVariable<?>, Type> typeVarAssigns) {
      if (toType != null && !(toType instanceof Class)) {
         if (toType instanceof ParameterizedType) {
            return isAssignable(type, (ParameterizedType)toType, typeVarAssigns);
         } else if (toType instanceof GenericArrayType) {
            return isAssignable(type, (GenericArrayType)toType, typeVarAssigns);
         } else if (toType instanceof WildcardType) {
            return isAssignable(type, (WildcardType)toType, typeVarAssigns);
         } else if (toType instanceof TypeVariable) {
            return isAssignable(type, (TypeVariable)toType, typeVarAssigns);
         } else {
            throw new IllegalStateException("found an unhandled type: " + toType);
         }
      } else {
         return isAssignable(type, (Class)toType);
      }
   }

   private static boolean isAssignable(Type type, Class<?> toClass) {
      if (type == null) {
         return toClass == null || !toClass.isPrimitive();
      } else if (toClass == null) {
         return false;
      } else if (toClass.equals(type)) {
         return true;
      } else if (type instanceof Class) {
         return ClassUtils.isAssignable((Class)type, toClass);
      } else if (type instanceof ParameterizedType) {
         return isAssignable(getRawType((ParameterizedType)type), (Class)toClass);
      } else if (type instanceof TypeVariable) {
         Type[] arr$ = ((TypeVariable)type).getBounds();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Type bound = arr$[i$];
            if (isAssignable(bound, toClass)) {
               return true;
            }
         }

         return false;
      } else if (!(type instanceof GenericArrayType)) {
         if (type instanceof WildcardType) {
            return false;
         } else {
            throw new IllegalStateException("found an unhandled type: " + type);
         }
      } else {
         return toClass.equals(Object.class) || toClass.isArray() && isAssignable(((GenericArrayType)type).getGenericComponentType(), toClass.getComponentType());
      }
   }

   private static boolean isAssignable(Type type, ParameterizedType toParameterizedType, Map<TypeVariable<?>, Type> typeVarAssigns) {
      if (type == null) {
         return true;
      } else if (toParameterizedType == null) {
         return false;
      } else if (toParameterizedType.equals(type)) {
         return true;
      } else {
         Class<?> toClass = getRawType(toParameterizedType);
         Map<TypeVariable<?>, Type> fromTypeVarAssigns = getTypeArguments((Type)type, toClass, (Map)null);
         if (fromTypeVarAssigns == null) {
            return false;
         } else if (fromTypeVarAssigns.isEmpty()) {
            return true;
         } else {
            Map<TypeVariable<?>, Type> toTypeVarAssigns = getTypeArguments(toParameterizedType, toClass, typeVarAssigns);
            Iterator i$ = toTypeVarAssigns.entrySet().iterator();

            Type toTypeArg;
            Type fromTypeArg;
            do {
               do {
                  do {
                     if (!i$.hasNext()) {
                        return true;
                     }

                     Entry<TypeVariable<?>, Type> entry = (Entry)i$.next();
                     toTypeArg = (Type)entry.getValue();
                     fromTypeArg = (Type)fromTypeVarAssigns.get(entry.getKey());
                  } while(fromTypeArg == null);
               } while(toTypeArg.equals(fromTypeArg));
            } while(toTypeArg instanceof WildcardType && isAssignable(fromTypeArg, toTypeArg, typeVarAssigns));

            return false;
         }
      }
   }

   private static boolean isAssignable(Type type, GenericArrayType toGenericArrayType, Map<TypeVariable<?>, Type> typeVarAssigns) {
      if (type == null) {
         return true;
      } else if (toGenericArrayType == null) {
         return false;
      } else if (toGenericArrayType.equals(type)) {
         return true;
      } else {
         Type toComponentType = toGenericArrayType.getGenericComponentType();
         if (!(type instanceof Class)) {
            if (type instanceof GenericArrayType) {
               return isAssignable(((GenericArrayType)type).getGenericComponentType(), toComponentType, typeVarAssigns);
            } else {
               int len$;
               int i$;
               Type bound;
               Type[] arr$;
               if (type instanceof WildcardType) {
                  arr$ = getImplicitUpperBounds((WildcardType)type);
                  len$ = arr$.length;

                  for(i$ = 0; i$ < len$; ++i$) {
                     bound = arr$[i$];
                     if (isAssignable(bound, (Type)toGenericArrayType)) {
                        return true;
                     }
                  }

                  return false;
               } else if (type instanceof TypeVariable) {
                  arr$ = getImplicitBounds((TypeVariable)type);
                  len$ = arr$.length;

                  for(i$ = 0; i$ < len$; ++i$) {
                     bound = arr$[i$];
                     if (isAssignable(bound, (Type)toGenericArrayType)) {
                        return true;
                     }
                  }

                  return false;
               } else if (type instanceof ParameterizedType) {
                  return false;
               } else {
                  throw new IllegalStateException("found an unhandled type: " + type);
               }
            }
         } else {
            Class<?> cls = (Class)type;
            return cls.isArray() && isAssignable(cls.getComponentType(), (Type)toComponentType, typeVarAssigns);
         }
      }
   }

   private static boolean isAssignable(Type type, WildcardType toWildcardType, Map<TypeVariable<?>, Type> typeVarAssigns) {
      if (type == null) {
         return true;
      } else if (toWildcardType == null) {
         return false;
      } else if (toWildcardType.equals(type)) {
         return true;
      } else {
         Type[] toUpperBounds = getImplicitUpperBounds(toWildcardType);
         Type[] toLowerBounds = getImplicitLowerBounds(toWildcardType);
         if (!(type instanceof WildcardType)) {
            Type[] arr$ = toUpperBounds;
            int len$ = toUpperBounds.length;

            int i$;
            Type toBound;
            for(i$ = 0; i$ < len$; ++i$) {
               toBound = arr$[i$];
               if (!isAssignable(type, substituteTypeVariables(toBound, typeVarAssigns), typeVarAssigns)) {
                  return false;
               }
            }

            arr$ = toLowerBounds;
            len$ = toLowerBounds.length;

            for(i$ = 0; i$ < len$; ++i$) {
               toBound = arr$[i$];
               if (!isAssignable(substituteTypeVariables(toBound, typeVarAssigns), type, typeVarAssigns)) {
                  return false;
               }
            }

            return true;
         } else {
            WildcardType wildcardType = (WildcardType)type;
            Type[] upperBounds = getImplicitUpperBounds(wildcardType);
            Type[] lowerBounds = getImplicitLowerBounds(wildcardType);
            Type[] arr$ = toUpperBounds;
            int len$ = toUpperBounds.length;

            int i$;
            Type toBound;
            Type[] arr$;
            int len$;
            int i$;
            Type bound;
            for(i$ = 0; i$ < len$; ++i$) {
               toBound = arr$[i$];
               toBound = substituteTypeVariables(toBound, typeVarAssigns);
               arr$ = upperBounds;
               len$ = upperBounds.length;

               for(i$ = 0; i$ < len$; ++i$) {
                  bound = arr$[i$];
                  if (!isAssignable(bound, toBound, typeVarAssigns)) {
                     return false;
                  }
               }
            }

            arr$ = toLowerBounds;
            len$ = toLowerBounds.length;

            for(i$ = 0; i$ < len$; ++i$) {
               toBound = arr$[i$];
               toBound = substituteTypeVariables(toBound, typeVarAssigns);
               arr$ = lowerBounds;
               len$ = lowerBounds.length;

               for(i$ = 0; i$ < len$; ++i$) {
                  bound = arr$[i$];
                  if (!isAssignable(toBound, bound, typeVarAssigns)) {
                     return false;
                  }
               }
            }

            return true;
         }
      }
   }

   private static boolean isAssignable(Type type, TypeVariable<?> toTypeVariable, Map<TypeVariable<?>, Type> typeVarAssigns) {
      if (type == null) {
         return true;
      } else if (toTypeVariable == null) {
         return false;
      } else if (toTypeVariable.equals(type)) {
         return true;
      } else {
         if (type instanceof TypeVariable) {
            Type[] bounds = getImplicitBounds((TypeVariable)type);
            Type[] arr$ = bounds;
            int len$ = bounds.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Type bound = arr$[i$];
               if (isAssignable(bound, toTypeVariable, typeVarAssigns)) {
                  return true;
               }
            }
         }

         if (!(type instanceof Class) && !(type instanceof ParameterizedType) && !(type instanceof GenericArrayType) && !(type instanceof WildcardType)) {
            throw new IllegalStateException("found an unhandled type: " + type);
         } else {
            return false;
         }
      }
   }

   private static Type substituteTypeVariables(Type type, Map<TypeVariable<?>, Type> typeVarAssigns) {
      if (type instanceof TypeVariable && typeVarAssigns != null) {
         Type replacementType = (Type)typeVarAssigns.get(type);
         if (replacementType == null) {
            throw new IllegalArgumentException("missing assignment type for type variable " + type);
         } else {
            return replacementType;
         }
      } else {
         return type;
      }
   }

   public static Map<TypeVariable<?>, Type> getTypeArguments(ParameterizedType type) {
      return getTypeArguments((ParameterizedType)type, getRawType(type), (Map)null);
   }

   public static Map<TypeVariable<?>, Type> getTypeArguments(Type type, Class<?> toClass) {
      return getTypeArguments((Type)type, toClass, (Map)null);
   }

   private static Map<TypeVariable<?>, Type> getTypeArguments(Type type, Class<?> toClass, Map<TypeVariable<?>, Type> subtypeVarAssigns) {
      if (type instanceof Class) {
         return getTypeArguments((Class)type, toClass, subtypeVarAssigns);
      } else if (type instanceof ParameterizedType) {
         return getTypeArguments((ParameterizedType)type, toClass, subtypeVarAssigns);
      } else if (type instanceof GenericArrayType) {
         return getTypeArguments(((GenericArrayType)type).getGenericComponentType(), toClass.isArray() ? toClass.getComponentType() : toClass, subtypeVarAssigns);
      } else {
         Type[] arr$;
         int len$;
         int i$;
         Type bound;
         if (type instanceof WildcardType) {
            arr$ = getImplicitUpperBounds((WildcardType)type);
            len$ = arr$.length;

            for(i$ = 0; i$ < len$; ++i$) {
               bound = arr$[i$];
               if (isAssignable(bound, toClass)) {
                  return getTypeArguments(bound, toClass, subtypeVarAssigns);
               }
            }

            return null;
         } else if (type instanceof TypeVariable) {
            arr$ = getImplicitBounds((TypeVariable)type);
            len$ = arr$.length;

            for(i$ = 0; i$ < len$; ++i$) {
               bound = arr$[i$];
               if (isAssignable(bound, toClass)) {
                  return getTypeArguments(bound, toClass, subtypeVarAssigns);
               }
            }

            return null;
         } else {
            throw new IllegalStateException("found an unhandled type: " + type);
         }
      }
   }

   private static Map<TypeVariable<?>, Type> getTypeArguments(ParameterizedType parameterizedType, Class<?> toClass, Map<TypeVariable<?>, Type> subtypeVarAssigns) {
      Class<?> cls = getRawType(parameterizedType);
      if (!isAssignable(cls, (Class)toClass)) {
         return null;
      } else {
         Type ownerType = parameterizedType.getOwnerType();
         Object typeVarAssigns;
         if (ownerType instanceof ParameterizedType) {
            ParameterizedType parameterizedOwnerType = (ParameterizedType)ownerType;
            typeVarAssigns = getTypeArguments(parameterizedOwnerType, getRawType(parameterizedOwnerType), subtypeVarAssigns);
         } else {
            typeVarAssigns = subtypeVarAssigns == null ? new HashMap() : new HashMap(subtypeVarAssigns);
         }

         Type[] typeArgs = parameterizedType.getActualTypeArguments();
         TypeVariable<?>[] typeParams = cls.getTypeParameters();

         for(int i = 0; i < typeParams.length; ++i) {
            Type typeArg = typeArgs[i];
            ((Map)typeVarAssigns).put(typeParams[i], ((Map)typeVarAssigns).containsKey(typeArg) ? (Type)((Map)typeVarAssigns).get(typeArg) : typeArg);
         }

         if (toClass.equals(cls)) {
            return (Map)typeVarAssigns;
         } else {
            return getTypeArguments((Type)getClosestParentType(cls, toClass), toClass, (Map)typeVarAssigns);
         }
      }
   }

   private static Map<TypeVariable<?>, Type> getTypeArguments(Class<?> cls, Class<?> toClass, Map<TypeVariable<?>, Type> subtypeVarAssigns) {
      if (!isAssignable(cls, (Class)toClass)) {
         return null;
      } else {
         if (cls.isPrimitive()) {
            if (toClass.isPrimitive()) {
               return new HashMap();
            }

            cls = ClassUtils.primitiveToWrapper(cls);
         }

         HashMap<TypeVariable<?>, Type> typeVarAssigns = subtypeVarAssigns == null ? new HashMap() : new HashMap(subtypeVarAssigns);
         return (Map)(cls.getTypeParameters().length <= 0 && !toClass.equals(cls) ? getTypeArguments((Type)getClosestParentType(cls, toClass), toClass, typeVarAssigns) : typeVarAssigns);
      }
   }

   public static Map<TypeVariable<?>, Type> determineTypeArguments(Class<?> cls, ParameterizedType superType) {
      Class<?> superClass = getRawType(superType);
      if (!isAssignable(cls, (Class)superClass)) {
         return null;
      } else if (cls.equals(superClass)) {
         return getTypeArguments((ParameterizedType)superType, superClass, (Map)null);
      } else {
         Type midType = getClosestParentType(cls, superClass);
         if (midType instanceof Class) {
            return determineTypeArguments((Class)midType, superType);
         } else {
            ParameterizedType midParameterizedType = (ParameterizedType)midType;
            Class<?> midClass = getRawType(midParameterizedType);
            Map<TypeVariable<?>, Type> typeVarAssigns = determineTypeArguments(midClass, superType);
            mapTypeVariablesToArguments(cls, midParameterizedType, typeVarAssigns);
            return typeVarAssigns;
         }
      }
   }

   private static <T> void mapTypeVariablesToArguments(Class<T> cls, ParameterizedType parameterizedType, Map<TypeVariable<?>, Type> typeVarAssigns) {
      Type ownerType = parameterizedType.getOwnerType();
      if (ownerType instanceof ParameterizedType) {
         mapTypeVariablesToArguments(cls, (ParameterizedType)ownerType, typeVarAssigns);
      }

      Type[] typeArgs = parameterizedType.getActualTypeArguments();
      TypeVariable<?>[] typeVars = getRawType(parameterizedType).getTypeParameters();
      List<TypeVariable<Class<T>>> typeVarList = Arrays.asList(cls.getTypeParameters());

      for(int i = 0; i < typeArgs.length; ++i) {
         TypeVariable<?> typeVar = typeVars[i];
         Type typeArg = typeArgs[i];
         if (typeVarList.contains(typeArg) && typeVarAssigns.containsKey(typeVar)) {
            typeVarAssigns.put((TypeVariable)typeArg, typeVarAssigns.get(typeVar));
         }
      }

   }

   private static Type getClosestParentType(Class<?> cls, Class<?> superClass) {
      if (superClass.isInterface()) {
         Type[] interfaceTypes = cls.getGenericInterfaces();
         Type genericInterface = null;
         Type[] arr$ = interfaceTypes;
         int len$ = interfaceTypes.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Type midType = arr$[i$];
            Class<?> midClass = null;
            if (midType instanceof ParameterizedType) {
               midClass = getRawType((ParameterizedType)midType);
            } else {
               if (!(midType instanceof Class)) {
                  throw new IllegalStateException("Unexpected generic interface type found: " + midType);
               }

               midClass = (Class)midType;
            }

            if (isAssignable(midClass, (Class)superClass) && isAssignable(genericInterface, (Type)midClass)) {
               genericInterface = midType;
            }
         }

         if (genericInterface != null) {
            return genericInterface;
         }
      }

      return cls.getGenericSuperclass();
   }

   public static boolean isInstance(Object value, Type type) {
      if (type == null) {
         return false;
      } else {
         return value == null ? !(type instanceof Class) || !((Class)type).isPrimitive() : isAssignable(value.getClass(), (Type)type, (Map)null);
      }
   }

   public static Type[] normalizeUpperBounds(Type[] bounds) {
      if (bounds.length < 2) {
         return bounds;
      } else {
         Set<Type> types = new HashSet(bounds.length);
         Type[] arr$ = bounds;
         int len$ = bounds.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Type type1 = arr$[i$];
            boolean subtypeFound = false;
            Type[] arr$ = bounds;
            int len$ = bounds.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Type type2 = arr$[i$];
               if (type1 != type2 && isAssignable(type2, (Type)type1, (Map)null)) {
                  subtypeFound = true;
                  break;
               }
            }

            if (!subtypeFound) {
               types.add(type1);
            }
         }

         return (Type[])types.toArray(new Type[types.size()]);
      }
   }

   public static Type[] getImplicitBounds(TypeVariable<?> typeVariable) {
      Type[] bounds = typeVariable.getBounds();
      return bounds.length == 0 ? new Type[]{Object.class} : normalizeUpperBounds(bounds);
   }

   public static Type[] getImplicitUpperBounds(WildcardType wildcardType) {
      Type[] bounds = wildcardType.getUpperBounds();
      return bounds.length == 0 ? new Type[]{Object.class} : normalizeUpperBounds(bounds);
   }

   public static Type[] getImplicitLowerBounds(WildcardType wildcardType) {
      Type[] bounds = wildcardType.getLowerBounds();
      return bounds.length == 0 ? new Type[]{null} : bounds;
   }

   public static boolean typesSatisfyVariables(Map<TypeVariable<?>, Type> typeVarAssigns) {
      Iterator i$ = typeVarAssigns.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<TypeVariable<?>, Type> entry = (Entry)i$.next();
         TypeVariable<?> typeVar = (TypeVariable)entry.getKey();
         Type type = (Type)entry.getValue();
         Type[] arr$ = getImplicitBounds(typeVar);
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Type bound = arr$[i$];
            if (!isAssignable(type, substituteTypeVariables(bound, typeVarAssigns), typeVarAssigns)) {
               return false;
            }
         }
      }

      return true;
   }

   private static Class<?> getRawType(ParameterizedType parameterizedType) {
      Type rawType = parameterizedType.getRawType();
      if (!(rawType instanceof Class)) {
         throw new IllegalStateException("Wait... What!? Type of rawType: " + rawType);
      } else {
         return (Class)rawType;
      }
   }

   public static Class<?> getRawType(Type type, Type assigningType) {
      if (type instanceof Class) {
         return (Class)type;
      } else if (type instanceof ParameterizedType) {
         return getRawType((ParameterizedType)type);
      } else if (type instanceof TypeVariable) {
         if (assigningType == null) {
            return null;
         } else {
            Object genericDeclaration = ((TypeVariable)type).getGenericDeclaration();
            if (!(genericDeclaration instanceof Class)) {
               return null;
            } else {
               Map<TypeVariable<?>, Type> typeVarAssigns = getTypeArguments(assigningType, (Class)genericDeclaration);
               if (typeVarAssigns == null) {
                  return null;
               } else {
                  Type typeArgument = (Type)typeVarAssigns.get(type);
                  return typeArgument == null ? null : getRawType(typeArgument, assigningType);
               }
            }
         }
      } else if (type instanceof GenericArrayType) {
         Class<?> rawComponentType = getRawType(((GenericArrayType)type).getGenericComponentType(), assigningType);
         return Array.newInstance(rawComponentType, 0).getClass();
      } else if (type instanceof WildcardType) {
         return null;
      } else {
         throw new IllegalArgumentException("unknown type: " + type);
      }
   }

   public static boolean isArrayType(Type type) {
      return type instanceof GenericArrayType || type instanceof Class && ((Class)type).isArray();
   }

   public static Type getArrayComponentType(Type type) {
      if (type instanceof Class) {
         Class<?> clazz = (Class)type;
         return clazz.isArray() ? clazz.getComponentType() : null;
      } else {
         return type instanceof GenericArrayType ? ((GenericArrayType)type).getGenericComponentType() : null;
      }
   }
}
