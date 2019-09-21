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

public class EnumOption extends Option {
   private String defaultValue;
   private final SortedSet<String> legalValues = Collections.synchronizedSortedSet(new TreeSet());
   private final Map<String, String> legalValueDesc = Collections.synchronizedMap(new HashMap());

   public EnumOption(OptionGroup group, String name, String shortDescription, String flag, String usage) throws MaltChainedException {
      super(group, name, shortDescription, flag, usage);
   }

   public Object getValueObject(String value) throws MaltChainedException {
      if (value == null) {
         return null;
      } else if (this.legalValues.contains(value)) {
         return new String(value);
      } else {
         throw new OptionException("'" + value + "' is not a legal value for the '" + this.getName() + "' option. ");
      }
   }

   public Object getDefaultValueObject() throws MaltChainedException {
      return new String(this.defaultValue);
   }

   public String getDefaultValueString() {
      return this.defaultValue.toString();
   }

   public void setDefaultValue(String defaultValue) throws MaltChainedException {
      if (defaultValue == null) {
         if (this.legalValues.isEmpty()) {
            throw new OptionException("The default value of the '" + this.getName() + "' option is null and the legal value set is empty.");
         }

         this.defaultValue = (String)this.legalValues.first();
      } else {
         if (!this.legalValues.contains(defaultValue.toLowerCase())) {
            throw new OptionException("The default value '" + defaultValue + "' for the '" + this.getName() + "' option is not a legal value. ");
         }

         this.defaultValue = defaultValue.toLowerCase();
      }

   }

   public void addLegalValue(String value, String desc) throws MaltChainedException {
      if (value != null && !value.equals("")) {
         if (this.legalValues.contains(value.toLowerCase())) {
            throw new OptionException("The legal value '" + value + "' already exists for the '" + this.getName() + "' option. ");
         } else {
            this.legalValues.add(value.toLowerCase());
            if (desc != null && !desc.equals("")) {
               this.legalValueDesc.put(value.toLowerCase(), desc);
            } else {
               this.legalValueDesc.put(value.toLowerCase(), "Description is missing. ");
            }

         }
      } else {
         throw new OptionException("The legal value is missing for the '" + this.getName() + "' option. ");
      }
   }

   public void addLegalValue(String value) throws MaltChainedException {
      this.addLegalValue(value, (String)null);
   }

   public String getStringRepresentation(Object value) {
      return value instanceof String && this.legalValues.contains(value) ? value.toString() : null;
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
