package org.maltparser.core.options.option;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionGroup;

public class StringOption extends Option {
   private String defaultValue;

   public StringOption(OptionGroup group, String name, String shortDescription, String flag, String usage, String defaultValue) throws MaltChainedException {
      super(group, name, shortDescription, flag, usage);
      this.setDefaultValue(defaultValue);
   }

   public Object getValueObject(String value) throws MaltChainedException {
      return new String(value);
   }

   public Object getDefaultValueObject() throws MaltChainedException {
      return new String(this.defaultValue);
   }

   public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
   }

   public String getDefaultValueString() {
      return this.defaultValue;
   }

   public String getStringRepresentation(Object value) {
      return value instanceof String ? value.toString() : null;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(super.toString());
      sb.append("-----------------------------------------------------------------------------\n");
      return sb.toString();
   }
}
