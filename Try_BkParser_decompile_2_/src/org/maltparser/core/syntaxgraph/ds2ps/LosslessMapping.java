/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.ds2ps;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.apache.log4j.Logger;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.MappablePhraseStructureGraph;
import org.maltparser.core.syntaxgraph.ds2ps.Dependency2PhraseStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.headrules.HeadRules;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class LosslessMapping
implements Dependency2PhraseStructure {
    private String DEPREL = "DEPREL";
    private String PHRASE = "PHRASE";
    private String HEADREL = "HEADREL";
    private String ATTACH = "ATTACH";
    private String CAT = "CAT";
    private String EDGELABEL;
    private final char EMPTY_SPINE = (char)42;
    private final String EMPTY_LABEL = "??";
    private final char SPINE_ELEMENT_SEPARATOR = (char)124;
    private final char LABEL_ELEMENT_SEPARATOR = (char)126;
    private final char QUESTIONMARK = (char)63;
    private String optionString;
    private HeadRules headRules;
    private DataFormatInstance dependencyDataFormatInstance;
    private DataFormatInstance phraseStructuretDataFormatInstance;
    private SymbolTableHandler symbolTableHandler;
    private boolean lockUpdate = false;
    private int nonTerminalCounter;
    private StringBuilder deprel;
    private StringBuilder headrel;
    private StringBuilder phrase;

    public LosslessMapping(DataFormatInstance dependencyDataFormatInstance, DataFormatInstance phraseStructuretDataFormatInstance, SymbolTableHandler symbolTableHandler) {
        this.symbolTableHandler = symbolTableHandler;
        this.setDependencyDataFormatInstance(dependencyDataFormatInstance);
        this.setPhraseStructuretDataFormatInstance(phraseStructuretDataFormatInstance);
        this.deprel = new StringBuilder();
        this.headrel = new StringBuilder();
        this.phrase = new StringBuilder();
        if (phraseStructuretDataFormatInstance.getPhraseStructureEdgeLabelColumnDescriptionSet().size() == 1) {
            for (ColumnDescription column : phraseStructuretDataFormatInstance.getPhraseStructureEdgeLabelColumnDescriptionSet()) {
                this.EDGELABEL = column.getName();
            }
        }
        this.clear();
    }

    public void clear() {
        this.nonTerminalCounter = 0;
    }

    public String getOptionString() {
        return this.optionString;
    }

    public void setOptionString(String optionString) {
        this.optionString = optionString;
    }

    public DataFormatInstance getDependencyDataFormatInstance() {
        return this.dependencyDataFormatInstance;
    }

    public void setDependencyDataFormatInstance(DataFormatInstance dependencyDataFormatInstance) {
        this.dependencyDataFormatInstance = dependencyDataFormatInstance;
    }

    public DataFormatInstance getPhraseStructuretDataFormatInstance() {
        return this.phraseStructuretDataFormatInstance;
    }

    public void setPhraseStructuretDataFormatInstance(DataFormatInstance phraseStructuretDataFormatInstance) {
        this.phraseStructuretDataFormatInstance = phraseStructuretDataFormatInstance;
    }

    public void update(MappablePhraseStructureGraph graph, Edge e, Object arg) throws MaltChainedException {
        if (!this.lockUpdate && e.getType() == 1 && e.getSource() instanceof DependencyNode && e.getTarget() instanceof DependencyNode && e.isLabeled() && e.getLabelSet().size() == 4) {
            this.updatePhraseStructureGraph(graph, e, false);
        }
    }

    public void updateDependenyGraph(MappablePhraseStructureGraph graph, PhraseStructureNode top) throws MaltChainedException {
        if (graph.nTokenNode() == 1 && graph.nNonTerminals() == 0) {
            Edge e = graph.addDependencyEdge(graph.getDependencyRoot(), graph.getDependencyNode(1));
            e.addLabel(graph.getSymbolTables().getSymbolTable(this.DEPREL), graph.getDefaultRootEdgeLabelSymbol(graph.getSymbolTables().getSymbolTable(this.DEPREL)));
            e.addLabel(graph.getSymbolTables().getSymbolTable(this.HEADREL), graph.getDefaultRootEdgeLabelSymbol(graph.getSymbolTables().getSymbolTable(this.HEADREL)));
            e.addLabel(graph.getSymbolTables().getSymbolTable(this.PHRASE), "*");
            e.addLabel(graph.getSymbolTables().getSymbolTable(this.ATTACH), graph.getDefaultRootEdgeLabelSymbol(graph.getSymbolTables().getSymbolTable(this.ATTACH)));
        } else {
            this.updateDependencyEdges(graph, top);
            this.updateDependenyLabels(graph);
        }
    }

    private void updateDependencyEdges(MappablePhraseStructureGraph graph, PhraseStructureNode top) throws MaltChainedException {
        if (top == null) {
            return;
        }
        DependencyNode head = null;
        DependencyNode dependent = null;
        if (top instanceof NonTerminalNode) {
            for (PhraseStructureNode node : ((NonTerminalNode)top).getChildren()) {
                if (node instanceof NonTerminalNode) {
                    this.updateDependencyEdges(graph, node);
                    continue;
                }
                head = ((NonTerminalNode)top).getLexicalHead(this.headRules);
                dependent = (DependencyNode)((Object)node);
                if (head == null || dependent == null || head == dependent) continue;
                this.lockUpdate = true;
                if (!dependent.hasHead()) {
                    graph.addDependencyEdge(head, dependent);
                } else if (head != dependent.getHead()) {
                    graph.moveDependencyEdge(head, dependent);
                }
                this.lockUpdate = false;
            }
        }
        head = null;
        if (top.getParent() != null) {
            head = ((NonTerminalNode)top.getParent()).getLexicalHead(this.headRules);
        } else if (top.isRoot()) {
            head = (DependencyNode)((Object)top);
        }
        if (top instanceof NonTerminalNode) {
            dependent = ((NonTerminalNode)top).getLexicalHead(this.headRules);
        } else if (!top.isRoot()) {
            dependent = (DependencyNode)((Object)top);
        }
        if (head != null && dependent != null && head != dependent) {
            this.lockUpdate = true;
            if (!dependent.hasHead()) {
                graph.addDependencyEdge(head, dependent);
            } else if (head != dependent.getHead()) {
                graph.moveDependencyEdge(head, dependent);
            }
            this.lockUpdate = false;
        }
    }

    private void updateDependenyLabels(MappablePhraseStructureGraph graph) throws MaltChainedException {
        Iterator i$ = graph.getTokenIndices().iterator();
        while (i$.hasNext()) {
            PhraseStructureNode top;
            int index = (Integer)i$.next();
            for (top = graph.getTokenNode((int)index); top != null && top.getParent() != null && graph.getTokenNode(index) == ((NonTerminalNode)top.getParent()).getLexicalHead(this.headRules); top = top.getParent()) {
            }
            this.lockUpdate = true;
            this.labelDependencyEdge(graph, graph.getTokenNode(index).getHeadEdge(), top);
            this.lockUpdate = false;
        }
    }

    private void labelDependencyEdge(MappablePhraseStructureGraph graph, Edge e, PhraseStructureNode top) throws MaltChainedException {
        PhraseStructureNode tmp;
        if (e == null) {
            return;
        }
        SymbolTableHandler symbolTables = graph.getSymbolTables();
        this.deprel.setLength(0);
        this.phrase.setLength(0);
        this.headrel.setLength(0);
        e.removeLabel(symbolTables.getSymbolTable(this.DEPREL));
        e.removeLabel(symbolTables.getSymbolTable(this.HEADREL));
        e.removeLabel(symbolTables.getSymbolTable(this.PHRASE));
        e.removeLabel(symbolTables.getSymbolTable(this.ATTACH));
        int i = 0;
        SortedMap<String, SymbolTable> edgeLabelSymbolTables = this.phraseStructuretDataFormatInstance.getPhraseStructureEdgeLabelSymbolTables(this.symbolTableHandler);
        SortedMap<String, SymbolTable> nodeLabelSymbolTables = this.phraseStructuretDataFormatInstance.getPhraseStructureNodeLabelSymbolTables(this.symbolTableHandler);
        if (!top.isRoot()) {
            for (String name : edgeLabelSymbolTables.keySet()) {
                if (top.hasParentEdgeLabel(symbolTables.getSymbolTable(name))) {
                    this.deprel.append(top.getParentEdgeLabelSymbol(symbolTables.getSymbolTable(name)));
                } else {
                    this.deprel.append("??");
                }
                if (++i >= edgeLabelSymbolTables.size()) continue;
                this.deprel.append('~');
            }
            if (this.deprel.length() != 0) {
                e.addLabel(symbolTables.getSymbolTable(this.DEPREL), this.deprel.toString());
            }
        } else {
            String deprelDefaultRootLabel = graph.getDefaultRootEdgeLabelSymbol(symbolTables.getSymbolTable(this.DEPREL));
            if (deprelDefaultRootLabel != null) {
                e.addLabel(symbolTables.getSymbolTable(this.DEPREL), deprelDefaultRootLabel);
            } else {
                e.addLabel(symbolTables.getSymbolTable(this.DEPREL), "??");
            }
        }
        for (tmp = (PhraseStructureNode)e.getTarget(); tmp != top && tmp.getParent() != null; tmp = tmp.getParent()) {
            i = 0;
            for (String name : edgeLabelSymbolTables.keySet()) {
                if (tmp.hasParentEdgeLabel(symbolTables.getSymbolTable(name))) {
                    this.headrel.append(tmp.getParentEdgeLabelSymbol(symbolTables.getSymbolTable(name)));
                } else {
                    this.headrel.append("??");
                }
                if (++i >= edgeLabelSymbolTables.size()) continue;
                this.headrel.append('~');
            }
            i = 0;
            this.headrel.append('|');
            for (String name : nodeLabelSymbolTables.keySet()) {
                if (tmp.getParent().hasLabel(symbolTables.getSymbolTable(name))) {
                    this.phrase.append(tmp.getParent().getLabelSymbol(symbolTables.getSymbolTable(name)));
                } else if (tmp.getParent().isRoot()) {
                    String deprelDefaultRootLabel = graph.getDefaultRootEdgeLabelSymbol(symbolTables.getSymbolTable(this.PHRASE));
                    if (deprelDefaultRootLabel != null) {
                        this.phrase.append(deprelDefaultRootLabel);
                    } else {
                        this.phrase.append("??");
                    }
                } else {
                    this.phrase.append("??");
                }
                if (++i >= nodeLabelSymbolTables.size()) continue;
                this.phrase.append('~');
            }
            this.phrase.append('|');
        }
        if (this.phrase.length() == 0) {
            this.headrel.append('*');
            this.phrase.append('*');
        } else {
            this.headrel.setLength(this.headrel.length() - 1);
            this.phrase.setLength(this.phrase.length() - 1);
        }
        e.addLabel(symbolTables.getSymbolTable(this.HEADREL), this.headrel.toString());
        e.addLabel(symbolTables.getSymbolTable(this.PHRASE), this.phrase.toString());
        int a = 0;
        tmp = (PhraseStructureNode)((Object)e.getSource());
        while (top.getParent() != null && tmp.getParent() != null && tmp.getParent() != top.getParent()) {
            ++a;
            tmp = tmp.getParent();
        }
        e.addLabel(symbolTables.getSymbolTable(this.ATTACH), Integer.toString(a));
    }

    public void connectUnattachedSpines(MappablePhraseStructureGraph graph) throws MaltChainedException {
        this.connectUnattachedSpines(graph, graph.getDependencyRoot());
        if (!graph.getPhraseStructureRoot().isLabeled()) {
            graph.getPhraseStructureRoot().addLabel(graph.getSymbolTables().addSymbolTable(this.CAT), graph.getDefaultRootEdgeLabelSymbol(graph.getSymbolTables().getSymbolTable(this.PHRASE)));
        }
    }

    private void connectUnattachedSpines(MappablePhraseStructureGraph graph, DependencyNode depNode) throws MaltChainedException {
        int i;
        if (!depNode.isRoot()) {
            PhraseStructureNode dependentSpine = (PhraseStructureNode)((Object)depNode);
            while (dependentSpine.getParent() != null) {
                dependentSpine = dependentSpine.getParent();
            }
            if (!dependentSpine.isRoot()) {
                this.updatePhraseStructureGraph(graph, depNode.getHeadEdge(), true);
            }
        }
        for (i = 0; i < depNode.getLeftDependentCount(); ++i) {
            this.connectUnattachedSpines(graph, depNode.getLeftDependent(i));
        }
        for (i = depNode.getRightDependentCount() - 1; i >= 0; --i) {
            this.connectUnattachedSpines(graph, depNode.getRightDependent(i));
        }
    }

    public void updatePhraseStructureGraph(MappablePhraseStructureGraph graph, Edge depEdge, boolean attachHeadSpineToRoot) throws MaltChainedException {
        PhraseStructureNode dependentSpine = (PhraseStructureNode)((Object)depEdge.getTarget());
        if (((PhraseStructureNode)((Object)depEdge.getTarget())).getParent() == null) {
            int i;
            String phraseSpineLabel = null;
            String edgeSpineLabel = null;
            int empty_label = 0;
            if (depEdge.hasLabel(graph.getSymbolTables().getSymbolTable(this.PHRASE))) {
                phraseSpineLabel = depEdge.getLabelSymbol(graph.getSymbolTables().getSymbolTable(this.PHRASE));
            }
            if (depEdge.hasLabel(graph.getSymbolTables().getSymbolTable(this.HEADREL))) {
                edgeSpineLabel = depEdge.getLabelSymbol(graph.getSymbolTables().getSymbolTable(this.HEADREL));
            }
            if (phraseSpineLabel != null && phraseSpineLabel.length() > 0 && phraseSpineLabel.charAt(0) != '*') {
                int ps = 0;
                int es = 0;
                int i2 = 0;
                int j = 0;
                int n = phraseSpineLabel.length() - 1;
                int m = edgeSpineLabel.length() - 1;
                PhraseStructureNode child = (PhraseStructureNode)((Object)depEdge.getTarget());
                do {
                    if (i2 <= n && phraseSpineLabel.charAt(i2) != '|') {
                        empty_label = phraseSpineLabel.charAt(i2) == '?' ? ++empty_label : 0;
                        ++i2;
                        continue;
                    }
                    dependentSpine = depEdge.getSource().isRoot() && i2 >= n ? graph.getPhraseStructureRoot() : graph.addNonTerminalNode(++this.nonTerminalCounter);
                    if (empty_label != 2 && ps != i2) {
                        dependentSpine.addLabel(graph.getSymbolTables().addSymbolTable(this.CAT), phraseSpineLabel.substring(ps, i2));
                    }
                    empty_label = 0;
                    if (edgeSpineLabel != null) {
                        while (j <= m && edgeSpineLabel.charAt(j) != '|') {
                            empty_label = edgeSpineLabel.charAt(j) == '?' ? ++empty_label : 0;
                            ++j;
                        }
                    }
                    this.lockUpdate = true;
                    Edge e = graph.addPhraseStructureEdge(dependentSpine, child);
                    if (empty_label != 2 && es != j && edgeSpineLabel != null && e != null) {
                        e.addLabel(graph.getSymbolTables().addSymbolTable(this.EDGELABEL), edgeSpineLabel.substring(es, j));
                    } else if (es == j) {
                        e.addLabel(graph.getSymbolTables().addSymbolTable(this.EDGELABEL), "??");
                    }
                    this.lockUpdate = false;
                    child = dependentSpine;
                    if (i2 >= n) break;
                    empty_label = 0;
                    ps = ++i2;
                    es = ++j;
                } while (true);
            }
            DependencyNode target = (DependencyNode)((Object)depEdge.getTarget());
            for (i = 0; i < target.getLeftDependentCount(); ++i) {
                this.updatePhraseStructureGraph(graph, target.getLeftDependent(i).getHeadEdge(), attachHeadSpineToRoot);
            }
            for (i = target.getRightDependentCount() - 1; i >= 0; --i) {
                this.updatePhraseStructureGraph(graph, target.getRightDependent(i).getHeadEdge(), attachHeadSpineToRoot);
            }
        } else {
            while (dependentSpine.getParent() != null && !dependentSpine.getParent().isRoot()) {
                dependentSpine = dependentSpine.getParent();
            }
        }
        PhraseStructureNode headSpine = null;
        if (((PhraseStructureNode)((Object)depEdge.getSource())).getParent() != null) {
            int a = 0;
            headSpine = ((PhraseStructureNode)((Object)depEdge.getSource())).getParent();
            if (depEdge.hasLabel(graph.getSymbolTables().getSymbolTable(this.ATTACH))) {
                try {
                    a = Integer.parseInt(depEdge.getLabelSymbol(graph.getSymbolTables().getSymbolTable(this.ATTACH)));
                }
                catch (NumberFormatException e) {
                    throw new MaltChainedException(e.getMessage());
                }
            }
            for (int i = 0; i < a && headSpine != null; headSpine = headSpine.getParent(), ++i) {
            }
            if ((headSpine == null || headSpine == dependentSpine) && attachHeadSpineToRoot) {
                headSpine = graph.getPhraseStructureRoot();
            }
            if (headSpine != null) {
                this.lockUpdate = true;
                Edge e = graph.addPhraseStructureEdge(headSpine, dependentSpine);
                if (depEdge.hasLabel(graph.getSymbolTables().getSymbolTable(this.DEPREL)) && !depEdge.getLabelSymbol(graph.getSymbolTables().getSymbolTable(this.DEPREL)).equals("??") & e != null) {
                    e.addLabel(graph.getSymbolTables().addSymbolTable(this.EDGELABEL), depEdge.getLabelSymbol(graph.getSymbolTables().getSymbolTable(this.DEPREL)));
                }
                this.lockUpdate = false;
            }
        } else if (depEdge.getSource().isRoot() && !depEdge.isLabeled()) {
            int i;
            headSpine = graph.getPhraseStructureRoot();
            this.lockUpdate = true;
            Edge e = graph.addPhraseStructureEdge(headSpine, dependentSpine);
            if (depEdge.hasLabel(graph.getSymbolTables().getSymbolTable(this.DEPREL)) && !depEdge.getLabelSymbol(graph.getSymbolTables().getSymbolTable(this.DEPREL)).equals("??") & e != null) {
                e.addLabel(graph.getSymbolTables().addSymbolTable(this.EDGELABEL), depEdge.getLabelSymbol(graph.getSymbolTables().getSymbolTable(this.DEPREL)));
            } else {
                e.addLabel(graph.getSymbolTables().addSymbolTable(this.EDGELABEL), graph.getDefaultRootEdgeLabelSymbol(graph.getSymbolTables().getSymbolTable(this.DEPREL)));
            }
            this.lockUpdate = false;
            DependencyNode target = (DependencyNode)((Object)depEdge.getTarget());
            for (i = 0; i < target.getLeftDependentCount(); ++i) {
                this.updatePhraseStructureGraph(graph, target.getLeftDependent(i).getHeadEdge(), attachHeadSpineToRoot);
            }
            for (i = target.getRightDependentCount() - 1; i >= 0; --i) {
                this.updatePhraseStructureGraph(graph, target.getRightDependent(i).getHeadEdge(), attachHeadSpineToRoot);
            }
        }
    }

    public HeadRules getHeadRules() {
        return this.headRules;
    }

    public void setHeadRules(HeadRules headRules) {
        this.headRules = headRules;
    }

    public void setHeadRules(String headRulesURL) throws MaltChainedException {
        if (headRulesURL != null && headRulesURL.length() > 0 && !headRulesURL.equals("*")) {
            this.headRules = new HeadRules(SystemLogger.logger(), this.phraseStructuretDataFormatInstance, this.symbolTableHandler);
            this.headRules.parseHeadRules(headRulesURL);
        }
    }
}

