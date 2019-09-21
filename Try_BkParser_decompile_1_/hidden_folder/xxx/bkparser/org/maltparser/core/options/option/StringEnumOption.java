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

public class StringEnumOption extends Option {
   private String defaultValue;
   private final SortedSet<String> legalValues = Collections.synchronizedSortedSet(new TreeSet());
   private final Map<String, String> legalValueDesc = Collections.synchronizedMap(new HashMap());
   private final Map<String, String> valueMapto = Collections.synchronizedMap(new HashMap());
   private final Map<String, String> maptoValue = Collections.synchronizedMap(new HashMap());

   public StringEnumOption(OptionGroup group, String name, String shortDescription, String flag, String usage) throws MaltChainedException {
      super(group, name, shortDescription, flag, usage);
   }

   public Object getValueObject(String value) throws MaltChainedException {
      if (value == null) {
         return null;
      } else {
         return this.legalValues.contains(value) ? new String((String)this.valueMapto.get(value)) : new String(value);
      }
   }

   public Object getDefaultValueObject() throws MaltChainedException {
      return new String(this.defaultValue);
   }

   public String getLegalValueString(String value) throws MaltChainedException {
      return new String((String)this.maptoValue.get(value));
   }

   public String getLegalValueMapToString(String value) throws MaltChainedException {
      return new String((String)this.valueMapto.get(value));
   }

   public void setDefaultValue(String defaultValue) throws MaltChainedException {
      if (defaultValue == null) {
         if (this.legalValues.isEmpty()) {
            throw new OptionException("The default value is null and the legal value set is empty for the '" + this.getName() + "' option. ");
         }

         this.defaultValue = (String)this.valueMapto.get(((TreeSet)this.valueMapto.keySet()).first());
      } else if (this.legalValues.contains(defaultValue.toLowerCase())) {
         this.defaultValue = (String)this.valueMapto.get(defaultValue.toLowerCase());
      } else {
         if (!defaultValue.equals("")) {
            throw new OptionException("The default value '" + defaultValue + "' for the '" + this.getName() + "' option is not a legal value. ");
         }

         this.defaultValue = defaultValue;
      }

   }

   public String getDefaultValueString() {
      return this.defaultValue.toString();
   }

   public String getMapto(String value) {
      return new String((String)this.valueMapto.get(value));
   }

   public void addLegalValue(String value, String desc, String mapto) throws MaltChainedException {
      if (value != null && !value.equals("")) {
         if (this.legalValues.contains(value.toLowerCase())) {
            throw new OptionException("The legal value " + value + " already exists for the option " + this.getName() + ". ");
         } else {
            this.legalValues.add(value.toLowerCase());
            if (desc != null && !desc.equals("")) {
               this.legalValueDesc.put(value.toLowerCase(), desc);
            } else {
               this.legalValueDesc.put(value.toLowerCase(), "Description is missing. ");
            }

            if (mapto != null && !mapto.equals("")) {
               this.valueMapto.put(value, mapto);
               this.maptoValue.put(mapto, value);
            } else {
               throw new OptionException("A mapto value is missing for the option " + this.getName() + ". ");
            }
         }
      } else {
         throw new OptionException("The legal value is missing for the option " + this.getName() + ".");
      }
   }

   public String getStringRepresentation(Object value) {
      if (value instanceof String) {
         return this.legalValues.contains(value) ? (String)this.valueMapto.get(value) : value.toString();
      } else {
         return null;
      }
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

      return sb.toString();
   }
}
