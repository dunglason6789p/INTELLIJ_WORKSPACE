package org.maltparser.core.options;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashSet;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.options.option.BoolOption;
import org.maltparser.core.options.option.ClassOption;
import org.maltparser.core.options.option.EnumOption;
import org.maltparser.core.options.option.IntegerOption;
import org.maltparser.core.options.option.Option;
import org.maltparser.core.options.option.StringEnumOption;
import org.maltparser.core.options.option.StringOption;
import org.maltparser.core.options.option.UnaryOption;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OptionDescriptions {
   private final HashMap<String, OptionGroup> optionGroups = new HashMap();
   private final TreeSet<String> ambiguous = new TreeSet();
   private final HashMap<String, Option> unambiguousOptionMap = new HashMap();
   private final HashMap<String, Option> ambiguousOptionMap = new HashMap();
   private final HashMap<String, Option> flagOptionMap = new HashMap();

   public OptionDescriptions() {
   }

   public Option getOption(String optiongroup, String optionname) throws MaltChainedException {
      if (optionname != null && optionname.length() > 0) {
         Option option;
         if (this.ambiguous.contains(optionname.toLowerCase())) {
            if (optiongroup == null || optiongroup.length() <= 0) {
               throw new OptionException("The option name '" + optionname + "' is ambiguous use option group name to distinguish the option. ");
            }

            option = (Option)this.ambiguousOptionMap.get(optiongroup.toLowerCase() + "-" + optionname.toLowerCase());
            if (option == null) {
               throw new OptionException("The option '--" + optiongroup.toLowerCase() + "-" + optionname.toLowerCase() + " does not exist. ");
            }
         } else {
            option = (Option)this.unambiguousOptionMap.get(optionname.toLowerCase());
            if (option == null) {
               throw new OptionException("The option '--" + optionname.toLowerCase() + " doesn't exist. ");
            }
         }

         return option;
      } else {
         throw new OptionException("The option name '" + optionname + "' cannot be found");
      }
   }

   public Option getOption(String optionflag) throws MaltChainedException {
      Option option = (Option)this.flagOptionMap.get(optionflag);
      if (option == null) {
         throw new OptionException("The option flag -" + optionflag + " could not be found. ");
      } else {
         return option;
      }
   }

   public Set<Option> getSaveOptionSet() {
      Set<Option> optionToSave = new HashSet();
      Iterator i$ = this.unambiguousOptionMap.keySet().iterator();

      String optionname;
      while(i$.hasNext()) {
         optionname = (String)i$.next();
         if (((Option)this.unambiguousOptionMap.get(optionname)).getUsage() == 4) {
            optionToSave.add(this.unambiguousOptionMap.get(optionname));
         }
      }

      i$ = this.ambiguousOptionMap.keySet().iterator();

      while(i$.hasNext()) {
         optionname = (String)i$.next();
         if (((Option)this.ambiguousOptionMap.get(optionname)).getUsage() == 4) {
            optionToSave.add(this.ambiguousOptionMap.get(optionname));
         }
      }

      return optionToSave;
   }

   public TreeSet<String> getOptionGroupNameSet() {
      return new TreeSet(this.optionGroups.keySet());
   }

   protected Collection<Option> getOptionGroupList(String groupname) {
      return ((OptionGroup)this.optionGroups.get(groupname)).getOptionList();
   }

   public void parseOptionDescriptionXMLfile(URL url) throws MaltChainedException {
      if (url == null) {
         throw new OptionException("The URL to the default option file is null. ");
      } else {
         try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Element root = db.parse(url.openStream()).getDocumentElement();
            NodeList groups = root.getElementsByTagName("optiongroup");

            for(int i = 0; i < groups.getLength(); ++i) {
               Element group = (Element)groups.item(i);
               String groupname = group.getAttribute("groupname").toLowerCase();
               OptionGroup og = null;
               if (this.optionGroups.containsKey(groupname)) {
                  og = (OptionGroup)this.optionGroups.get(groupname);
               } else {
                  this.optionGroups.put(groupname, new OptionGroup(groupname));
                  og = (OptionGroup)this.optionGroups.get(groupname);
               }

               this.parseOptionsDescription(group, og);
            }

         } catch (IOException var10) {
            throw new OptionException("Can't find the file " + url.toString() + ".", var10);
         } catch (OptionException var11) {
            throw new OptionException("Problem parsing the file " + url.toString() + ". ", var11);
         } catch (ParserConfigurationException var12) {
            throw new OptionException("Problem parsing the file " + url.toString() + ". ", var12);
         } catch (SAXException var13) {
            throw new OptionException("Problem parsing the file " + url.toString() + ". ", var13);
         }
      }
   }

   private void parseOptionsDescription(Element group, OptionGroup og) throws MaltChainedException {
      NodeList options = group.getElementsByTagName("option");

      for(int i = 0; i < options.getLength(); ++i) {
         Element option = (Element)options.item(i);
         String optionname = option.getAttribute("name").toLowerCase();
         String optiontype = option.getAttribute("type").toLowerCase();
         String defaultValue = option.getAttribute("default");
         String usage = option.getAttribute("usage").toLowerCase();
         String flag = option.getAttribute("flag");
         NodeList shortdescs = option.getElementsByTagName("shortdesc");
         String shortdesctext = "";
         if (shortdescs.getLength() == 1) {
            Element shortdesc = (Element)shortdescs.item(0);
            shortdesctext = shortdesc.getTextContent();
         }

         Option op;
         if (!optiontype.equals("string") && !optiontype.equals("bool") && !optiontype.equals("integer") && !optiontype.equals("unary")) {
            if (optiontype.equals("class") || optiontype.equals("enum") || optiontype.equals("stringenum")) {
               op = og.getOption(optionname);
               if (op != null) {
                  if (op instanceof EnumOption && !optiontype.equals("enum")) {
                     throw new OptionException("The option name '" + optionname + "' for option group '" + og.getName() + "' already exists. The existing option is of enum type, but the new option is of '" + optiontype + "' type. ");
                  }

                  if (op instanceof ClassOption && !optiontype.equals("class")) {
                     throw new OptionException("The option name '" + optionname + "' for option group '" + og.getName() + "' already exists. The existing option is of class type, but the new option is of '" + optiontype + "' type. ");
                  }

                  if (op instanceof StringEnumOption && !optiontype.equals("stringenum")) {
                     throw new OptionException("The option name '" + optionname + "' for option group '" + og.getName() + "' already exists. The existing option is of urlenum type, but the new option is of '" + optiontype + "' type. ");
                  }
               }
            }
         } else {
            op = og.getOption(optionname);
            if (op != null) {
               throw new OptionException("The option name '" + optionname + "' for option group '" + og.getName() + "' already exists. It is only allowed to override the class and enum option type to add legal value. ");
            }
         }

         if (optiontype.equals("string")) {
            og.addOption(new StringOption(og, optionname, shortdesctext, flag, usage, defaultValue));
         } else if (optiontype.equals("bool")) {
            og.addOption(new BoolOption(og, optionname, shortdesctext, flag, usage, defaultValue));
         } else if (optiontype.equals("integer")) {
            og.addOption(new IntegerOption(og, optionname, shortdesctext, flag, usage, defaultValue));
         } else if (optiontype.equals("unary")) {
            og.addOption(new UnaryOption(og, optionname, shortdesctext, flag, usage));
         } else {
            NodeList legalvalues;
            Element legalvalue;
            int j;
            String legalvaluename;
            String url;
            if (optiontype.equals("enum")) {
               op = og.getOption(optionname);
               EnumOption eop = null;
               if (op == null) {
                  eop = new EnumOption(og, optionname, shortdesctext, flag, usage);
               } else if (op instanceof EnumOption) {
                  eop = (EnumOption)op;
               }

               legalvalues = option.getElementsByTagName("legalvalue");

               for(j = 0; j < legalvalues.getLength(); ++j) {
                  legalvalue = (Element)legalvalues.item(j);
                  legalvaluename = legalvalue.getAttribute("name");
                  url = legalvalue.getTextContent();
                  eop.addLegalValue(legalvaluename, url);
               }

               if (op == null) {
                  eop.setDefaultValue(defaultValue);
                  og.addOption(eop);
               }
            } else {
               String legalvaluetext;
               if (optiontype.equals("class")) {
                  op = og.getOption(optionname);
                  ClassOption cop = null;
                  if (op == null) {
                     cop = new ClassOption(og, optionname, shortdesctext, flag, usage);
                  } else if (op instanceof ClassOption) {
                     cop = (ClassOption)op;
                  }

                  legalvalues = option.getElementsByTagName("legalvalue");

                  for(j = 0; j < legalvalues.getLength(); ++j) {
                     legalvalue = (Element)legalvalues.item(j);
                     legalvaluename = legalvalue.getAttribute("name").toLowerCase();
                     url = legalvalue.getAttribute("class");
                     legalvaluetext = legalvalue.getTextContent();
                     cop.addLegalValue(legalvaluename, legalvaluetext, url);
                  }

                  if (op == null) {
                     cop.setDefaultValue(defaultValue);
                     og.addOption(cop);
                  }
               } else {
                  if (!optiontype.equals("stringenum")) {
                     throw new OptionException("Illegal option type found in the setting file. ");
                  }

                  op = og.getOption(optionname);
                  StringEnumOption ueop = null;
                  if (op == null) {
                     ueop = new StringEnumOption(og, optionname, shortdesctext, flag, usage);
                  } else if (op instanceof StringEnumOption) {
                     ueop = (StringEnumOption)op;
                  }

                  legalvalues = option.getElementsByTagName("legalvalue");

                  for(j = 0; j < legalvalues.getLength(); ++j) {
                     legalvalue = (Element)legalvalues.item(j);
                     legalvaluename = legalvalue.getAttribute("name").toLowerCase();
                     url = legalvalue.getAttribute("mapto");
                     legalvaluetext = legalvalue.getTextContent();
                     ueop.addLegalValue(legalvaluename, legalvaluetext, url);
                  }

                  if (op == null) {
                     ueop.setDefaultValue(defaultValue);
                     og.addOption(ueop);
                  }
               }
            }
         }
      }

   }

   public boolean hasOptions() {
      return this.optionGroups.size() > 0;
   }

   public void generateMaps() throws MaltChainedException {
      Iterator i$ = this.optionGroups.keySet().iterator();

      while(i$.hasNext()) {
         String groupname = (String)i$.next();
         OptionGroup og = (OptionGroup)this.optionGroups.get(groupname);
         Collection<Option> options = og.getOptionList();
         Iterator i$ = options.iterator();

         while(i$.hasNext()) {
            Option option = (Option)i$.next();
            Option co;
            if (this.ambiguous.contains(option.getName())) {
               option.setAmbiguous(true);
               this.ambiguousOptionMap.put(option.getGroup().getName() + "-" + option.getName(), option);
            } else if (!this.unambiguousOptionMap.containsKey(option.getName())) {
               this.unambiguousOptionMap.put(option.getName(), option);
            } else {
               co = (Option)this.unambiguousOptionMap.get(option.getName());
               this.unambiguousOptionMap.remove(co);
               co.setAmbiguous(true);
               option.setAmbiguous(true);
               this.ambiguous.add(option.getName());
               this.ambiguousOptionMap.put(co.getGroup().getName() + "-" + co.getName(), co);
               this.ambiguousOptionMap.put(option.getGroup().getName() + "-" + option.getName(), option);
            }

            if (option.getFlag() != null) {
               co = (Option)this.flagOptionMap.get(option.getFlag());
               if (co != null) {
                  this.flagOptionMap.remove(co);
                  co.setFlag((String)null);
                  option.setFlag((String)null);
                  if (SystemLogger.logger().isDebugEnabled()) {
                     SystemLogger.logger().debug("Ambiguous use of an option flag -> the option flag is removed for all ambiguous options\n");
                  }
               } else {
                  this.flagOptionMap.put(option.getFlag(), option);
               }
            }
         }
      }

   }

   public String toStringMaps() {
      StringBuilder sb = new StringBuilder();
      sb.append("UnambiguousOptionMap\n");
      Iterator i$ = (new TreeSet(this.unambiguousOptionMap.keySet())).iterator();

      String flag;
      while(i$.hasNext()) {
         flag = (String)i$.next();
         sb.append("   " + flag + "\n");
      }

      sb.append("AmbiguousSet\n");
      i$ = this.ambiguous.iterator();

      while(i$.hasNext()) {
         flag = (String)i$.next();
         sb.append("   " + flag + "\n");
      }

      sb.append("AmbiguousOptionMap\n");
      i$ = (new TreeSet(this.ambiguousOptionMap.keySet())).iterator();

      while(i$.hasNext()) {
         flag = (String)i$.next();
         sb.append("   " + flag + "\n");
      }

      sb.append("CharacterOptionMap\n");
      i$ = (new TreeSet(this.flagOptionMap.keySet())).iterator();

      while(i$.hasNext()) {
         flag = (String)i$.next();
         sb.append("   -" + flag + " -> " + ((Option)this.flagOptionMap.get(flag)).getName() + "\n");
      }

      return sb.toString();
   }

   public String toStringOptionGroup(String groupname) {
      OptionGroup.toStringSetting = 1;
      return ((OptionGroup)this.optionGroups.get(groupname)).toString() + "\n";
   }

   public String toString() {
      OptionGroup.toStringSetting = 0;
      StringBuilder sb = new StringBuilder();
      Iterator i$ = (new TreeSet(this.optionGroups.keySet())).iterator();

      while(i$.hasNext()) {
         String groupname = (String)i$.next();
         sb.append(((OptionGroup)this.optionGroups.get(groupname)).toString() + "\n");
      }

      return sb.toString();
   }
}
