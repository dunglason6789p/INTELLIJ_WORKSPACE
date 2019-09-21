/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.options.option;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionGroup;
import org.maltparser.core.options.option.Option;

public class StringOption
extends Option {
    private String defaultValue;

    public StringOption(OptionGroup group, String name, String shortDescription, String flag, String usage, String defaultValue) throws MaltChainedException {
        super(group, name, shortDescription, flag, usage);
        this.setDefaultValue(defaultValue);
    }

    @Override
    public Object getValueObject(String value) throws MaltChainedException {
        return new String(value);
    }

    @Override
    public Object getDefaultValueObject() throws MaltChainedException {
        return new String(this.defaultValue);
    }

    @Override
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getDefaultValueString() {
        return this.defaultValue;
    }

    @Override
    public String getStringRepresentation(Object value) {
        if (value instanceof String) {
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

