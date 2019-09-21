/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.options.option;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionGroup;
import org.maltparser.core.options.option.Option;

public class UnaryOption
extends Option {
    public UnaryOption(OptionGroup group, String name, String shortDescription, String flag, String usage) throws MaltChainedException {
        super(group, name, shortDescription, flag, usage);
    }

    @Override
    public Object getValueObject(String value) throws MaltChainedException {
        if (value.equalsIgnoreCase("used")) {
            return new Boolean(true);
        }
        return null;
    }

    @Override
    public Object getDefaultValueObject() throws MaltChainedException {
        return null;
    }

    @Override
    public String getDefaultValueString() {
        return null;
    }

    @Override
    public String getStringRepresentation(Object value) {
        return null;
    }

    @Override
    public void setDefaultValue(String defaultValue) throws MaltChainedException {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("-----------------------------------------------------------------------------\n");
        return sb.toString();
    }
}

