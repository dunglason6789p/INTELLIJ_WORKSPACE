/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.options.option;

import java.util.Formatter;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionException;
import org.maltparser.core.options.OptionGroup;

public abstract class Option
implements Comparable<Option> {
    public static final int NONE = 0;
    public static final int TRAIN = 1;
    public static final int PROCESS = 2;
    public static final int BOTH = 3;
    public static final int SAVE = 4;
    private final OptionGroup group;
    private final String name;
    private final String shortDescription;
    private String flag;
    private int usage;
    private boolean ambiguous;

    public Option(OptionGroup group, String name, String shortDescription, String flag, String usage) throws MaltChainedException {
        this.group = group;
        if (name == null || name.length() == 0) {
            throw new OptionException("The option name has no value. ");
        }
        this.name = name.toLowerCase();
        this.shortDescription = shortDescription;
        this.setFlag(flag);
        this.setUsage(usage);
        this.setAmbiguous(false);
    }

    public abstract Object getValueObject(String var1) throws MaltChainedException;

    public abstract Object getDefaultValueObject() throws MaltChainedException;

    public abstract String getDefaultValueString();

    public abstract void setDefaultValue(String var1) throws MaltChainedException;

    public abstract String getStringRepresentation(Object var1);

    public OptionGroup getGroup() {
        return this.group;
    }

    public String getName() {
        return this.name;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }

    public String getFlag() {
        return this.flag;
    }

    public void setFlag(String flag) throws MaltChainedException {
        this.flag = flag == null ? null : flag;
    }

    public int getUsage() {
        return this.usage;
    }

    public void setUsage(String usage) throws MaltChainedException {
        if (usage == null || usage.equals("") || usage.toLowerCase().equals("none")) {
            this.usage = 0;
        } else if (usage.toLowerCase().equals("train")) {
            this.usage = 1;
        } else if (usage.toLowerCase().equals("process")) {
            this.usage = 2;
        } else if (usage.toLowerCase().equals("both")) {
            this.usage = 3;
        } else if (usage.toLowerCase().equals("save")) {
            this.usage = 4;
        } else {
            throw new OptionException("Illegal use of the usage attibute value: " + usage + " for the '" + this.getName() + "' option. ");
        }
    }

    public void setUsage(int usage) throws MaltChainedException {
        if (usage < 0 || usage > 4) {
            throw new OptionException("Illegal use of the usage attibute value: " + usage + " for the '" + this.getName() + "' option. ");
        }
        this.usage = usage;
    }

    public boolean isAmbiguous() {
        return this.ambiguous;
    }

    public void setAmbiguous(boolean ambiguous) {
        this.ambiguous = ambiguous;
    }

    @Override
    public int compareTo(Option o) {
        if (this.group.getName().equals(o.group.getName())) {
            return this.name.compareTo(o.getName());
        }
        return this.group.getName().compareTo(o.group.getName());
    }

    public String toString() {
        int splitsize = 45;
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("%-20s ", this.getName());
        if (this.isAmbiguous()) {
            formatter.format("*", new Object[0]);
        } else {
            sb.append(" ");
        }
        if (this.getFlag() != null) {
            formatter.format("(%4s) : ", "-" + this.getFlag());
        } else {
            sb.append("       : ");
        }
        int r = this.shortDescription.length() / splitsize;
        for (int i = 0; i <= r; ++i) {
            if (this.shortDescription.substring(splitsize * i).length() <= splitsize) {
                formatter.format((i == 0 ? "%s" : "%28s") + "%-45s\n", "", this.shortDescription.substring(splitsize * i));
                continue;
            }
            formatter.format((i == 0 ? "%s" : "%28s") + "%-45s\n", "", this.shortDescription.substring(splitsize * i, splitsize * i + splitsize));
        }
        return sb.toString();
    }
}

