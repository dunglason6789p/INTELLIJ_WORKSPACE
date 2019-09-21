/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.maltparser.core.flow.system.elem.ChartAttribute;
import org.maltparser.core.flow.system.elem.ChartElement;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class CopyChartItem
extends ChartItem {
    private String idName;
    private String targetName;
    private String sourceName;
    private String taskName;
    private boolean usePartialTree;
    private TokenStructure cachedSource = null;
    private TokenStructure cachedTarget = null;

    @Override
    public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
        super.initialize(flowChartinstance, chartItemSpecification);
        for (String key : chartItemSpecification.getChartItemAttributes().keySet()) {
            if (key.equals("id")) {
                this.idName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (key.equals("target")) {
                this.targetName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (key.equals("source")) {
                this.sourceName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (!key.equals("task")) continue;
            this.taskName = chartItemSpecification.getChartItemAttributes().get(key);
        }
        if (this.idName == null) {
            this.idName = this.getChartElement("copy").getAttributes().get("id").getDefaultValue();
        } else if (this.targetName == null) {
            this.targetName = this.getChartElement("copy").getAttributes().get("target").getDefaultValue();
        } else if (this.sourceName == null) {
            this.sourceName = this.getChartElement("copy").getAttributes().get("source").getDefaultValue();
        } else if (this.taskName == null) {
            this.taskName = this.getChartElement("copy").getAttributes().get("task").getDefaultValue();
        }
        this.usePartialTree = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "singlemalt", "use_partial_tree").toString().equals("true");
    }

    @Override
    public int preprocess(int signal) throws MaltChainedException {
        return signal;
    }

    @Override
    public int process(int signal) throws MaltChainedException {
        if (this.taskName.equals("terminals")) {
            if (this.cachedSource == null) {
                this.cachedSource = (TokenStructure)this.flowChartinstance.getFlowChartRegistry(TokenStructure.class, this.sourceName);
            }
            if (this.cachedTarget == null) {
                this.cachedTarget = (TokenStructure)this.flowChartinstance.getFlowChartRegistry(TokenStructure.class, this.targetName);
            }
            this.copyTerminalStructure(this.cachedSource, this.cachedTarget);
            if (this.usePartialTree && this.cachedSource instanceof DependencyStructure && this.cachedTarget instanceof DependencyStructure) {
                this.copyPartialDependencyStructure((DependencyStructure)this.cachedSource, (DependencyStructure)this.cachedTarget);
            }
        }
        return signal;
    }

    @Override
    public int postprocess(int signal) throws MaltChainedException {
        return signal;
    }

    @Override
    public void terminate() throws MaltChainedException {
        this.cachedSource = null;
        this.cachedTarget = null;
    }

    public void copyTerminalStructure(TokenStructure sourceGraph, TokenStructure targetGraph) throws MaltChainedException {
        targetGraph.clear();
        Iterator i$ = sourceGraph.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            TokenNode gnode = sourceGraph.getTokenNode(index);
            TokenNode pnode = targetGraph.addTokenNode(gnode.getIndex());
            for (SymbolTable table : gnode.getLabelTypes()) {
                pnode.addLabel(table, gnode.getLabelSymbol(table));
            }
        }
        if (sourceGraph.hasComments()) {
            for (int i = 1; i <= sourceGraph.nTokenNode() + 1; ++i) {
                ArrayList<String> commentList = sourceGraph.getComment(i);
                if (commentList == null) continue;
                for (int j = 0; j < commentList.size(); ++j) {
                    targetGraph.addComment(commentList.get(j), i);
                }
            }
        }
    }

    public void copyPartialDependencyStructure(DependencyStructure sourceGraph, DependencyStructure targetGraph) throws MaltChainedException {
        SymbolTable partHead = this.cachedSource.getSymbolTables().getSymbolTable("PARTHEAD");
        SymbolTable partDeprel = this.cachedSource.getSymbolTables().getSymbolTable("PARTDEPREL");
        if (partHead == null || partDeprel == null) {
            return;
        }
        SymbolTable deprel = this.cachedTarget.getSymbolTables().getSymbolTable("DEPREL");
        Iterator i$ = sourceGraph.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            TokenNode snode = sourceGraph.getTokenNode(index);
            TokenNode tnode = targetGraph.getTokenNode(index);
            if (snode == null || tnode == null) continue;
            int spartheadindex = Integer.parseInt(snode.getLabelSymbol(partHead));
            String spartdeprel = snode.getLabelSymbol(partDeprel);
            if (spartheadindex <= 0) continue;
            Edge tedge = targetGraph.addDependencyEdge(spartheadindex, snode.getIndex());
            tedge.addLabel(deprel, spartdeprel);
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
        sb.append("    copy ");
        sb.append("id:");
        sb.append(this.idName);
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

