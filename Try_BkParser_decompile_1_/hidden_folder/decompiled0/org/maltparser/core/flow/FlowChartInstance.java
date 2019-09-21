/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.flow;

import java.util.HashMap;
import java.util.LinkedHashSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartManager;
import org.maltparser.core.flow.FlowException;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.maltparser.core.flow.spec.ChartSpecification;

public class FlowChartInstance {
    private FlowChartManager flowChartManager;
    private int optionContainerIndex;
    private String name;
    private ChartSpecification chartSpecification;
    private final LinkedHashSet<ChartItem> preProcessChartItems;
    private final LinkedHashSet<ChartItem> processChartItems;
    private final LinkedHashSet<ChartItem> postProcessChartItems;
    private final HashMap<String, Object> flowChartRegistry;
    private final HashMap<String, Object> engineRegistry;
    private final StringBuilder flowChartRegistryKey;

    public FlowChartInstance(int optionContainerIndex, ChartSpecification chartSpecification, FlowChartManager flowChartManager) throws MaltChainedException {
        this.setFlowChartManager(flowChartManager);
        this.setOptionContainerIndex(optionContainerIndex);
        this.setChartSpecification(chartSpecification);
        this.flowChartRegistry = new HashMap();
        this.engineRegistry = new HashMap();
        this.flowChartRegistryKey = new StringBuilder();
        this.preProcessChartItems = new LinkedHashSet();
        for (ChartItemSpecification chartItemSpecification : chartSpecification.getPreProcessChartItemSpecifications()) {
            this.preProcessChartItems.add(this.initChartItem(chartItemSpecification));
        }
        this.processChartItems = new LinkedHashSet();
        for (ChartItemSpecification chartItemSpecification : chartSpecification.getProcessChartItemSpecifications()) {
            this.processChartItems.add(this.initChartItem(chartItemSpecification));
        }
        this.postProcessChartItems = new LinkedHashSet();
        for (ChartItemSpecification chartItemSpecification : chartSpecification.getPostProcessChartItemSpecifications()) {
            this.postProcessChartItems.add(this.initChartItem(chartItemSpecification));
        }
    }

    protected ChartItem initChartItem(ChartItemSpecification chartItemSpecification) throws MaltChainedException {
        ChartItem chartItem = null;
        try {
            chartItem = chartItemSpecification.getChartItemClass().newInstance();
            chartItem.initialize(this, chartItemSpecification);
        }
        catch (InstantiationException e) {
            throw new FlowException("The chart item '" + chartItemSpecification.getChartItemName() + "' could not be created. ", e);
        }
        catch (IllegalAccessException e) {
            throw new FlowException("The chart item '" + chartItemSpecification.getChartItemName() + "' could not be created. ", e);
        }
        return chartItem;
    }

    private void setFlowChartRegistryKey(Class<?> entryClass, String identifier) {
        this.flowChartRegistryKey.setLength(0);
        this.flowChartRegistryKey.append(identifier.toString());
        this.flowChartRegistryKey.append(entryClass.toString());
    }

    public void addFlowChartRegistry(Class<?> entryClass, String identifier, Object entry) {
        this.setFlowChartRegistryKey(entryClass, identifier);
        this.flowChartRegistry.put(this.flowChartRegistryKey.toString(), entry);
    }

    public void removeFlowChartRegistry(Class<?> entryClass, String identifier) {
        this.setFlowChartRegistryKey(entryClass, identifier);
        this.flowChartRegistry.remove(this.flowChartRegistryKey.toString());
    }

    public Object getFlowChartRegistry(Class<?> entryClass, String identifier) {
        this.setFlowChartRegistryKey(entryClass, identifier);
        return this.flowChartRegistry.get(this.flowChartRegistryKey.toString());
    }

    public void setEngineRegistry(String key, Object value) {
        this.engineRegistry.put(key, value);
    }

    public Object getEngineRegistry(String key) {
        return this.engineRegistry.get(key);
    }

    public FlowChartManager getFlowChartManager() {
        return this.flowChartManager;
    }

    protected void setFlowChartManager(FlowChartManager flowChartManager) {
        this.flowChartManager = flowChartManager;
    }

    public int getOptionContainerIndex() {
        return this.optionContainerIndex;
    }

    protected void setOptionContainerIndex(int optionContainerIndex) {
        this.optionContainerIndex = optionContainerIndex;
    }

    public ChartSpecification getChartSpecification() {
        return this.chartSpecification;
    }

    protected void setChartSpecification(ChartSpecification chartSpecification) {
        this.chartSpecification = chartSpecification;
    }

