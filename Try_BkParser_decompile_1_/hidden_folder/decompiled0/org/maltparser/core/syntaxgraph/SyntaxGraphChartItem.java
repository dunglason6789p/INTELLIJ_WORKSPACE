/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.SortedMap;
import org.maltparser.core.config.ConfigurationDir;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.flow.FlowException;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.maltparser.core.flow.system.elem.ChartAttribute;
import org.maltparser.core.flow.system.elem.ChartElement;
import org.maltparser.core.helper.HashSet;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.io.dataformat.DataFormatManager;
import org.maltparser.core.io.dataformat.DataFormatSpecification;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyGraph;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.MappablePhraseStructureGraph;
import org.maltparser.core.syntaxgraph.PhraseStructure;
import org.maltparser.core.syntaxgraph.PhraseStructureGraph;
import org.maltparser.core.syntaxgraph.Sentence;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.ds2ps.LosslessMapping;

public class SyntaxGraphChartItem
extends ChartItem {
    private String idName;
    private String structureName;
    private String taskName;
    private TokenStructure graph;

    @Override
    public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
        super.initialize(flowChartinstance, chartItemSpecification);
        for (String key : chartItemSpecification.getChartItemAttributes().keySet()) {
            if (key.equals("id")) {
                this.idName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (key.equals("structure")) {
                this.structureName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (!key.equals("task")) continue;
            this.taskName = chartItemSpecification.getChartItemAttributes().get(key);
        }
        if (this.idName == null) {
            this.idName = this.getChartElement("graph").getAttributes().get("id").getDefaultValue();
        } else if (this.structureName == null) {
            this.structureName = this.getChartElement("graph").getAttributes().get("structure").getDefaultValue();
        } else if (this.taskName == null) {
            this.taskName = this.getChartElement("graph").getAttributes().get("task").getDefaultValue();
        }
    }

    @Override
    public int preprocess(int signal) throws MaltChainedException {
        if (this.taskName.equals("create")) {
            boolean phrase = false;
            boolean dependency = false;
            ConfigurationDir configDir = (ConfigurationDir)this.flowChartinstance.getFlowChartRegistry(ConfigurationDir.class, this.idName);
            DataFormatInstance dataFormatInstance = null;
            DataFormatManager dataFormatManager = configDir.getDataFormatManager();
            SymbolTableHandler symbolTables = configDir.getSymbolTables();
            for (String key : configDir.getDataFormatInstanceKeys()) {
                DataFormatInstance dfi = configDir.getDataFormatInstance(key);
                if (dfi.getDataFormarSpec().getDataStructure() == DataFormatSpecification.DataStructure.PHRASE) {
                    phrase = true;
                }
                if (dfi.getDataFormarSpec().getDataStructure() != DataFormatSpecification.DataStructure.DEPENDENCY) continue;
                dependency = true;
                dataFormatInstance = dfi;
            }
            if (!dependency && OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "config", "flowchart").toString().equals("learn")) {
                dependency = true;
                HashSet<DataFormatSpecification.Dependency> deps = dataFormatManager.getInputDataFormatSpec().getDependencies();
                String nullValueStategy = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "singlemalt", "null_value").toString();
                for (DataFormatSpecification.Dependency dep : deps) {
                    dataFormatInstance = dataFormatManager.getDataFormatSpec(dep.getDependentOn()).createDataFormatInstance(symbolTables, nullValueStategy);
                    configDir.addDataFormatInstance(dataFormatManager.getOutputDataFormatSpec().getDataFormatName(), dataFormatInstance);
                }
            }
            if (dependency && !phrase) {
                this.graph = new DependencyGraph(symbolTables);
                this.flowChartinstance.addFlowChartRegistry(DependencyStructure.class, this.structureName, this.graph);
            } else if (dependency && phrase) {
                LosslessMapping mapping;
                this.graph = new MappablePhraseStructureGraph(symbolTables);
                DataFormatInstance inFormat = configDir.getDataFormatInstance(dataFormatManager.getInputDataFormatSpec().getDataFormatName());
                DataFormatInstance outFormat = configDir.getDataFormatInstance(dataFormatManager.getOutputDataFormatSpec().getDataFormatName());
                if (inFormat != null && outFormat != null) {
                    mapping = null;
                    mapping = inFormat.getDataFormarSpec().getDataStructure() == DataFormatSpecification.DataStructure.DEPENDENCY ? new LosslessMapping(inFormat, outFormat, symbolTables) : new LosslessMapping(outFormat, inFormat, symbolTables);
                    if (inFormat.getDataFormarSpec().getDataStructure() == DataFormatSpecification.DataStructure.PHRASE) {
                        mapping.setHeadRules(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "graph", "head_rules").toString());
                    }
                } else {
                    throw new FlowException("Couldn't determine the input and output data format. ");
                }
                ((MappablePhraseStructureGraph)this.graph).setMapping(mapping);
                this.flowChartinstance.addFlowChartRegistry(DependencyStructure.class, this.structureName, this.graph);
                this.flowChartinstance.addFlowChartRegistry(PhraseStructure.class, this.structureName, this.graph);
            } else if (!dependency && phrase) {
                this.graph = new PhraseStructureGraph(symbolTables);
                this.flowChartinstance.addFlowChartRegistry(PhraseStructure.class, this.structureName, this.graph);
            } else {
                this.graph = new Sentence(symbolTables);
            }
            if (dataFormatInstance != null) {
                ((DependencyStructure)this.graph).setDefaultRootEdgeLabels(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "graph", "root_label").toString(), dataFormatInstance.getDependencyEdgeLabelSymbolTables(symbolTables));
            }
            this.flowChartinstance.addFlowChartRegistry(TokenStructure.class, this.structureName, this.graph);
        }
        return signal;
    }

    @Override
    public int process(int signal) throws MaltChainedException {
        return signal;
    }

    @Override
    public int postprocess(int signal) throws MaltChainedException {
        return signal;
    }

    @Override
    public void terminate() throws MaltChainedException {
        if (this.graph != null) {
            this.graph.clear();
            this.graph = null;
        }
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
        sb.append("    graph ");
        sb.append("id:");
        sb.append(this.idName);
        sb.append(' ');
        sb.append("task:");
        sb.append(this.taskName);
        sb.append(' ');
        sb.append("structure:");
        sb.append(this.structureName);
        return sb.toString();
    }
}

