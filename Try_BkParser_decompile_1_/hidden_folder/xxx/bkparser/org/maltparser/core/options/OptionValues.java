package org.maltparser.core.options;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.core.options.option.Option;

public class OptionValues {
   private final SortedMap<Integer, OptionContainer> optionContainers = Collections.synchronizedSortedMap(new TreeMap());

   public OptionValues() {
   }

   public Object getOptionValue(int containerIndex, Option option) throws OptionException {
      OptionContainer oc = (OptionContainer)this.optionContainers.get(containerIndex);
      if (oc == null) {
         throw new OptionException("The option container '" + containerIndex + "' cannot be found. ");
      } else {
         return oc.getOptionValue(option);
      }
   }

   public String getOptionValueString(int containerIndex, Option option) throws OptionException {
      OptionContainer oc = (OptionContainer)this.optionContainers.get(containerIndex);
      if (oc == null) {
         throw new OptionException("The option container '" + containerIndex + "' cannot be found. ");
      } else {
         return oc.getOptionValueString(option);
      }
   }

   public Object getOptionValue(Option option) throws OptionException {
      if (this.optionContainers.size() == 0) {
         return null;
      } else {
         OptionContainer oc = (OptionContainer)this.optionContainers.get(this.optionContainers.firstKey());
         return oc.getOptionValue(option);
      }
   }

   public int getNumberOfOptionValues(int containerIndex) {
      return !this.optionContainers.containsKey(containerIndex) ? 0 : ((OptionContainer)this.optionContainers.get(containerIndex)).getNumberOfOptionValues();
   }

   public Set<Integer> getOptionContainerIndices() {
      return this.optionContainers.keySet();
   }

   protected boolean addOptionValue(int containerType, int containerIndex, Option option, Object value) throws OptionException {
      if (option == null) {
         throw new OptionException("The option cannot be found. ");
      } else if (value == null) {
         throw new OptionException("The option value cannot be found. ");
      } else {
         if (!this.optionContainers.containsKey(containerIndex)) {
            this.optionContainers.put(containerIndex, new OptionContainer(containerIndex));
         }

         OptionContainer oc = (OptionContainer)this.optionContainers.get(containerIndex);
         if (oc == null) {
            throw new OptionException("The option container index " + containerIndex + " is unknown");
         } else if (!oc.contains(containerType, option)) {
            oc.addOptionValue(containerType, option, value);
            return true;
         } else {
            return false;
         }
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      if (this.optionContainers.size() == 0) {
         sb.append("No option values.");
      } else if (this.optionContainers.size() == 1) {
         sb.append(this.optionContainers.get(this.optionContainers.firstKey()));
      } else {
         Iterator i$ = this.optionContainers.keySet().iterator();

         while(i$.hasNext()) {
            Integer index = (Integer)i$.next();
            sb.append("Option container : " + index + "\n");
            sb.append(this.optionContainers.get(index) + "\n");
         }
      }

      return sb.toString();
   }
}