    public LinkedHashSet<ChartItem> getPreProcessChartItems() {
        return this.preProcessChartItems;
    }

    public LinkedHashSet<ChartItem> getProcessChartItems() {
        return this.processChartItems;
    }

    public LinkedHashSet<ChartItem> getPostProcessChartItems() {
        return this.postProcessChartItems;
    }

    public boolean hasPreProcessChartItems() {
        return this.preProcessChartItems.size() != 0;
    }

    public boolean hasProcessChartItems() {
        return this.processChartItems.size() != 0;
    }

    public boolean hasPostProcessChartItems() {
        return this.postProcessChartItems.size() != 0;
    }

    public int preprocess() throws MaltChainedException {
        LinkedHashSet<ChartItem> chartItems = this.getPreProcessChartItems();
        if (chartItems.size() == 0) {
            return 2;
        }
        int signal = 1;
        for (ChartItem chartItem : chartItems) {
            signal = chartItem.preprocess(signal);
            if (signal != 2) continue;
            return signal;
        }
        return signal;
    }

    public int process() throws MaltChainedException {
        LinkedHashSet<ChartItem> chartItems = this.getProcessChartItems();
        if (chartItems.size() == 0) {
            return 2;
        }
        int signal = 1;
        for (ChartItem chartItem : chartItems) {
            signal = chartItem.process(signal);
        }
        return signal;
    }

    public int postprocess() throws MaltChainedException {
        LinkedHashSet<ChartItem> chartItems = this.getPostProcessChartItems();
        if (chartItems.size() == 0) {
            return 2;
        }
        int signal = 1;
        for (ChartItem chartItem : chartItems) {
            signal = chartItem.postprocess(signal);
            if (signal != 2) continue;
            return signal;
        }
        return signal;
    }

    public void terminate() throws MaltChainedException {
        LinkedHashSet<ChartItem> chartItems = this.getPreProcessChartItems();
        for (ChartItem chartItem : chartItems) {
            chartItem.terminate();
        }
        chartItems = this.getProcessChartItems();
        for (ChartItem chartItem : chartItems) {
            chartItem.terminate();
        }
        chartItems = this.getPostProcessChartItems();
        for (ChartItem chartItem : chartItems) {
            chartItem.terminate();
        }
        this.flowChartRegistry.clear();
        this.engineRegistry.clear();
        this.flowChartRegistryKey.setLength(0);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.optionContainerIndex;
        result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
        result = 31 * result + (this.chartSpecification == null ? 0 : this.chartSpecification.hashCode());
        result = 31 * result + (this.flowChartRegistry == null ? 0 : this.flowChartRegistry.hashCode());
        result = 31 * result + (this.postProcessChartItems == null ? 0 : this.postProcessChartItems.hashCode());
        result = 31 * result + (this.preProcessChartItems == null ? 0 : this.preProcessChartItems.hashCode());
        result = 31 * result + (this.processChartItems == null ? 0 : this.processChartItems.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        FlowChartInstance other = (FlowChartInstance)obj;
        if (this.optionContainerIndex != other.optionContainerIndex) {
            return false;
        }
        if (this.name == null ? other.name != null : !this.name.equals(other.name)) {
            return false;
        }
        if (this.chartSpecification == null ? other.chartSpecification != null : !this.chartSpecification.equals(other.chartSpecification)) {
            return false;
        }
        if (this.flowChartRegistry == null ? other.flowChartRegistry != null : !this.flowChartRegistry.equals(other.flowChartRegistry)) {
            return false;
        }
        if (this.postProcessChartItems == null ? other.postProcessChartItems != null : !this.postProcessChartItems.equals(other.postProcessChartItems)) {
            return false;
        }
        if (this.preProcessChartItems == null ? other.preProcessChartItems != null : !this.preProcessChartItems.equals(other.preProcessChartItems)) {
            return false;
        }
        return !(this.processChartItems == null ? other.processChartItems != null : !this.processChartItems.equals(other.processChartItems));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append('\n');
        if (this.preProcessChartItems.size() > 0) {
            sb.append("  preprocess:");
            sb.append('\n');
            for (ChartItem key : this.preProcessChartItems) {
                sb.append(key);
                sb.append('\n');
            }
        }
        if (this.processChartItems.size() > 0) {
            sb.append("  process:");
            sb.append('\n');
            for (ChartItem key : this.processChartItems) {
                sb.append(key);
                sb.append('\n');
            }
        }
        if (this.postProcessChartItems.size() > 0) {
            sb.append("  postprocess:");
            sb.append('\n');
            for (ChartItem key : this.postProcessChartItems) {
                sb.append(key);
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}

