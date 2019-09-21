/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.flow.system.elem;

import java.util.LinkedHashMap;
import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowException;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.system.FlowChartSystem;
import org.maltparser.core.flow.system.elem.ChartAttribute;
import org.maltparser.core.plugin.PluginLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ChartElement {
    private String item;
    private Class<? extends ChartItem> chartItemClass;
    private LinkedHashMap<String, ChartAttribute> attributes = new LinkedHashMap();

    public String getItem() {
        return this.item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void addAttribute(String name, ChartAttribute attribute) {
        this.attributes.put(name, attribute);
    }

    public ChartAttribute getAttribute(String name) {
        return this.attributes.get(name);
    }

    public Class<? extends ChartItem> getChartItemClass() {
        return this.chartItemClass;
    }

    public LinkedHashMap<String, ChartAttribute> getAttributes() {
        return this.attributes;
    }

    public void read(Element chartElem, FlowChartSystem flowChartSystem) throws MaltChainedException {
        this.setItem(chartElem.getAttribute("item"));
        String chartItemClassName = chartElem.getAttribute("class");
        Class<?> clazz = null;
        try {
            if (PluginLoader.instance() != null) {
                clazz = PluginLoader.instance().getClass(chartItemClassName);
            }
            if (clazz == null) {
                clazz = Class.forName(chartItemClassName);
            }
            this.chartItemClass = clazz.asSubclass(ChartItem.class);
        }
        catch (ClassCastException e) {
            throw new FlowException("The class '" + clazz.getName() + "' is not a subclass of '" + ChartItem.class.getName() + "'. ", e);
        }
        catch (ClassNotFoundException e) {
            throw new FlowException("The class " + chartItemClassName + "  could not be found. ", e);
        }
        NodeList attrElements = chartElem.getElementsByTagName("attribute");
        for (int i = 0; i < attrElements.getLength(); ++i) {
            ChartAttribute attribute = new ChartAttribute();
            attribute.read((Element)attrElements.item(i), flowChartSystem);
            this.attributes.put(((Element)attrElements.item(i)).getAttribute("name"), attribute);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("    ");
        sb.append(this.item);
        sb.append(' ');
        sb.append(this.chartItemClass.getName());
        sb.append('\n');
        for (String key : this.attributes.keySet()) {
            sb.append("       ");
            sb.append(this.attributes.get(key));
            sb.append('\n');
        }
        return sb.toString();
    }
}

