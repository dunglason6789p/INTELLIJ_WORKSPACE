/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.flow.spec;

import java.util.HashMap;
import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartManager;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.system.FlowChartSystem;
import org.maltparser.core.flow.system.elem.ChartElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ChartItemSpecification {
    private String chartItemName;
    private Class<? extends ChartItem> chartItemClass;
    private HashMap<String, String> attributes;

    public ChartItemSpecification() {
        this(null, null);
    }

    public ChartItemSpecification(String chartItemName, Class<? extends ChartItem> chartItemClass) {
        this.setChartItemName(chartItemName);
        this.setChartItemClass(chartItemClass);
        this.attributes = new HashMap(3);
    }

    public String getChartItemName() {
        return this.chartItemName;
    }

    public void setChartItemName(String chartItemName) {
        this.chartItemName = chartItemName;
    }

    public Class<? extends ChartItem> getChartItemClass() {
        return this.chartItemClass;
    }

    public void setChartItemClass(Class<? extends ChartItem> chartItemClass) {
        this.chartItemClass = chartItemClass;
    }

    public HashMap<String, String> getChartItemAttributes() {
        return this.attributes;
    }

    public String getChartItemAttribute(String key) {
        return this.attributes.get(key);
    }

    public void addChartItemAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    public void removeChartItemAttribute(String key) {
        this.attributes.remove(key);
    }

    public void read(Element chartItemSpec, FlowChartManager flowCharts) throws MaltChainedException {
        this.chartItemName = chartItemSpec.getAttribute("item");
        this.chartItemClass = flowCharts.getFlowChartSystem().getChartElement(this.chartItemName).getChartItemClass();
        NamedNodeMap attrs = chartItemSpec.getAttributes();
        for (int i = 0; i < attrs.getLength(); ++i) {
            Attr attribute = (Attr)attrs.item(i);
            this.addChartItemAttribute(attribute.getName(), attribute.getValue());
        }
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.chartItemName == null ? 0 : this.chartItemName.hashCode());
        result = 31 * result + (this.attributes == null ? 0 : this.attributes.hashCode());
        result = 31 * result + (this.chartItemClass == null ? 0 : this.chartItemClass.hashCode());
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
        ChartItemSpecification other = (ChartItemSpecification)obj;
        if (this.chartItemName == null ? other.chartItemName != null : !this.chartItemName.equals(other.chartItemName)) {
            return false;
        }
        if (this.attributes == null ? other.attributes != null : !this.attributes.equals(other.attributes)) {
            return false;
        }
        return !(this.chartItemClass == null ? other.chartItemClass != null : !this.chartItemClass.equals(other.chartItemClass));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.chartItemName);
        sb.append(' ');
        for (String key : this.attributes.keySet()) {
            sb.append(key);
            sb.append('=');
            sb.append(this.attributes.get(key));
            sb.append(' ');
        }
        return sb.toString();
    }
}

