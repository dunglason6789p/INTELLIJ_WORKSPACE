package org.maltparser.core.options;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import org.maltparser.core.options.option.Option;

public class OptionGroup {
   private final String name;
   private final HashMap<String, Option> options;
   public static int toStringSetting = 0;
   public static final int WITHGROUPNAME = 0;
   public static final int NOGROUPNAME = 1;

   public OptionGroup(String name) {
      this.name = name;
      this.options = new HashMap();
   }

   public String getName() {
      return this.name;
   }

   public void addOption(Option option) throws OptionException {
      if (option.getName() != null && !option.getName().equals("")) {
         if (this.options.containsKey(option.getName().toLowerCase())) {
            throw new OptionException("The option name already exists for that option group. ");
         } else {
            this.options.put(option.getName().toLowerCase(), option);
         }
      } else {
         throw new OptionException("The option name is null or contains the empty string. ");
      }
   }

   public Option getOption(String optionname) {
      return (Option)this.options.get(optionname);
   }

   public Collection<Option> getOptionList() {
      return this.options.values();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      if (toStringSetting == 0) {
         sb.append("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
         sb.append("+ " + this.name + "\n");
         sb.append("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
      }

      Iterator i$ = (new TreeSet(this.options.keySet())).iterator();

      while(i$.hasNext()) {
         String value = (String)i$.next();
         sb.append(((Option)this.options.get(value)).toString());
      }

      return sb.toString();
   }
}
