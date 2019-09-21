package org.maltparser.core.options;

import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.core.options.option.Option;

public class OptionContainer implements Comparable<OptionContainer> {
   public static final int SAVEDOPTION = 0;
   public static final int DEPENDENCIES_RESOLVED = 1;
   public static final int COMMANDLINE = 2;
   public static final int OPTIONFILE = 3;
   private final int index;
   private final SortedMap<Option, Object> savedOptionMap;
   private final SortedMap<Option, Object> dependenciesResolvedOptionMap;
   private final SortedMap<Option, Object> commandLineOptionMap;
   private final SortedMap<Option, Object> optionFileOptionMap;

   public OptionContainer(int index) throws OptionException {
      this.index = index;
      this.savedOptionMap = Collections.synchronizedSortedMap(new TreeMap());
      this.dependenciesResolvedOptionMap = Collections.synchronizedSortedMap(new TreeMap());
      this.commandLineOptionMap = Collections.synchronizedSortedMap(new TreeMap());
      this.optionFileOptionMap = Collections.synchronizedSortedMap(new TreeMap());
   }

   protected void addOptionValue(int type, Option option, Object value) throws OptionException {
      if (type == 0) {
         this.savedOptionMap.put(option, value);
      } else if (type == 1) {
         this.dependenciesResolvedOptionMap.put(option, value);
      } else if (type == 2) {
         this.commandLineOptionMap.put(option, value);
      } else {
         if (type != 3) {
            throw new OptionException("Unknown option container type");
         }

         this.optionFileOptionMap.put(option, value);
      }

   }

   public Object getOptionValue(Option option) {
      Object value = null;

      for(int i = 0; i <= 3; ++i) {
         if (i == 0) {
            value = this.savedOptionMap.get(option);
         } else if (i == 1) {
            value = this.dependenciesResolvedOptionMap.get(option);
         } else if (i == 2) {
            value = this.commandLineOptionMap.get(option);
         } else if (i == 3) {
            value = this.optionFileOptionMap.get(option);
         }

         if (value != null) {
            return value;
         }
      }

      return null;
   }

   public String getOptionValueString(Option option) {
      String value = null;

      for(int i = 0; i <= 3; ++i) {
         if (i == 0) {
            value = option.getStringRepresentation(this.savedOptionMap.get(option));
         } else if (i == 1) {
            value = option.getStringRepresentation(this.dependenciesResolvedOptionMap.get(option));
         } else if (i == 2) {
            value = option.getStringRepresentation(this.commandLineOptionMap.get(option));
         } else if (i == 3) {
            value = option.getStringRepresentation(this.optionFileOptionMap.get(option));
         }

         if (value != null) {
            return value;
         }
      }

      return null;
   }

   public boolean contains(int type, Option option) throws OptionException {
      if (type == 0) {
         return this.savedOptionMap.containsValue(option);
      } else if (type == 1) {
         return this.dependenciesResolvedOptionMap.containsValue(option);
      } else if (type == 2) {
         return this.commandLineOptionMap.containsValue(option);
      } else if (type == 3) {
         return this.optionFileOptionMap.containsValue(option);
      } else {
         throw new OptionException("Unknown option container type");
      }
   }

   public int getNumberOfOptionValues() {
      SortedSet<Option> union = new TreeSet(this.savedOptionMap.keySet());
      union.addAll(this.dependenciesResolvedOptionMap.keySet());
      union.addAll(this.commandLineOptionMap.keySet());
      union.addAll(this.optionFileOptionMap.keySet());
      return union.size();
   }

   public int getIndex() {
      return this.index;
   }

   public int compareTo(OptionContainer that) {
      int BEFORE = true;
      int EQUAL = false;
      int AFTER = true;
      if (this == that) {
         return 0;
      } else if (this.index < that.index) {
         return -1;
      } else {
         return this.index > that.index ? 1 : 0;
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      SortedSet<Option> union = new TreeSet(this.savedOptionMap.keySet());
      union.addAll(this.dependenciesResolvedOptionMap.keySet());
      union.addAll(this.commandLineOptionMap.keySet());
      union.addAll(this.optionFileOptionMap.keySet());

      Option option;
      Object value;
      for(Iterator i$ = union.iterator(); i$.hasNext(); sb.append(option.getGroup().getName() + "\t" + option.getName() + "\t" + value + "\n")) {
         option = (Option)i$.next();
         value = null;

         for(int i = 0; i <= 3; ++i) {
            if (i == 0) {
               value = this.savedOptionMap.get(option);
            } else if (i == 1) {
               value = this.dependenciesResolvedOptionMap.get(option);
            } else if (i == 2) {
               value = this.commandLineOptionMap.get(option);
            } else if (i == 3) {
               value = this.optionFileOptionMap.get(option);
            }

            if (value != null) {
               break;
            }
         }
      }

      return sb.toString();
   }
}
