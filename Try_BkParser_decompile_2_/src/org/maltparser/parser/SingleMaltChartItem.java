/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Pattern;
import org.maltparser.core.config.Configuration;
import org.maltparser.core.config.ConfigurationDir;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.maltparser.core.flow.system.elem.ChartAttribute;
import org.maltparser.core.flow.system.elem.ChartElement;
import org.maltparser.core.helper.HashSet;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.io.dataformat.DataFormatManager;
import org.maltparser.core.io.dataformat.DataFormatSpecification;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.SingleMalt;
import org.maltparser.parser.guide.ClassifierGuide;

public class SingleMaltChartItem
extends ChartItem {
    private SingleMalt singleMalt;
    private String idName;
    private String targetName;
    private String sourceName;
    private String modeName;
    private String taskName;
    private DependencyStructure cachedSourceGraph = null;
    private DependencyStructure cachedTargetGraph = null;

    @Override
    public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
        super.initialize(flowChartinstance, chartItemSpecification);
        for (String key : chartItemSpecification.getChartItemAttributes().keySet()) {
            if (key.equals("target")) {
                this.targetName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (key.equals("source")) {
                this.sourceName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (key.equals("mode")) {
                this.modeName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (key.equals("task")) {
                this.taskName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (!key.equals("id")) continue;
            this.idName = chartItemSpecification.getChartItemAttributes().get(key);
        }
        if (this.targetName == null) {
            this.targetName = this.getChartElement("singlemalt").getAttributes().get("target").getDefaultValue();
        } else if (this.sourceName == null) {
            this.sourceName = this.getChartElement("singlemalt").getAttributes().get("source").getDefaultValue();
        } else if (this.modeName == null) {
            this.modeName = this.getChartElement("singlemalt").getAttributes().get("mode").getDefaultValue();
        } else if (this.taskName == null) {
            this.taskName = this.getChartElement("singlemalt").getAttributes().get("task").getDefaultValue();
        } else if (this.idName == null) {
            this.idName = this.getChartElement("singlemalt").getAttributes().get("id").getDefaultValue();
        }
        this.singleMalt = (SingleMalt)flowChartinstance.getFlowChartRegistry(SingleMalt.class, this.idName);
        if (this.singleMalt == null) {
            this.singleMalt = new SingleMalt();
            flowChartinstance.addFlowChartRegistry(SingleMalt.class, this.idName, this.singleMalt);
            flowChartinstance.addFlowChartRegistry(Configuration.class, this.idName, this.singleMalt);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public int preprocess(int signal) throws MaltChainedException {
        if (!this.taskName.equals("init")) return signal;
        if (!this.modeName.equals("learn") && !this.modeName.equals("parse")) return 2;
        OptionManager.instance().overloadOptionValue(this.getOptionContainerIndex(), "singlemalt", "mode", this.modeName);
        ConfigurationDir configDir = (ConfigurationDir)this.flowChartinstance.getFlowChartRegistry(ConfigurationDir.class, this.idName);
        DataFormatManager dataFormatManager = configDir.getDataFormatManager();
        if (this.modeName.equals("learn")) {
            DataFormatInstance dataFormatInstance = null;
            if (dataFormatManager.getInputDataFormatSpec().getDataStructure() == DataFormatSpecification.DataStructure.PHRASE) {
                HashSet<DataFormatSpecification.Dependency> deps = dataFormatManager.getInputDataFormatSpec().getDependencies();
                String nullValueStrategy = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "singlemalt", "null_value").toString();
                for (DataFormatSpecification.Dependency dep : dataFormatManager.getInputDataFormatSpec().getDependencies()) {
                    dataFormatInstance = dataFormatManager.getDataFormatSpec(dep.getDependentOn()).createDataFormatInstance(configDir.getSymbolTables(), nullValueStrategy);
                    configDir.addDataFormatInstance(dataFormatManager.getOutputDataFormatSpec().getDataFormatName(), dataFormatInstance);
                }
                String decisionSettings = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "guide", "decision_settings").toString().trim();
                StringBuilder newDecisionSettings = new StringBuilder();
                if (!Pattern.matches(".*A\\.HEADREL.*", decisionSettings)) {
                    newDecisionSettings.append("+A.HEADREL");
                }
                if (!Pattern.matches(".*A\\.PHRASE.*", decisionSettings)) {
                    newDecisionSettings.append("+A.PHRASE");
                }
                if (!Pattern.matches(".*A\\.ATTACH.*", decisionSettings)) {
                    newDecisionSettings.append("+A.ATTACH");
                }
                if (newDecisionSettings.length() > 0) {
                    OptionManager.instance().overloadOptionValue(this.getOptionContainerIndex(), "guide", "decision_settings", decisionSettings + newDecisionSettings.toString());
                }
            } else {
                dataFormatInstance = configDir.getDataFormatInstance(dataFormatManager.getInputDataFormatSpec().getDataFormatName());
            }
            this.singleMalt.initialize(this.getOptionContainerIndex(), dataFormatInstance, configDir.getSymbolTables(), configDir, 0);
            return signal;
        } else {
            if (!this.modeName.equals("parse")) return 2;
            this.singleMalt.initialize(this.getOptionContainerIndex(), configDir.getDataFormatInstance(dataFormatManager.getInputDataFormatSpec().getDataFormatName()), configDir.getSymbolTables(), configDir, 1);
        }
        return signal;
    }

    @Override
    public int process(int signal) throws MaltChainedException {
        if (this.taskName.equals("process")) {
            if (this.cachedSourceGraph == null) {
                this.cachedSourceGraph = (DependencyStructure)this.flowChartinstance.getFlowChartRegistry(DependencyStructure.class, this.sourceName);
            }
            if (this.cachedTargetGraph == null) {
                this.cachedTargetGraph = (DependencyStructure)this.flowChartinstance.getFlowChartRegistry(DependencyStructure.class, this.targetName);
            }
            if (this.modeName.equals("learn")) {
                this.singleMalt.oracleParse(this.cachedSourceGraph, this.cachedTargetGraph);
            } else if (this.modeName.equals("parse")) {
                this.singleMalt.parse(this.cachedSourceGraph);
            }
        }
        return signal;
    }

    @Override
    public int postprocess(int signal) throws MaltChainedException {
        if (this.taskName.equals("train") && this.singleMalt.getGuide() != null) {
            this.singleMalt.getGuide().noMoreInstances();
        } else if (this.taskName.equals("train") && this.singleMalt.getGuide() == null) {
            this.singleMalt.train();
        }
        return signal;
    }

    @Override
    public void terminate() throws MaltChainedException {
        if (this.flowChartinstance.getFlowChartRegistry(SingleMalt.class, this.idName) != null) {
            this.singleMalt.terminate(null);
            this.flowChartinstance.removeFlowChartRegistry(SingleMalt.class, this.idName);
            this.flowChartinstance.removeFlowChartRegistry(Configuration.class, this.idName);
            this.singleMalt = null;
        } else {
            this.singleMalt = null;
        }
        this.cachedSourceGraph = null;
        this.cachedTargetGraph = null;
    }

    public SingleMalt getSingleMalt() {
        return this.singleMalt;
    }

    public void setSingleMalt(SingleMalt singleMalt) {
        this.singleMalt = singleMalt;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getSourceName() {
        return this.sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
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
        return obj.toString().equals(this.toString());
    }

    public int hashCode() {
        return 217 + (null == this.toString() ? 0 : this.toString().hashCode());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("    singlemalt ");
        sb.append("id:");
        sb.append(this.idName);
        sb.append(' ');
        sb.append("mode:");
        sb.append(this.modeName);
        sb.append(' ');
        sb.append("task:");
        sb.append(this.taskName);
        sb.append(' ');
        sb.append("source:");
        sb.append(this.sourceName);
        sb.append(' ');
        sb.append("target:");
        sb.append(this.targetName);
        return sb.toString();
    }
}

