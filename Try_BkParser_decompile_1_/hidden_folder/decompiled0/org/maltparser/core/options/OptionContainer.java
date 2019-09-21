/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.options;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.core.options.OptionException;
import org.maltparser.core.options.OptionGroup;
import org.maltparser.core.options.option.Option;

public class OptionContainer
implements Comparable<OptionContainer> {
    public static final int SAVEDOPTION = 0;
    public static final int DEPENDENCIES_RESOLVED = 1;
    public static final int COMMANDLINE = 2;
    public static final int OPTIONFILE = 3;
    private final int index;
    private final SortedMap<Option, Object> savedOptionMap;
    private final SortedMap<Option, Object> dependenciesResolvedOptionMap;
    private final SortedMap<Option, Object> commandLineOptionMap;
    private final SortedMap<Option, Object> optionFileOptionMap;

    public OptionContainer(int index) throws OptionException {
        this.index = index;
        this.savedOptionMap = Collections.synchronizedSortedMap(new TreeMap());
        this.dependenciesResolvedOptionMap = Collections.synchronizedSortedMap(new TreeMap());
        this.commandLineOptionMap = Collections.synchronizedSortedMap(new TreeMap());
        this.optionFileOptionMap = Collections.synchronizedSortedMap(new TreeMap());
    }

    protected void addOptionValue(int type, Option option, Object value) throws OptionException {
        if (type == 0) {
            this.savedOptionMap.put(option, value);
        } else if (type == 1) {
            this.dependenciesResolvedOptionMap.put(option, value);
        } else if (type == 2) {
            this.commandLineOptionMap.put(option, value);
        } else if (type == 3) {
            this.optionFileOptionMap.put(option, value);
        } else {
            throw new OptionException("Unknown option container type");
        }
    }

    public Object getOptionValue(Option option) {
        Object value = null;
        for (int i = 0; i <= 3; ++i) {
            if (i == 0) {
                value = this.savedOptionMap.get(option);
            } else if (i == 1) {
                value = this.dependenciesResolvedOptionMap.get(option);
            } else if (i == 2) {
                value = this.commandLineOptionMap.get(option);
            } else if (i == 3) {
                value = this.optionFileOptionMap.get(option);
            }
            if (value == null) continue;
            return value;
        }
        return null;
    }

    public String getOptionValueString(Option option) {
        String value = null;
        for (int i = 0; i <= 3; ++i) {
            if (i == 0) {
                value = option.getStringRepresentation(this.savedOptionMap.get(option));
            } else if (i == 1) {
                value = option.getStringRepresentation(this.dependenciesResolvedOptionMap.get(option));
            } else if (i == 2) {
                value = option.getStringRepresentation(this.commandLineOptionMap.get(option));
            } else if (i == 3) {
                value = option.getStringRepresentation(this.optionFileOptionMap.get(option));
            }
            if (value == null) continue;
            return value;
        }
        return null;
    }

    public boolean contains(int type, Option option) throws OptionException {
        if (type == 0) {
            return this.savedOptionMap.containsValue(option);
        }
        if (type == 1) {
            return this.dependenciesResolvedOptionMap.containsValue(option);
        }
        if (type == 2) {
            return this.commandLineOptionMap.containsValue(option);
        }
        if (type == 3) {
            return this.optionFileOptionMap.containsValue(option);
        }
        throw new OptionException("Unknown option container type");
    }

    public int getNumberOfOptionValues() {
        TreeSet<Option> union = new TreeSet<Option>(this.savedOptionMap.keySet());
        union.addAll(this.dependenciesResolvedOptionMap.keySet());
        union.addAll(this.commandLineOptionMap.keySet());
        union.addAll(this.optionFileOptionMap.keySet());
        return union.size();
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public int compareTo(OptionContainer that) {
        int BEFORE = -1;
        boolean EQUAL = false;
        boolean AFTER = true;
        if (this == that) {
            return 0;
        }
        if (this.index < that.index) {
            return -1;
        }
        return this.index > that.index;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        TreeSet<Option> union = new TreeSet<Option>(this.savedOptionMap.keySet());
        union.addAll(this.dependenciesResolvedOptionMap.keySet());
        union.addAll(this.commandLineOptionMap.keySet());
        union.addAll(this.optionFileOptionMap.keySet());
        for (Option option : union) {
            Object value = null;
            for (int i = 0; i <= 3; ++i) {
                if (i == 0) {
                    value = this.savedOptionMap.get(option);
                } else if (i == 1) {
                    value = this.dependenciesResolvedOptionMap.get(option);
                } else if (i == 2) {
                    value = this.commandLineOptionMap.get(option);
                } else if (i == 3) {
                    value = this.optionFileOptionMap.get(option);
                }
                if (value != null) break;
            }
            sb.append(option.getGroup().getName() + "\t" + option.getName() + "\t" + value + "\n");
        }
        return sb.toString();
    }
}
