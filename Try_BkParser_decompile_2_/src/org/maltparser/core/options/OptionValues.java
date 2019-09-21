/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.options;

import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.core.options.OptionContainer;
import org.maltparser.core.options.OptionException;
import org.maltparser.core.options.option.Option;

public class OptionValues {
    private final SortedMap<Integer, OptionContainer> optionContainers = Collections.synchronizedSortedMap(new TreeMap());

    public Object getOptionValue(int containerIndex, Option option) throws OptionException {
        OptionContainer oc = (OptionContainer)this.optionContainers.get(containerIndex);
        if (oc == null) {
            throw new OptionException("The option container '" + containerIndex + "' cannot be found. ");
        }
        return oc.getOptionValue(option);
    }

    public String getOptionValueString(int containerIndex, Option option) throws OptionException {
        OptionContainer oc = (OptionContainer)this.optionContainers.get(containerIndex);
        if (oc == null) {
            throw new OptionException("The option container '" + containerIndex + "' cannot be found. ");
        }
        return oc.getOptionValueString(option);
    }

    public Object getOptionValue(Option option) throws OptionException {
        if (this.optionContainers.size() == 0) {
            return null;
        }
        OptionContainer oc = (OptionContainer)this.optionContainers.get(this.optionContainers.firstKey());
        return oc.getOptionValue(option);
    }

    public int getNumberOfOptionValues(int containerIndex) {
        if (!this.optionContainers.containsKey(containerIndex)) {
            return 0;
        }
        return ((OptionContainer)this.optionContainers.get(containerIndex)).getNumberOfOptionValues();
    }

    public Set<Integer> getOptionContainerIndices() {
        return this.optionContainers.keySet();
    }

    protected boolean addOptionValue(int containerType, int containerIndex, Option option, Object value) throws OptionException {
        OptionContainer oc;
        if (option == null) {
            throw new OptionException("The option cannot be found. ");
        }
        if (value == null) {
            throw new OptionException("The option value cannot be found. ");
        }
        if (!this.optionContainers.containsKey(containerIndex)) {
            this.optionContainers.put(containerIndex, new OptionContainer(containerIndex));
        }
        if ((oc = (OptionContainer)this.optionContainers.get(containerIndex)) == null) {
            throw new OptionException("The option container index " + containerIndex + " is unknown");
        }
        if (!oc.contains(containerType, option)) {
            oc.addOptionValue(containerType, option, value);
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.optionContainers.size() == 0) {
            sb.append("No option values.");
        } else if (this.optionContainers.size() == 1) {
            sb.append(this.optionContainers.get(this.optionContainers.firstKey()));
        } else {
            for (Integer index : this.optionContainers.keySet()) {
                sb.append("Option container : " + index + "\n");
                sb.append(this.optionContainers.get(index) + "\n");
            }
        }
        return sb.toString();
    }
}

