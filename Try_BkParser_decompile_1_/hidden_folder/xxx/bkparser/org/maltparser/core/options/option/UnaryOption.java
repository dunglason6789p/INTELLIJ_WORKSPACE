package org.maltparser.core.options.option;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionGroup;

public class UnaryOption extends Option {
   public UnaryOption(OptionGroup group, String name, String shortDescription, String flag, String usage) throws MaltChainedException {
      super(group, name, shortDescription, flag, usage);
   }

   public Object getValueObject(String value) throws MaltChainedException {
      return value.equalsIgnoreCase("used") ? new Boolean(true) : null;
   }

   public Object getDefaultValueObject() throws MaltChainedException {
      return null;
   }

   public String getDefaultValueString() {
      return null;
   }

   public String getStringRepresentation(Object value) {
      return null;
   }

   public void setDefaultValue(String defaultValue) throws MaltChainedException {
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(super.toString());
      sb.append("-----------------------------------------------------------------------------\n");
      return sb.toString();
   }
}
