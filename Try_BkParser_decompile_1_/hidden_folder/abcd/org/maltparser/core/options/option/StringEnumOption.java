/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.options.option;

import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionException;
import org.maltparser.core.options.OptionGroup;
import org.maltparser.core.options.option.Option;

public class StringEnumOption
extends Option {
    private String defaultValue;
    private final SortedSet<String> legalValues = Collections.synchronizedSortedSet(new TreeSet());
    private final Map<String, String> legalValueDesc = Collections.synchronizedMap(new HashMap());
    private final Map<String, String> valueMapto = Collections.synchronizedMap(new HashMap());
    private final Map<String, String> maptoValue = Collections.synchronizedMap(new HashMap());

    public StringEnumOption(OptionGroup group, String name, String shortDescription, String flag, String usage) throws MaltChainedException {
        super(group, name, shortDescription, flag, usage);
    }

    @Override
    public Object getValueObject(String value) throws MaltChainedException {
        if (value == null) {
            return null;
        }
        if (this.legalValues.contains(value)) {
            return new String(this.valueMapto.get(value));
        }
        return new String(value);
    }

    @Override
    public Object getDefaultValueObject() throws MaltChainedException {
        return new String(this.defaultValue);
    }

    public String getLegalValueString(String value) throws MaltChainedException {
        return new String(this.maptoValue.get(value));
    }

    public String getLegalValueMapToString(String value) throws MaltChainedException {
        return new String(this.valueMapto.get(value));
    }

    @Override
    public void setDefaultValue(String defaultValue) throws MaltChainedException {
        if (defaultValue == null) {
            if (this.legalValues.isEmpty()) {
                throw new OptionException("The default value is null and the legal value set is empty for the '" + this.getName() + "' option. ");
            }
            this.defaultValue = this.valueMapto.get(((TreeSet)this.valueMapto.keySet()).first());
        } else if (this.legalValues.contains(defaultValue.toLowerCase())) {
            this.defaultValue = this.valueMapto.get(defaultValue.toLowerCase());
        } else if (defaultValue.equals("")) {
            this.defaultValue = defaultValue;
        } else {
            throw new OptionException("The default value '" + defaultValue + "' for the '" + this.getName() + "' option is not a legal value. ");
        }
    }

    @Override
    public String getDefaultValueString() {
        return this.defaultValue.toString();
    }

    public String getMapto(String value) {
        return new String(this.valueMapto.get(value));
    }

    public void addLegalValue(String value, String desc, String mapto) throws MaltChainedException {
        if (value == null || value.equals("")) {
            throw new OptionException("The legal value is missing for the option " + this.getName() + ".");
        }
        if (this.legalValues.contains(value.toLowerCase())) {
            throw new OptionException("The legal value " + value + " already exists for the option " + this.getName() + ". ");
        }
        this.legalValues.add(value.toLowerCase());
        if (desc == null || desc.equals("")) {
            this.legalValueDesc.put(value.toLowerCase(), "Description is missing. ");
        } else {
            this.legalValueDesc.put(value.toLowerCase(), desc);
        }
        if (mapto == null || mapto.equals("")) {
            throw new OptionException("A mapto value is missing for the option " + this.getName() + ". ");
        }
        this.valueMapto.put(value, mapto);
        this.maptoValue.put(mapto, value);
    }

    @Override
    public String getStringRepresentation(Object value) {
        if (value instanceof String) {
            if (this.legalValues.contains(value)) {
                return this.valueMapto.get(value);
            }
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
        return sb.toString();
    }
}

