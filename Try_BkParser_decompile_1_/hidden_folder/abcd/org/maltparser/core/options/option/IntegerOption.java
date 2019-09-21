/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.options.option;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionException;
import org.maltparser.core.options.OptionGroup;
import org.maltparser.core.options.option.Option;

public class IntegerOption
extends Option {
    private int defaultValue = 0;

    public IntegerOption(OptionGroup group, String name, String shortDescription, String flag, String usage, String defaultValue) throws MaltChainedException {
        super(group, name, shortDescription, flag, usage);
        this.setDefaultValue(defaultValue);
    }

    @Override
    public Object getValueObject(String value) throws MaltChainedException {
        try {
            return new Integer(Integer.parseInt(value));
        }
        catch (NumberFormatException e) {
            throw new OptionException("Illegal integer value '" + value + "' for the '" + this.getName() + "' option. ", e);
        }
    }

    @Override
    public Object getDefaultValueObject() throws MaltChainedException {
        return new Integer(this.defaultValue);
    }

    @Override
    public void setDefaultValue(String defaultValue) throws MaltChainedException {
        try {
            this.defaultValue = Integer.parseInt(defaultValue);
        }
        catch (NumberFormatException e) {
            throw new OptionException("Illegal integer default value '" + defaultValue + "' for the '" + this.getName() + "' option. ", e);
        }
    }

    @Override
    public String getDefaultValueString() {
        return Integer.toString(this.defaultValue);
    }

    @Override
    public String getStringRepresentation(Object value) {
        if (value instanceof Integer) {
            return value.toString();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("-----------------------------------------------------------------------------\n");
        return sb.toString();
    }
}

