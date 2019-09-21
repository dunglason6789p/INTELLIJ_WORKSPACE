/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.flow.spec;

import java.util.LinkedHashSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartManager;
import org.maltparser.core.flow.FlowException;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ChartSpecification {
    private String name;
    private LinkedHashSet<ChartItemSpecification> preProcessChartItemSpecifications = new LinkedHashSet(7);
    private LinkedHashSet<ChartItemSpecification> processChartItemSpecifications = new LinkedHashSet(7);
    private LinkedHashSet<ChartItemSpecification> postProcessChartItemSpecifications = new LinkedHashSet(7);

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedHashSet<ChartItemSpecification> getPreProcessChartItemSpecifications() {
        return this.preProcessChartItemSpecifications;
    }

    public void addPreProcessChartItemSpecifications(ChartItemSpecification chartItemSpecification) {
        this.preProcessChartItemSpecifications.add(chartItemSpecification);
    }

    public void removePreProcessChartItemSpecifications(ChartItemSpecification chartItemSpecification) {
        this.preProcessChartItemSpecifications.remove(chartItemSpecification);
    }

    public LinkedHashSet<ChartItemSpecification> getProcessChartItemSpecifications() {
        return this.processChartItemSpecifications;
    }

    public void addProcessChartItemSpecifications(ChartItemSpecification chartItemSpecification) {
        this.processChartItemSpecifications.add(chartItemSpecification);
    }

    public void removeProcessChartItemSpecifications(ChartItemSpecification chartItemSpecification) {
        this.processChartItemSpecifications.remove(chartItemSpecification);
    }

    public LinkedHashSet<ChartItemSpecification> getPostProcessChartItemSpecifications() {
        return this.postProcessChartItemSpecifications;
    }

    public void addPostProcessChartItemSpecifications(ChartItemSpecification chartItemSpecification) {
        this.postProcessChartItemSpecifications.add(chartItemSpecification);
    }

    public void removePostProcessChartItemSpecifications(ChartItemSpecification chartItemSpecification) {
        this.postProcessChartItemSpecifications.remove(chartItemSpecification);
    }

    public void read(Element chartElem, FlowChartManager flowCharts) throws MaltChainedException {
        this.setName(chartElem.getAttribute("name"));
        NodeList flowChartProcessList = chartElem.getElementsByTagName("preprocess");
        if (flowChartProcessList.getLength() == 1) {
            this.readChartItems((Element)flowChartProcessList.item(0), flowCharts, this.preProcessChartItemSpecifications);
        } else if (flowChartProcessList.getLength() > 1) {
            throw new FlowException("The flow chart '" + this.getName() + "' has more than one preprocess elements. ");
        }
        flowChartProcessList = chartElem.getElementsByTagName("process");
        if (flowChartProcessList.getLength() == 1) {
            this.readChartItems((Element)flowChartProcessList.item(0), flowCharts, this.processChartItemSpecifications);
        } else if (flowChartProcessList.getLength() > 1) {
            throw new FlowException("The flow chart '" + this.getName() + "' has more than one process elements. ");
        }
        flowChartProcessList = chartElem.getElementsByTagName("postprocess");
        if (flowChartProcessList.getLength() == 1) {
            this.readChartItems((Element)flowChartProcessList.item(0), flowCharts, this.postProcessChartItemSpecifications);
        } else if (flowChartProcessList.getLength() > 1) {
            throw new FlowException("The flow chart '" + this.getName() + "' has more than one postprocess elements. ");
        }
    }

    private void readChartItems(Element chartElem, FlowChartManager flowCharts, LinkedHashSet<ChartItemSpecification> chartItemSpecifications) throws MaltChainedException {
        NodeList flowChartItemList = chartElem.getElementsByTagName("chartitem");
        for (int i = 0; i < flowChartItemList.getLength(); ++i) {
            ChartItemSpecification chartItemSpecification = new ChartItemSpecification();
            chartItemSpecification.read((Element)flowChartItemList.item(i), flowCharts);
            chartItemSpecifications.add(chartItemSpecification);
        }
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
        result = 31 * result + (this.postProcessChartItemSpecifications == null ? 0 : this.postProcessChartItemSpecifications.hashCode());
        result = 31 * result + (this.preProcessChartItemSpecifications == null ? 0 : this.preProcessChartItemSpecifications.hashCode());
        result = 31 * result + (this.processChartItemSpecifications == null ? 0 : this.processChartItemSpecifications.hashCode());
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
        ChartSpecification other = (ChartSpecification)obj;
        if (this.name == null ? other.name != null : !this.name.equals(other.name)) {
            return false;
        }
        if (this.postProcessChartItemSpecifications == null ? other.postProcessChartItemSpecifications != null : !this.postProcessChartItemSpecifications.equals(other.postProcessChartItemSpecifications)) {
            return false;
        }
        if (this.preProcessChartItemSpecifications == null ? other.preProcessChartItemSpecifications != null : !this.preProcessChartItemSpecifications.equals(other.preProcessChartItemSpecifications)) {
            return false;
        }
        return !(this.processChartItemSpecifications == null ? other.processChartItemSpecifications != null : !this.processChartItemSpecifications.equals(other.processChartItemSpecifications));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append('\n');
        if (this.preProcessChartItemSpecifications.size() > 0) {
            sb.append("  preprocess:");
            sb.append('\n');
            for (ChartItemSpecification key : this.preProcessChartItemSpecifications) {
                sb.append(key);
                sb.append('\n');
            }
        }
        if (this.processChartItemSpecifications.size() > 0) {
            sb.append("  process:");
            sb.append('\n');
            for (ChartItemSpecification key : this.processChartItemSpecifications) {
                sb.append(key);
                sb.append('\n');
            }
        }
        if (this.postProcessChartItemSpecifications.size() > 0) {
            sb.append("  postprocess:");
            sb.append('\n');
            for (ChartItemSpecification key : this.postProcessChartItemSpecifications) {
                sb.append(key);
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}

