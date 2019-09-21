/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.options;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import org.maltparser.core.options.OptionException;
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
        if (option.getName() == null || option.getName().equals("")) {
            throw new OptionException("The option name is null or contains the empty string. ");
        }
        if (this.options.containsKey(option.getName().toLowerCase())) {
            throw new OptionException("The option name already exists for that option group. ");
        }
        this.options.put(option.getName().toLowerCase(), option);
    }

    public Option getOption(String optionname) {
        return this.options.get(optionname);
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
        for (String value : new TreeSet<String>(this.options.keySet())) {
            sb.append(this.options.get(value).toString());
        }
        return sb.toString();
    }
}

