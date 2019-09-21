package org.maltparser.core.options.option;

import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionException;
import org.maltparser.core.options.OptionGroup;
import org.maltparser.core.plugin.PluginLoader;

public class ClassOption extends Option {
   private Class<?> defaultValue;
   private final SortedSet<String> legalValues = Collections.synchronizedSortedSet(new TreeSet());
   private final Map<String, String> legalValueDesc = Collections.synchronizedMap(new HashMap());
   private final Map<String, Class<?>> legalValueClass = Collections.synchronizedMap(new HashMap());
   private final Map<Class<?>, String> classLegalValues = Collections.synchronizedMap(new HashMap());

   public ClassOption(OptionGroup group, String name, String shortDescription, String flag, String usage) throws MaltChainedException {
      super(group, name, shortDescription, flag, usage);
   }

   public Object getValueObject(String value) throws MaltChainedException {
      if (value == null) {
         return null;
      } else if (this.legalValues.contains(value)) {
         return this.legalValueClass.get(value);
      } else {
         throw new OptionException("'" + value + "' is not a legal value for the '" + this.getName() + "' option. ");
      }
   }

   public Object getDefaultValueObject() throws OptionException {
      return this.defaultValue;
   }

   public String getLegalValueString(Class<?> clazz) throws MaltChainedException {
      return (String)this.classLegalValues.get(clazz);
   }

   public void setDefaultValue(String defaultValue) throws MaltChainedException {
      if (defaultValue == null) {
         if (this.legalValues.isEmpty()) {
            throw new OptionException("The default value is null and the legal value set is empty for the '" + this.getName() + "' option. ");
         }

         this.defaultValue = (Class)this.legalValueClass.get(((TreeSet)this.legalValueClass.keySet()).first());
      } else {
         if (!this.legalValues.contains(defaultValue.toLowerCase())) {
            throw new OptionException("The default value '" + defaultValue + "' is not a legal value for the '" + this.getName() + "' option. ");
         }

         this.defaultValue = (Class)this.legalValueClass.get(defaultValue.toLowerCase());
      }

   }

   public Class<?> getClazz(String value) {
      return (Class)this.legalValueClass.get(value);
   }

   public void addLegalValue(String value, String desc, String classname) throws MaltChainedException {
      if (value != null && !value.equals("")) {
         if (this.legalValues.contains(value.toLowerCase())) {
            throw new OptionException("The legal value for the '" + this.getName() + "' option already exists. ");
         } else {
            this.legalValues.add(value.toLowerCase());
            if (desc != null && !desc.equals("")) {
               this.legalValueDesc.put(value.toLowerCase(), desc);
            } else {
               this.legalValueDesc.put(value.toLowerCase(), "Description is missing. ");
            }

            if (classname != null && !classname.equals("")) {
               try {
                  Class<?> clazz = null;
                  if (PluginLoader.instance() != null) {
                     clazz = PluginLoader.instance().getClass(classname);
                  }

                  if (clazz == null) {
                     clazz = Class.forName(classname);
                  }

                  this.legalValueClass.put(value, clazz);
                  this.classLegalValues.put(clazz, value);
               } catch (ClassNotFoundException var5) {
                  throw new OptionException("The class " + classname + " for the '" + this.getName() + "' option could not be found. ", var5);
               }
            } else {
               throw new OptionException("The class name used by the '" + this.getName() + "' option is missing. ");
            }
         }
      } else {
         throw new OptionException("The legal value is missing for the '" + this.getName() + "' option. ");
      }
   }

   public String getDefaultValueString() {
      return (String)this.classLegalValues.get(this.defaultValue);
   }

   public String getStringRepresentation(Object value) {
      return value instanceof Class && this.classLegalValues.containsKey(value) ? (String)this.classLegalValues.get(value) : null;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(super.toString());
      Formatter formatter = new Formatter(sb);
      Iterator i$ = this.legalValues.iterator();

      while(i$.hasNext()) {
         String value = (String)i$.next();
         formatter.format("%2s%-10s - %-20s\n", "", value, this.legalValueDesc.get(value));
      }

      sb.append("-----------------------------------------------------------------------------\n");
      return sb.toString();
   }
}
