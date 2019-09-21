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
import org.maltparser.core.plugin.PluginLoader;

public class ClassOption
extends Option {
    private Class<?> defaultValue;
    private final SortedSet<String> legalValues = Collections.synchronizedSortedSet(new TreeSet());
    private final Map<String, String> legalValueDesc = Collections.synchronizedMap(new HashMap());
    private final Map<String, Class<?>> legalValueClass = Collections.synchronizedMap(new HashMap());
    private final Map<Class<?>, String> classLegalValues = Collections.synchronizedMap(new HashMap());

    public ClassOption(OptionGroup group, String name, String shortDescription, String flag, String usage) throws MaltChainedException {
        super(group, name, shortDescription, flag, usage);
    }

    @Override
    public Object getValueObject(String value) throws MaltChainedException {
        if (value == null) {
            return null;
        }
        if (this.legalValues.contains(value)) {
            return this.legalValueClass.get(value);
        }
        throw new OptionException("'" + value + "' is not a legal value for the '" + this.getName() + "' option. ");
    }

    @Override
    public Object getDefaultValueObject() throws OptionException {
        return this.defaultValue;
    }

    public String getLegalValueString(Class<?> clazz) throws MaltChainedException {
        return this.classLegalValues.get(clazz);
    }

    @Override
    public void setDefaultValue(String defaultValue) throws MaltChainedException {
        if (defaultValue == null) {
            if (this.legalValues.isEmpty()) {
                throw new OptionException("The default value is null and the legal value set is empty for the '" + this.getName() + "' option. ");
            }
            this.defaultValue = this.legalValueClass.get(((TreeSet)this.legalValueClass.keySet()).first());
        } else if (this.legalValues.contains(defaultValue.toLowerCase())) {
            this.defaultValue = this.legalValueClass.get(defaultValue.toLowerCase());
        } else {
            throw new OptionException("The default value '" + defaultValue + "' is not a legal value for the '" + this.getName() + "' option. ");
        }
    }

    public Class<?> getClazz(String value) {
        return this.legalValueClass.get(value);
    }

    public void addLegalValue(String value, String desc, String classname) throws MaltChainedException {
        if (value == null || value.equals("")) {
            throw new OptionException("The legal value is missing for the '" + this.getName() + "' option. ");
        }
        if (this.legalValues.contains(value.toLowerCase())) {
            throw new OptionException("The legal value for the '" + this.getName() + "' option already exists. ");
        }
        this.legalValues.add(value.toLowerCase());
        if (desc == null || desc.equals("")) {
            this.legalValueDesc.put(value.toLowerCase(), "Description is missing. ");
        } else {
            this.legalValueDesc.put(value.toLowerCase(), desc);
        }
        if (classname == null || classname.equals("")) {
            throw new OptionException("The class name used by the '" + this.getName() + "' option is missing. ");
        }
        try {
            Class<?> clazz = null;
            if (PluginLoader.instance() != null) {
                clazz = PluginLoader.instance().getClass(classname);
            }
            if (clazz == null) {
                clazz = Class.forName(classname);
            }
            this.legalValueClass.put(value, clazz);
            this.classLegalValues.put(clazz, value);
        }
        catch (ClassNotFoundException e) {
            throw new OptionException("The class " + classname + " for the '" + this.getName() + "' option could not be found. ", e);
        }
    }

    @Override
    public String getDefaultValueString() {
        return this.classLegalValues.get(this.defaultValue);
    }

    @Override
    public String getStringRepresentation(Object value) {
        if (value instanceof Class && this.classLegalValues.containsKey(value)) {
            return this.classLegalValues.get(value);
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

