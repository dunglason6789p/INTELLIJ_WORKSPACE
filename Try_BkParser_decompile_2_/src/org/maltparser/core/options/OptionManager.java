/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.options;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionDescriptions;
import org.maltparser.core.options.OptionException;
import org.maltparser.core.options.OptionGroup;
import org.maltparser.core.options.OptionValues;
import org.maltparser.core.options.option.ClassOption;
import org.maltparser.core.options.option.EnumOption;
import org.maltparser.core.options.option.Option;
import org.maltparser.core.options.option.StringEnumOption;
import org.maltparser.core.options.option.UnaryOption;
import org.maltparser.core.plugin.PluginLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OptionManager {
    public static final int DEFAULTVALUE = -1;
    private final OptionDescriptions optionDescriptions;
    private final OptionValues optionValues = new OptionValues();
    private static OptionManager uniqueInstance = new OptionManager();

    private OptionManager() {
        this.optionDescriptions = new OptionDescriptions();
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
        }
        Object value = this.optionValues.getOptionValue(containerIndex, option);
        if (value == null) {
            value = option.getDefaultValueObject();
        }
        return value;
    }

    public Object getOptionDefaultValue(String optiongroup, String optionname) throws MaltChainedException {
        Option option = this.optionDescriptions.getOption(optiongroup, optionname);
        return option.getDefaultValueObject();
    }

    public Object getOptionValueNoDefault(int containerIndex, String optiongroup, String optionname) throws MaltChainedException {
        Option option = this.optionDescriptions.getOption(optiongroup, optionname);
        if (containerIndex == -1) {
            return option.getDefaultValueObject();
        }
        return this.optionValues.getOptionValue(containerIndex, option);
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
        }
        Object ovalue = option.getValueObject(value);
        this.optionValues.addOptionValue(containerType, containerIndex, option, ovalue);
    }

    public int getNumberOfOptionValues(int containerIndex) {
        return this.optionValues.getNumberOfOptionValues(containerIndex);
    }

    public Set<Integer> getOptionContainerIndices() {
        return this.optionValues.getOptionContainerIndices();
    }

    public void loadOptions(int containerIndex, String fileName) throws MaltChainedException {
        try {
            this.loadOptions(containerIndex, new InputStreamReader((InputStream)new FileInputStream(fileName), "UTF-8"));
        }
        catch (FileNotFoundException e) {
            throw new OptionException("The saved option file '" + fileName + "' cannot be found. ", e);
        }
        catch (UnsupportedEncodingException e) {
            throw new OptionException("The charset is unsupported. ", e);
        }
    }

    public void loadOptions(int containerIndex, InputStreamReader isr) throws MaltChainedException {
        try {
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            Option option = null;
            Pattern tabPattern = Pattern.compile("\t");
            while ((line = br.readLine()) != null) {
                Object ovalue;
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
                this.optionValues.addOptionValue(0, containerIndex, option, ovalue);
            }
            br.close();
        }
        catch (ClassNotFoundException e) {
            throw new OptionException("The class cannot be found. ", e);
        }
        catch (NumberFormatException e) {
            throw new OptionException("Option container index isn't an integer value. ", e);
        }
        catch (IOException e) {
            throw new OptionException("Error when reading the saved options. ", e);
        }
    }

    public void saveOptions(String fileName) throws MaltChainedException {
        try {
            this.saveOptions(new OutputStreamWriter((OutputStream)new FileOutputStream(fileName), "UTF-8"));
        }
        catch (FileNotFoundException e) {
            throw new OptionException("The file '" + fileName + "' cannot be created. ", e);
        }
        catch (UnsupportedEncodingException e) {
            throw new OptionException("The charset 'UTF-8' is unsupported. ", e);
        }
    }

    public void saveOptions(OutputStreamWriter osw) throws MaltChainedException {
        try {
            BufferedWriter bw = new BufferedWriter(osw);
            Set<Option> optionToSave = this.optionDescriptions.getSaveOptionSet();
            Object value = null;
            for (Integer index : this.optionValues.getOptionContainerIndices()) {
                for (Option option : optionToSave) {
                    value = this.optionValues.getOptionValue(index, option);
                    if (value == null) {
                        value = option.getDefaultValueObject();
                    }
                    bw.append(index + "\t" + option.getGroup().getName() + "\t" + option.getName() + "\t" + value + "\n");
                }
            }
            bw.flush();
            bw.close();
        }
        catch (IOException e) {
            throw new OptionException("Error when saving the saved options. ", e);
        }
    }

    public void saveOptions(int containerIndex, String fileName) throws MaltChainedException {
        try {
            this.saveOptions(containerIndex, new OutputStreamWriter((OutputStream)new FileOutputStream(fileName), "UTF-8"));
        }
        catch (FileNotFoundException e) {
            throw new OptionException("The file '" + fileName + "' cannot be found.", e);
        }
        catch (UnsupportedEncodingException e) {
            throw new OptionException("The charset 'UTF-8' is unsupported. ", e);
        }
    }

    public void saveOptions(int containerIndex, OutputStreamWriter osw) throws MaltChainedException {
        try {
            BufferedWriter bw = new BufferedWriter(osw);
            Set<Option> optionToSave = this.optionDescriptions.getSaveOptionSet();
            Object value = null;
            for (Option option : optionToSave) {
                value = this.optionValues.getOptionValue(containerIndex, option);
                if (value == null) {
                    value = option.getDefaultValueObject();
                }
                bw.append(containerIndex + "\t" + option.getGroup().getName() + "\t" + option.getName() + "\t" + value + "\n");
            }
            bw.flush();
            bw.close();
        }
        catch (IOException e) {
            throw new OptionException("Error when saving the saved options.", e);
        }
    }

    public void generateMaps() throws MaltChainedException {
        this.optionDescriptions.generateMaps();
    }

    public boolean parseCommandLine(String argString, int containerIndex) throws MaltChainedException {
        return this.parseCommandLine(argString.split(" "), containerIndex);
    }

    public boolean parseCommandLine(String[] args, int containerIndex) throws MaltChainedException {
        if (args == null || args.length == 0) {
            return false;
        }
        int i = 0;
        HashMap<String, String> oldFlags = new HashMap<String, String>();
        oldFlags.put("llo", "lo");
        oldFlags.put("lso", "lo");
        oldFlags.put("lli", "li");
        oldFlags.put("lsi", "li");
        oldFlags.put("llx", "lx");
        oldFlags.put("lsx", "lx");
        oldFlags.put("llv", "lv");
        oldFlags.put("lsv", "lv");
        while (i < args.length) {
            Option option = null;
            String value = null;
            if (args[i].startsWith("--")) {
                String optiongroup;
                String optionstring;
                String optionname;
                if (args[i].length() == 2) {
                    throw new OptionException("The argument contains only '--', please check the user guide to see the correct format. ");
                }
                int indexEqualSign = args[i].indexOf(61);
                if (indexEqualSign != -1) {
                    value = args[i].substring(indexEqualSign + 1);
                    optionstring = args[i].substring(2, indexEqualSign);
                } else {
                    value = null;
                    optionstring = args[i].substring(2);
                }
                int indexMinusSign = optionstring.indexOf(45);
                if (indexMinusSign != -1) {
                    optionname = optionstring.substring(indexMinusSign + 1);
                    optiongroup = optionstring.substring(0, indexMinusSign);
                } else {
                    optiongroup = null;
                    optionname = optionstring;
                }
                option = this.optionDescriptions.getOption(optiongroup, optionname);
                if (option instanceof UnaryOption) {
                    value = "used";
                }
                ++i;
            } else if (args[i].startsWith("-")) {
                if (args[i].length() < 2) {
                    throw new OptionException("Wrong use of option flag '" + args[i] + "', please check the user guide to see the correct format. ");
                }
                String flag = "";
                flag = oldFlags.containsKey(args[i].substring(1)) ? (String)oldFlags.get(args[i].substring(1)) : args[i].substring(1);
                if (args[i].substring(1).equals("r")) {
                    throw new OptionException("The flag -r (root_handling) is replaced with two flags -nr (allow_root) and -ne (allow_reduce) since MaltParser 1.7. Read more about these changes in the user guide.");
                }
                option = this.optionDescriptions.getOption(flag);
                if (option instanceof UnaryOption) {
                    value = "used";
                } else if (args.length > ++i) {
                    value = args[i];
                } else {
                    throw new OptionException("Could not find the corresponding value for -" + option.getFlag() + ". ");
                }
                ++i;
            } else {
                throw new OptionException("The option should starts with a minus sign (-), error at argument '" + args[i] + "'");
            }
            Object optionvalue = option.getValueObject(value);
            this.optionValues.addOptionValue(2, containerIndex, option, optionvalue);
        }
        return true;
    }

    public void parseOptionInstanceXMLfile(String fileName) throws MaltChainedException {
        File file = new File(fileName);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Element root = db.parse(file).getDocumentElement();
            NodeList containers = root.getElementsByTagName("optioncontainer");
            for (int i = 0; i < containers.getLength(); ++i) {
                Element container = (Element)containers.item(i);
                this.parseOptionValues(container, i);
            }
        }
        catch (IOException e) {
            throw new OptionException("Can't find the file " + fileName + ". ", e);
        }
        catch (OptionException e) {
            throw new OptionException("Problem parsing the file " + fileName + ". ", e);
        }
        catch (ParserConfigurationException e) {
            throw new OptionException("Problem parsing the file " + fileName + ". ", e);
        }
        catch (SAXException e) {
            throw new OptionException("Problem parsing the file " + fileName + ". ", e);
        }
    }

    private void parseOptionValues(Element container, int containerIndex) throws MaltChainedException {
        NodeList optiongroups = container.getElementsByTagName("optiongroup");
        for (int i = 0; i < optiongroups.getLength(); ++i) {
            Element optiongroup = (Element)optiongroups.item(i);
            String groupname = optiongroup.getAttribute("groupname").toLowerCase();
            if (groupname == null) {
                throw new OptionException("The option group name is missing. ");
            }
            NodeList optionvalues = optiongroup.getElementsByTagName("option");
            for (int j = 0; j < optionvalues.getLength(); ++j) {
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
        if (containerIndex == -1) {
            for (String groupname : this.optionDescriptions.getOptionGroupNameSet()) {
                if (excludeGroups.contains(groupname)) continue;
                sb.append(groupname + "\n");
                for (Option option : this.optionDescriptions.getOptionGroupList(groupname)) {
                    int nSpaces = reservedSpaceForOptionName - option.getName().length();
                    if (nSpaces <= 1) {
                        nSpaces = 1;
                    }
                    sb.append(new Formatter().format("  %s (%4s)%" + nSpaces + "s %s\n", option.getName(), "-" + option.getFlag(), " ", option.getDefaultValueString()));
                }
            }
        } else {
            for (String groupname : this.optionDescriptions.getOptionGroupNameSet()) {
                if (excludeGroups.contains(groupname)) continue;
                sb.append(groupname + "\n");
                for (Option option : this.optionDescriptions.getOptionGroupList(groupname)) {
                    String value = this.optionValues.getOptionValueString(containerIndex, option);
                    int nSpaces = reservedSpaceForOptionName - option.getName().length();
                    if (nSpaces <= 1) {
                        nSpaces = 1;
                    }
                    if (value == null) {
                        sb.append(new Formatter().format("  %s (%4s)%" + nSpaces + "s %s\n", option.getName(), "-" + option.getFlag(), " ", option.getDefaultValueString()));
                        continue;
                    }
                    sb.append(new Formatter().format("  %s (%4s)%" + nSpaces + "s %s\n", option.getName(), "-" + option.getFlag(), " ", value));
                }
            }
        }
        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.optionDescriptions + "\n");
        sb.append(this.optionValues + "\n");
        return sb.toString();
    }
}

