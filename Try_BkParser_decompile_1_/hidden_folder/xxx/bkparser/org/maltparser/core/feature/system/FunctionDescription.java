package org.maltparser.core.feature.system;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.feature.function.Function;

public class FunctionDescription {
   private final String name;
   private final Class<?> functionClass;
   private final boolean hasSubfunctions;
   private final boolean hasFactory;

   public FunctionDescription(String _name, Class<?> _functionClass, boolean _hasSubfunctions, boolean _hasFactory) {
      this.name = _name;
      this.functionClass = _functionClass;
      this.hasSubfunctions = _hasSubfunctions;
      this.hasFactory = _hasFactory;
   }

   public Function newFunction(FeatureRegistry registry) throws MaltChainedException {
      if (this.hasFactory) {
         return registry.getFactory(this.functionClass).makeFunction(this.name, registry);
      } else {
         Constructor<?>[] constructors = this.functionClass.getConstructors();
         if (constructors.length == 0) {
            try {
               return (Function)this.functionClass.newInstance();
            } catch (InstantiationException var6) {
               throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", var6);
            } catch (IllegalAccessException var7) {
               throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", var7);
            }
         } else {
            Class<?>[] params = constructors[0].getParameterTypes();
            if (params.length == 0) {
               try {
                  return (Function)this.functionClass.newInstance();
               } catch (InstantiationException var8) {
                  throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", var8);
               } catch (IllegalAccessException var9) {
                  throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", var9);
               }
            } else {
               Object[] arguments = new Object[params.length];

               for(int i = 0; i < params.length; ++i) {
                  if (this.hasSubfunctions && params[i] == String.class) {
                     arguments[i] = this.name;
                  } else {
                     arguments[i] = registry.get(params[i]);
                     if (arguments[i] == null) {
                        return null;
                     }
                  }
               }

               try {
                  return (Function)constructors[0].newInstance(arguments);
               } catch (InstantiationException var10) {
                  throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", var10);
               } catch (IllegalAccessException var11) {
                  throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", var11);
               } catch (InvocationTargetException var12) {
                  throw new FeatureException("The function '" + this.functionClass.getName() + "' cannot be initialized. ", var12);
               }
            }
         }
      }
   }

   public String getName() {
      return this.name;
   }

   public Class<?> getFunctionClass() {
      return this.functionClass;
   }

   public boolean isHasSubfunctions() {
      return this.hasSubfunctions;
   }

   public boolean isHasFactory() {
      return this.hasFactory;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else if (!this.name.equalsIgnoreCase(((FunctionDescription)obj).getName())) {
         return false;
      } else {
         return this.functionClass.equals(((FunctionDescription)obj).getFunctionClass());
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.name);
      sb.append("->");
      sb.append(this.functionClass.getName());
      return sb.toString();
   }
}
