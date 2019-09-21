/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.flow;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.flow.FlowException;
import org.maltparser.core.flow.spec.ChartSpecification;
import org.maltparser.core.flow.system.FlowChartSystem;
import org.maltparser.core.helper.URLFinder;
import org.maltparser.core.plugin.Plugin;
import org.maltparser.core.plugin.PluginLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FlowChartManager {
    private static FlowChartManager uniqueInstance = new FlowChartManager();
    private final FlowChartSystem flowChartSystem = new FlowChartSystem();
    private final HashMap<String, ChartSpecification> chartSpecifications = new HashMap();

    public static FlowChartManager instance() {
        return uniqueInstance;
    }

    public void load(String urlstring) throws MaltChainedException {
        URLFinder f = new URLFinder();
        this.load(f.findURL(urlstring));
    }

    public void load(PluginLoader plugins) throws MaltChainedException {
        for (Plugin plugin : plugins) {
            URL url = null;
            try {
                url = new URL("jar:" + plugin.getUrl() + "!/appdata/plugin.xml");
            }
            catch (MalformedURLException e) {
                throw new FeatureException("Malformed URL: 'jar:" + plugin.getUrl() + "!plugin.xml'", e);
            }
            try {
                InputStream is = url.openStream();
                is.close();
            }
            catch (IOException e) {
                continue;
            }
            this.load(url);
        }
    }

    public void load(URL url) throws MaltChainedException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Element root = null;
            root = db.parse(url.openStream()).getDocumentElement();
            if (root == null) {
                throw new FlowException("The flow chart specification file '" + url.getFile() + "' cannot be found. ");
            }
            this.readFlowCharts(root);
        }
        catch (IOException e) {
            throw new FlowException("The flow chart specification file '" + url.getFile() + "' cannot be found. ", e);
        }
        catch (ParserConfigurationException e) {
            throw new FlowException("Problem parsing the flow chart file " + url.getFile() + ". ", e);
        }
        catch (SAXException e) {
            throw new FlowException("Problem parsing the flow chart file " + url.getFile() + ". ", e);
        }
    }

    private void readFlowCharts(Element flowcharts) throws MaltChainedException {
        NodeList flowChartList = flowcharts.getElementsByTagName("flowchart");
        for (int i = 0; i < flowChartList.getLength(); ++i) {
            String flowChartName = ((Element)flowChartList.item(i)).getAttribute("name");
            if (this.chartSpecifications.containsKey(flowChartName)) {
                throw new FlowException("Problem parsing the flow chart file. The flow chart with the name " + flowChartName + " already exists. ");
            }
            ChartSpecification chart = new ChartSpecification();
            this.chartSpecifications.put(flowChartName, chart);
            chart.read((Element)flowChartList.item(i), this);
        }
    }

    public FlowChartInstance initialize(int optionContainerIndex, String flowChartName) throws MaltChainedException {
        return new FlowChartInstance(optionContainerIndex, this.chartSpecifications.get(flowChartName), this);
    }

    public FlowChartSystem getFlowChartSystem() {
        return this.flowChartSystem;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FLOW CHART SYSTEM\n");
        sb.append(this.flowChartSystem);
        sb.append('\n');
        sb.append("FLOW CHARTS:\n");
        for (String key : this.chartSpecifications.keySet()) {
            sb.append(this.chartSpecifications.get(key));
            sb.append('\n');
        }
        return sb.toString();
    }
}

