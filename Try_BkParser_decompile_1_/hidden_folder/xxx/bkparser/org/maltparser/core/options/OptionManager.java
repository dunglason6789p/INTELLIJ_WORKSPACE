package org.maltparser.core.options;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.option.ClassOption;
import org.maltparser.core.options.option.EnumOption;
import org.maltparser.core.options.option.Option;
import org.maltparser.core.options.option.StringEnumOption;
import org.maltparser.core.options.option.UnaryOption;
import org.maltparser.core.plugin.PluginLoader;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OptionManager {
   public static final int DEFAULTVALUE = -1;
   private final OptionDescriptions optionDescriptions = new OptionDescriptions();
   private final OptionValues optionValues = new OptionValues();
   private static OptionManager uniqueInstance = new OptionManager();

   private OptionManager() {
   }

   public static OptionManager instance() {
      return uniqueInstance;
   }

   public void loadOptionDescriptionFile() throws MaltChainedException {
      this.optionDescriptions.parseOptionDescriptionXMLfile(this.getClass().getResource("/appdata/options.xml"));
   }

   public void loadOptionDescriptionFile(URL url) throws MaltChainedException {
      this.optionDescriptions.parseOptionDescriptionXMLfile(url);
   }

   public OptionDescriptions getOptionDescriptions() {
      return this.optionDescriptions;
   }

   public boolean hasOptions() {
      return this.optionDescriptions.hasOptions();
   }

   public Object getOptionValue(int containerIndex, String optiongroup, String optionname) throws MaltChainedException {
      Option option = this.optionDescriptions.getOption(optiongroup, optionname);
      if (containerIndex == -1) {
         return option.getDefaultValueObject();
      } else {
         Object value = this.optionValues.getOptionValue(containerIndex, option);
         if (value == null) {
            value = option.getDefaultValueObject();
         }

         return value;
      }
   }

   public Object getOptionDefaultValue(String optiongroup, String optionname) throws MaltChainedException {
      Option option = this.optionDescriptions.getOption(optiongroup, optionname);
      return option.getDefaultValueObject();
   }

   public Object getOptionValueNoDefault(int containerIndex, String optiongroup, String optionname) throws MaltChainedException {
      Option option = this.optionDescriptions.getOption(optiongroup, optionname);
      return containerIndex == -1 ? option.getDefaultValueObject() : this.optionValues.getOptionValue(containerIndex, option);
   }

   public String getOptionValueString(int containerIndex, String optiongroup, String optionname) throws MaltChainedException {
      Option option = this.optionDescriptions.getOption(optiongroup, optionname);
      String value = this.optionValues.getOptionValueString(containerIndex, option);
      if (value == null) {
         value = option.getDefaultValueString();
      }

      return value;
   }

   public String getOptionValueStringNoDefault(int containerIndex, String optiongroup, String optionname) throws MaltChainedException {
      return this.optionValues.getOptionValueString(containerIndex, this.optionDescriptions.getOption(optiongroup, optionname));
   }

   public void addLegalValue(String optiongroup, String optionname, String value, String desc, String target) throws MaltChainedException {
      Option option = this.optionDescriptions.getOption(optiongroup, optionname);
      if (option != null) {
         if (option instanceof EnumOption) {
            ((EnumOption)option).addLegalValue(value, desc);
         } else if (option instanceof ClassOption) {
            ((ClassOption)option).addLegalValue(value, desc, target);
         } else if (option instanceof StringEnumOption) {
            ((StringEnumOption)option).addLegalValue(value, desc, target);
         }
      }

   }

   public void overloadOptionValue(int containerIndex, String optiongroup, String optionname, String value) throws MaltChainedException {
      this.overloadOptionValue(containerIndex, 1, optiongroup, optionname, value);
   }

   public void overloadOptionValue(int containerIndex, int containerType, String optiongroup, String optionname, String value) throws MaltChainedException {
      Option option = this.optionDescriptions.getOption(optiongroup, optionname);
      if (value == null) {
         throw new OptionException("The option value is missing. ");
      } else {
         Object ovalue = option.getValueObject(value);
         this.optionValues.addOptionValue(containerType, containerIndex, option, ovalue);
      }
   }

   public int getNumberOfOptionValues(int containerIndex) {
      return this.optionValues.getNumberOfOptionValues(containerIndex);
   }

   public Set<Integer> getOptionContainerIndices() {
      return this.optionValues.getOptionContainerIndices();
   }

   public void loadOptions(int containerIndex, String fileName) throws MaltChainedException {
      try {
         this.loadOptions(containerIndex, new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
      } catch (FileNotFoundException var4) {
         throw new OptionException("The saved option file '" + fileName + "' cannot be found. ", var4);
      } catch (UnsupportedEncodingException var5) {
         throw new OptionException("The charset is unsupported. ", var5);
      }
   }

   public void loadOptions(int containerIndex, InputStreamReader isr) throws MaltChainedException {
      try {
         BufferedReader br = new BufferedReader(isr);
         String line = null;
         Option option = null;

         Object ovalue;
         for(Pattern tabPattern = Pattern.compile("\t"); (line = br.readLine()) != null; this.optionValues.addOptionValue(0, containerIndex, option, ovalue)) {
            String[] items = tabPattern.split(line);
            if (items.length < 3 || items.length > 4) {
               throw new OptionException("Could not load the saved option. ");
            }

            option = this.optionDescriptions.getOption(items[1], items[2]);
            if (items.length == 3) {
               ovalue = new String("");
            } else if (option instanceof ClassOption) {
               if (items[3].startsWith("class ")) {
                  Class<?> clazz = null;
                  if (PluginLoader.instance() != null) {
                     clazz = PluginLoader.instance().getClass(items[3].substring(6));
                  }

                  if (clazz == null) {
                     clazz = Class.forName(items[3].substring(6));
                  }

                  ovalue = option.getValueObject(((ClassOption)option).getLegalValueString(clazz));
               } else {
                  ovalue = option.getValueObject(items[3]);
               }
            } else {
               ovalue = option.getValueObject(items[3]);
            }
         }

         br.close();
      } catch (ClassNotFoundException var10) {
         throw new OptionException("The class cannot be found. ", var10);
      } catch (NumberFormatException var11) {
         throw new OptionException("Option container index isn't an integer value. ", var11);
      } catch (IOException var12) {
         throw new OptionException("Error when reading the saved options. ", var12);
      }
   }

   public void saveOptions(String fileName) throws MaltChainedException {
      try {
         this.saveOptions(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
      } catch (FileNotFoundException var3) {
         throw new OptionException("The file '" + fileName + "' cannot be created. ", var3);
      } catch (UnsupportedEncodingException var4) {
         throw new OptionException("The charset 'UTF-8' is unsupported. ", var4);
      }
   }

   public void saveOptions(OutputStreamWriter osw) throws MaltChainedException {
      try {
         BufferedWriter bw = new BufferedWriter(osw);
         Set<Option> optionToSave = this.optionDescriptions.getSaveOptionSet();
         Object value = null;
         Iterator i$ = this.optionValues.getOptionContainerIndices().iterator();

         while(i$.hasNext()) {
            Integer index = (Integer)i$.next();

            Option option;
            for(Iterator i$ = optionToSave.iterator(); i$.hasNext(); bw.append(index + "\t" + option.getGroup().getName() + "\t" + option.getName() + "\t" + value + "\n")) {
               option = (Option)i$.next();
               value = this.optionValues.getOptionValue(index, option);
               if (value == null) {
                  value = option.getDefaultValueObject();
               }
            }
         }

         bw.flush();
         bw.close();
      } catch (IOException var9) {
         throw new OptionException("Error when saving the saved options. ", var9);
      }
   }

   public void saveOptions(int containerIndex, String fileName) throws MaltChainedException {
      try {
         this.saveOptions(containerIndex, new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
      } catch (FileNotFoundException var4) {
         throw new OptionException("The file '" + fileName + "' cannot be found.", var4);
      } catch (UnsupportedEncodingException var5) {
         throw new OptionException("The charset 'UTF-8' is unsupported. ", var5);
      }
   }

   public void saveOptions(int containerIndex, OutputStreamWriter osw) throws MaltChainedException {
      try {
         BufferedWriter bw = new BufferedWriter(osw);
         Set<Option> optionToSave = this.optionDescriptions.getSaveOptionSet();
         Object value = null;

         Option option;
         for(Iterator i$ = optionToSave.iterator(); i$.hasNext(); bw.append(containerIndex + "\t" + option.getGroup().getName() + "\t" + option.getName() + "\t" + value + "\n")) {
            option = (Option)i$.next();
            value = this.optionValues.getOptionValue(containerIndex, option);
            if (value == null) {
               value = option.getDefaultValueObject();
            }
         }

         bw.flush();
         bw.close();
      } catch (IOException var8) {
         throw new OptionException("Error when saving the saved options.", var8);
      }
   }

   public void generateMaps() throws MaltChainedException {
      this.optionDescriptions.generateMaps();
   }

   public boolean parseCommandLine(String argString, int containerIndex) throws MaltChainedException {
      return this.parseCommandLine(argString.split(" "), containerIndex);
   }

   public boolean parseCommandLine(String[] args, int containerIndex) throws MaltChainedException {
      if (args != null && args.length != 0) {
         int i = 0;
         HashMap<String, String> oldFlags = new HashMap();
         oldFlags.put("llo", "lo");
         oldFlags.put("lso", "lo");
         oldFlags.put("lli", "li");
         oldFlags.put("lsi", "li");
         oldFlags.put("llx", "lx");
         oldFlags.put("lsx", "lx");
         oldFlags.put("llv", "lv");
         oldFlags.put("lsv", "lv");

         while(i < args.length) {
            Option option = null;
            String value = null;
            String flag;
            if (args[i].startsWith("--")) {
               if (args[i].length() == 2) {
                  throw new OptionException("The argument contains only '--', please check the user guide to see the correct format. ");
               }

               int indexEqualSign = args[i].indexOf(61);
               if (indexEqualSign != -1) {
                  value = args[i].substring(indexEqualSign + 1);
                  flag = args[i].substring(2, indexEqualSign);
               } else {
                  value = null;
                  flag = args[i].substring(2);
               }

               int indexMinusSign = flag.indexOf(45);
               String optiongroup;
               String optionname;
               if (indexMinusSign != -1) {
                  optionname = flag.substring(indexMinusSign + 1);
                  optiongroup = flag.substring(0, indexMinusSign);
               } else {
                  optiongroup = null;
                  optionname = flag;
               }

               option = this.optionDescriptions.getOption(optiongroup, optionname);
               if (option instanceof UnaryOption) {
                  value = "used";
               }

               ++i;
            } else {
               if (!args[i].startsWith("-")) {
                  throw new OptionException("The option should starts with a minus sign (-), error at argument '" + args[i] + "'");
               }

               if (args[i].length() < 2) {
                  throw new OptionException("Wrong use of option flag '" + args[i] + "', please check the user guide to see the correct format. ");
               }

               flag = "";
               if (oldFlags.containsKey(args[i].substring(1))) {
                  flag = (String)oldFlags.get(args[i].substring(1));
               } else {
                  flag = args[i].substring(1);
               }

               if (args[i].substring(1).equals("r")) {
                  throw new OptionException("The flag -r (root_handling) is replaced with two flags -nr (allow_root) and -ne (allow_reduce) since MaltParser 1.7. Read more about these changes in the user guide.");
               }

               option = this.optionDescriptions.getOption(flag);
               if (option instanceof UnaryOption) {
                  value = "used";
               } else {
                  ++i;
                  if (args.length <= i) {
                     throw new OptionException("Could not find the corresponding value for -" + option.getFlag() + ". ");
                  }

                  value = args[i];
               }

               ++i;
            }

            Object optionvalue = option.getValueObject(value);
            this.optionValues.addOptionValue(2, containerIndex, option, optionvalue);
         }

         return true;
      } else {
         return false;
      }
   }

   public void parseOptionInstanceXMLfile(String fileName) throws MaltChainedException {
      File file = new File(fileName);

      try {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         Element root = db.parse(file).getDocumentElement();
         NodeList containers = root.getElementsByTagName("optioncontainer");

         for(int i = 0; i < containers.getLength(); ++i) {
            Element container = (Element)containers.item(i);
            this.parseOptionValues(container, i);
         }

      } catch (IOException var9) {
         throw new OptionException("Can't find the file " + fileName + ". ", var9);
      } catch (OptionException var10) {
         throw new OptionException("Problem parsing the file " + fileName + ". ", var10);
      } catch (ParserConfigurationException var11) {
         throw new OptionException("Problem parsing the file " + fileName + ". ", var11);
      } catch (SAXException var12) {
         throw new OptionException("Problem parsing the file " + fileName + ". ", var12);
      }
   }

   private void parseOptionValues(Element container, int containerIndex) throws MaltChainedException {
      NodeList optiongroups = container.getElementsByTagName("optiongroup");

      for(int i = 0; i < optiongroups.getLength(); ++i) {
         Element optiongroup = (Element)optiongroups.item(i);
         String groupname = optiongroup.getAttribute("groupname").toLowerCase();
         if (groupname == null) {
            throw new OptionException("The option group name is missing. ");
         }

         NodeList optionvalues = optiongroup.getElementsByTagName("option");

         for(int j = 0; j < optionvalues.getLength(); ++j) {
            Element optionvalue = (Element)optionvalues.item(j);
            String optionname = optionvalue.getAttribute("name").toLowerCase();
            String value = optionvalue.getAttribute("value");
            if (optionname == null) {
               throw new OptionException("The option name is missing. ");
            }

            Option option = this.optionDescriptions.getOption(groupname, optionname);
            if (option instanceof UnaryOption) {
               value = "used";
            }

            if (value == null) {
               throw new OptionException("The option value is missing. ");
            }

            Object ovalue = option.getValueObject(value);
            this.optionValues.addOptionValue(3, containerIndex, option, ovalue);
         }
      }

   }

   public String toStringPrettyValues(int containerIndex, Set<String> excludeGroups) throws MaltChainedException {
      int reservedSpaceForOptionName = 30;
      OptionGroup.toStringSetting = 0;
      StringBuilder sb = new StringBuilder();
      Iterator i$;
      String groupname;
      Iterator i$;
      Option option;
      if (containerIndex == -1) {
         i$ = this.optionDescriptions.getOptionGroupNameSet().iterator();

         while(true) {
            do {
               if (!i$.hasNext()) {
                  return sb.toString();
               }

               groupname = (String)i$.next();
            } while(excludeGroups.contains(groupname));

            sb.append(groupname + "\n");

            int nSpaces;
            for(i$ = this.optionDescriptions.getOptionGroupList(groupname).iterator(); i$.hasNext(); sb.append((new Formatter()).format("  %s (%4s)%" + nSpaces + "s %s\n", option.getName(), "-" + option.getFlag(), " ", option.getDefaultValueString()))) {
               option = (Option)i$.next();
               nSpaces = reservedSpaceForOptionName - option.getName().length();
               if (nSpaces <= 1) {
                  nSpaces = 1;
               }
            }
         }
      } else {
         i$ = this.optionDescriptions.getOptionGroupNameSet().iterator();

         while(true) {
            do {
               if (!i$.hasNext()) {
                  return sb.toString();
               }

               groupname = (String)i$.next();
            } while(excludeGroups.contains(groupname));

            sb.append(groupname + "\n");
            i$ = this.optionDescriptions.getOptionGroupList(groupname).iterator();

            while(i$.hasNext()) {
               option = (Option)i$.next();
               String value = this.optionValues.getOptionValueString(containerIndex, option);
               int nSpaces = reservedSpaceForOptionName - option.getName().length();
               if (nSpaces <= 1) {
                  nSpaces = 1;
               }

               if (value == null) {
                  sb.append((new Formatter()).format("  %s (%4s)%" + nSpaces + "s %s\n", option.getName(), "-" + option.getFlag(), " ", option.getDefaultValueString()));
               } else {
                  sb.append((new Formatter()).format("  %s (%4s)%" + nSpaces + "s %s\n", option.getName(), "-" + option.getFlag(), " ", value));
               }
            }
         }
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.optionDescriptions + "\n");
      sb.append(this.optionValues + "\n");
      return sb.toString();
   }
}
