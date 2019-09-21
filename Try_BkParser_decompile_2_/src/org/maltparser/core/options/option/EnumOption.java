/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.options.option;

import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionException;
import org.maltparser.core.options.OptionGroup;
import org.maltparser.core.options.option.Option;

public class EnumOption
extends Option {
    private String defaultValue;
    private final SortedSet<String> legalValues = Collections.synchronizedSortedSet(new TreeSet());
    private final Map<String, String> legalValueDesc = Collections.synchronizedMap(new HashMap());

    public EnumOption(OptionGroup group, String name, String shortDescription, String flag, String usage) throws MaltChainedException {
        super(group, name, shortDescription, flag, usage);
    }

    @Override
    public Object getValueObject(String value) throws MaltChainedException {
        if (value == null) {
            return null;
        }
        if (this.legalValues.contains(value)) {
            return new String(value);
        }
        throw new OptionException("'" + value + "' is not a legal value for the '" + this.getName() + "' option. ");
    }

    @Override
    public Object getDefaultValueObject() throws MaltChainedException {
        return new String(this.defaultValue);
    }

    @Override
    public String getDefaultValueString() {
        return this.defaultValue.toString();
    }

    @Override
    public void setDefaultValue(String defaultValue) throws MaltChainedException {
        if (defaultValue == null) {
            if (this.legalValues.isEmpty()) {
                throw new OptionException("The default value of the '" + this.getName() + "' option is null and the legal value set is empty.");
            }
            this.defaultValue = this.legalValues.first();
        } else if (this.legalValues.contains(defaultValue.toLowerCase())) {
            this.defaultValue = defaultValue.toLowerCase();
        } else {
            throw new OptionException("The default value '" + defaultValue + "' for the '" + this.getName() + "' option is not a legal value. ");
        }
    }

    public void addLegalValue(String value, String desc) throws MaltChainedException {
        if (value == null || value.equals("")) {
            throw new OptionException("The legal value is missing for the '" + this.getName() + "' option. ");
        }
        if (this.legalValues.contains(value.toLowerCase())) {
            throw new OptionException("The legal value '" + value + "' already exists for the '" + this.getName() + "' option. ");
        }
        this.legalValues.add(value.toLowerCase());
        if (desc == null || desc.equals("")) {
            this.legalValueDesc.put(value.toLowerCase(), "Description is missing. ");
        } else {
            this.legalValueDesc.put(value.toLowerCase(), desc);
        }
    }

    public void addLegalValue(String value) throws MaltChainedException {
        this.addLegalValue(value, null);
    }

    @Override
    public String getStringRepresentation(Object value) {
        if (value instanceof String && this.legalValues.contains(value)) {
            return value.toString();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        Formatter formatter = new Formatter(sb);
        for (String value : this.legalValues) {
            formatter.format("%2s%-10s - %-20s\n", "", value, this.legalValueDesc.get(value));
        }
        sb.append("-----------------------------------------------------------------------------\n");
        return sb.toString();
    }
}

