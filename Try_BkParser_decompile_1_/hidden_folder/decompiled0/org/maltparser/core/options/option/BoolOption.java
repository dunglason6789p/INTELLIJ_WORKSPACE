/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.options.option;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionException;
import org.maltparser.core.options.OptionGroup;
import org.maltparser.core.options.option.Option;

public class BoolOption
extends Option {
    private Boolean defaultValue;

    public BoolOption(OptionGroup group, String name, String shortDescription, String flag, String usage, String defaultValue) throws MaltChainedException {
        super(group, name, shortDescription, flag, usage);
        this.setDefaultValue(defaultValue);
    }

    @Override
    public Object getValueObject(String value) throws MaltChainedException {
        if (value.equalsIgnoreCase("true")) {
            return new Boolean(true);
        }
        if (value.equalsIgnoreCase("false")) {
            return new Boolean(false);
        }
        throw new OptionException("Illegal boolean value '" + value + "' for the '" + this.getName() + "' option. ");
    }

    @Override
    public Object getDefaultValueObject() throws MaltChainedException {
        return new Boolean(this.defaultValue);
    }

    @Override
    public void setDefaultValue(String defaultValue) throws MaltChainedException {
        if (defaultValue.equalsIgnoreCase("true")) {
            this.defaultValue = true;
        } else if (defaultValue.equalsIgnoreCase("false")) {
            this.defaultValue = false;
        } else {
            throw new OptionException("Illegal boolean default value '" + defaultValue + "' for the '" + this.getName() + "' option. ");
        }
    }

    @Override
    public String getDefaultValueString() {
        return this.defaultValue.toString();
    }

    @Override
    public String getStringRepresentation(Object value) {
        if (value instanceof Boolean) {
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

