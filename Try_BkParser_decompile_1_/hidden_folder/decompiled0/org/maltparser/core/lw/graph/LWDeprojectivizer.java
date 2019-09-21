/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.lw.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public final class LWDeprojectivizer {
    public static final int NONE = 0;
    public static final int BASELINE = 1;
    public static final int HEAD = 1;
    public static final int PATH = 1;
    public static final int HEADPATH = 1;
    public static final int TRACE = 1;

    public static int getMarkingStrategyInt(String markingStrategyString) {
        if (markingStrategyString.equalsIgnoreCase("none")) {
            return 0;
        }
        if (markingStrategyString.equalsIgnoreCase("baseline")) {
            return 1;
        }
        if (markingStrategyString.equalsIgnoreCase("head")) {
            return 1;
        }
        if (markingStrategyString.equalsIgnoreCase("path")) {
            return 1;
        }
        if (markingStrategyString.equalsIgnoreCase("head+path")) {
            return 1;
        }
        return markingStrategyString.equalsIgnoreCase("trace");
    }

    public void deprojectivize(DependencyStructure pdg, int markingStrategy) throws MaltChainedException {
        SymbolTable deprelSymbolTable = pdg.getSymbolTables().getSymbolTable("DEPREL");
        SymbolTable ppliftedSymbolTable = pdg.getSymbolTables().getSymbolTable("PPLIFTED");
        SymbolTable pppathSymbolTable = pdg.getSymbolTables().getSymbolTable("PPPATH");
        boolean[] nodeLifted = new boolean[pdg.nDependencyNode()];
        Arrays.fill(nodeLifted, false);
        boolean[] nodePath = new boolean[pdg.nDependencyNode()];
        Arrays.fill(nodePath, false);
        Object[] synacticHeadDeprel = new String[pdg.nDependencyNode()];
        Arrays.fill(synacticHeadDeprel, null);
        Iterator i$ = pdg.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            Edge e = pdg.getDependencyNode(index).getHeadEdge();
            if (!e.hasLabel(deprelSymbolTable)) continue;
            if (e.hasLabel(pppathSymbolTable) && pppathSymbolTable.getSymbolCodeToString(e.getLabelCode(pppathSymbolTable)).equals("#true#")) {
                nodePath[pdg.getDependencyNode((int)index).getIndex()] = true;
            }
            if (!e.hasLabel(ppliftedSymbolTable) || ppliftedSymbolTable.getSymbolCodeToString(e.getLabelCode(ppliftedSymbolTable)).equals("#false#")) continue;
            nodeLifted[index] = true;
            if (ppliftedSymbolTable.getSymbolCodeToString(e.getLabelCode(ppliftedSymbolTable)).equals("#true#")) continue;
            synacticHeadDeprel[index] = ppliftedSymbolTable.getSymbolCodeToString(e.getLabelCode(ppliftedSymbolTable));
        }
        this.deattachCoveredRootsForDeprojectivization(pdg, deprelSymbolTable);
        if (markingStrategy == 1 && this.needsDeprojectivizeWithHead(pdg, nodeLifted, nodePath, (String[])synacticHeadDeprel, deprelSymbolTable)) {
            this.deprojectivizeWithHead(pdg, pdg.getDependencyRoot(), nodeLifted, nodePath, (String[])synacticHeadDeprel, deprelSymbolTable);
        } else if (markingStrategy == 1) {
            this.deprojectivizeWithPath(pdg, pdg.getDependencyRoot(), nodeLifted, nodePath);
        } else if (markingStrategy == 1) {
            this.deprojectivizeWithHeadAndPath(pdg, pdg.getDependencyRoot(), nodeLifted, nodePath, (String[])synacticHeadDeprel, deprelSymbolTable);
        }
    }

    private void deattachCoveredRootsForDeprojectivization(DependencyStructure pdg, SymbolTable deprelSymbolTable) throws MaltChainedException {
        SymbolTable ppcoveredRootSymbolTable = pdg.getSymbolTables().getSymbolTable("PPCOVERED");
        Iterator i$ = pdg.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            Edge e = pdg.getDependencyNode(index).getHeadEdge();
            if (!e.hasLabel(deprelSymbolTable) || !e.hasLabel(ppcoveredRootSymbolTable) || !ppcoveredRootSymbolTable.getSymbolCodeToString(e.getLabelCode(ppcoveredRootSymbolTable)).equals("#true#")) continue;
            pdg.moveDependencyEdge(pdg.getDependencyRoot().getIndex(), pdg.getDependencyNode(index).getIndex());
        }
    }

    private boolean needsDeprojectivizeWithHead(DependencyStructure pdg, boolean[] nodeLifted, boolean[] nodePath, String[] synacticHeadDeprel, SymbolTable deprelSymbolTable) throws MaltChainedException {
        Iterator i$ = pdg.getDependencyIndices().iterator();
        while (i$.hasNext()) {
            DependencyNode node;
            int index = (Integer)i$.next();
            if (!nodeLifted[index] || this.breadthFirstSearchSortedByDistanceForHead(pdg, (node = pdg.getDependencyNode(index)).getHead(), node, synacticHeadDeprel[index], nodePath, deprelSymbolTable) == null) continue;
            return true;
        }
        return false;
    }

    private boolean deprojectivizeWithHead(DependencyStructure pdg, DependencyNode node, boolean[] nodeLifted, boolean[] nodePath, String[] synacticHeadDeprel, SymbolTable deprelSymbolTable) throws MaltChainedException {
        boolean success = true;
        boolean childSuccess = false;
        int childAttempts = 2;
        if (nodeLifted[node.getIndex()]) {
            String syntacticHeadDeprel = synacticHeadDeprel[node.getIndex()];
            DependencyNode possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForHead(pdg, node.getHead(), node, syntacticHeadDeprel, nodePath, deprelSymbolTable);
            if (possibleSyntacticHead != null) {
                pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
                nodeLifted[node.getIndex()] = false;
            } else {
                success = false;
            }
        }
        while (!childSuccess && childAttempts > 0) {
            childSuccess = true;
            List<DependencyNode> children = node.getListOfDependents();
            for (int i = 0; i < children.size(); ++i) {
                if (this.deprojectivizeWithHead(pdg, children.get(i), nodeLifted, nodePath, synacticHeadDeprel, deprelSymbolTable)) continue;
                childSuccess = false;
            }
            --childAttempts;
        }
        return childSuccess && success;
    }

    private DependencyNode breadthFirstSearchSortedByDistanceForHead(DependencyStructure dg, DependencyNode start, DependencyNode avoid, String syntacticHeadDeprel, boolean[] nodePath, SymbolTable deprelSymbolTable) throws MaltChainedException {
        ArrayList<DependencyNode> nodes = new ArrayList<DependencyNode>();
        nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, start, avoid, false, nodePath));
        while (nodes.size() > 0) {
            String dependentDeprel;
            DependencyNode dependent = (DependencyNode)nodes.remove(0);
            if (dependent.getHeadEdge().hasLabel(deprelSymbolTable) && (dependentDeprel = deprelSymbolTable.getSymbolCodeToString(dependent.getHeadEdge().getLabelCode(deprelSymbolTable))).equals(syntacticHeadDeprel)) {
                return dependent;
            }
            nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, dependent, avoid, false, nodePath));
        }
        return null;
    }

    private List<DependencyNode> findAllDependentsVectorSortedByDistanceToPProjNode(DependencyStructure dg, DependencyNode governor, DependencyNode avoid, boolean percentOnly, boolean[] nodePath) {
        int i;
        ArrayList<DependencyNode> output = new ArrayList<DependencyNode>();
        List<DependencyNode> dependents = governor.getListOfDependents();
        DependencyNode[] deps = new DependencyNode[dependents.size()];
        int[] distances = new int[dependents.size()];
        for (i = 0; i < dependents.size(); ++i) {
            distances[i] = Math.abs(dependents.get(i).getIndex() - avoid.getIndex());
            deps[i] = dependents.get(i);
        }
        if (distances.length > 1) {
            int n = distances.length;
            for (int i2 = 0; i2 < n; ++i2) {
                int smallest = i2;
                for (int j = i2; j < n; ++j) {
                    if (distances[j] >= distances[smallest]) continue;
                    smallest = j;
                }
                if (smallest == i2) continue;
                int tmpDist = distances[smallest];
                distances[smallest] = distances[i2];
                distances[i2] = tmpDist;
                DependencyNode tmpDep = deps[smallest];
                deps[smallest] = deps[i2];
                deps[i2] = tmpDep;
            }
        }
        for (i = 0; i < distances.length; ++i) {
            if (deps[i] == avoid || percentOnly && (!percentOnly || !nodePath[deps[i].getIndex()])) continue;
            output.add(deps[i]);
        }
        return output;
    }

    private boolean deprojectivizeWithPath(DependencyStructure pdg, DependencyNode node, boolean[] nodeLifted, boolean[] nodePath) throws MaltChainedException {
        DependencyNode possibleSyntacticHead;
        boolean success = true;
        boolean childSuccess = false;
        int childAttempts = 2;
        if (node.hasHead() && node.getHeadEdge().isLabeled() && nodeLifted[node.getIndex()] && nodePath[node.getIndex()]) {
            possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForPath(pdg, node.getHead(), node, nodePath);
            if (possibleSyntacticHead != null) {
                pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
                nodeLifted[node.getIndex()] = false;
            } else {
                success = false;
            }
        }
        if (node.hasHead() && node.getHeadEdge().isLabeled() && nodeLifted[node.getIndex()]) {
            possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForPath(pdg, node.getHead(), node, nodePath);
            if (possibleSyntacticHead != null) {
                pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
                nodeLifted[node.getIndex()] = false;
            } else {
                success = false;
            }
        }
        while (!childSuccess && childAttempts > 0) {
            childSuccess = true;
            List<DependencyNode> children = node.getListOfDependents();
            for (int i = 0; i < children.size(); ++i) {
                if (this.deprojectivizeWithPath(pdg, children.get(i), nodeLifted, nodePath)) continue;
                childSuccess = false;
            }
            --childAttempts;
        }
        return childSuccess && success;
    }

    private DependencyNode breadthFirstSearchSortedByDistanceForPath(DependencyStructure dg, DependencyNode start, DependencyNode avoid, boolean[] nodePath) {
        ArrayList<DependencyNode> nodes = new ArrayList<DependencyNode>();
        nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, start, avoid, true, nodePath));
        while (nodes.size() > 0) {
            DependencyNode dependent = (DependencyNode)nodes.remove(0);
            List<DependencyNode> newNodes = this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, dependent, avoid, true, nodePath);
            if (newNodes.size() == 0) {
                return dependent;
            }
            nodes.addAll(newNodes);
        }
        return null;
    }

    private boolean deprojectivizeWithHeadAndPath(DependencyStructure pdg, DependencyNode node, boolean[] nodeLifted, boolean[] nodePath, String[] synacticHeadDeprel, SymbolTable deprelSymbolTable) throws MaltChainedException {
        DependencyNode possibleSyntacticHead;
        boolean success = true;
        boolean childSuccess = false;
        int childAttempts = 2;
        if (node.hasHead() && node.getHeadEdge().isLabeled() && nodeLifted[node.getIndex()] && nodePath[node.getIndex()]) {
            possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForHeadAndPath(pdg, node.getHead(), node, synacticHeadDeprel[node.getIndex()], nodePath, deprelSymbolTable);
            if (possibleSyntacticHead != null) {
                pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
                nodeLifted[node.getIndex()] = false;
            } else {
                success = false;
            }
        }
        if (node.hasHead() && node.getHeadEdge().isLabeled() && nodeLifted[node.getIndex()]) {
            possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForHeadAndPath(pdg, node.getHead(), node, synacticHeadDeprel[node.getIndex()], nodePath, deprelSymbolTable);
            if (possibleSyntacticHead != null) {
                pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
                nodeLifted[node.getIndex()] = false;
            } else {
                success = false;
            }
        }
        while (!childSuccess && childAttempts > 0) {
            childSuccess = true;
            List<DependencyNode> children = node.getListOfDependents();
            for (int i = 0; i < children.size(); ++i) {
                if (this.deprojectivizeWithHeadAndPath(pdg, children.get(i), nodeLifted, nodePath, synacticHeadDeprel, deprelSymbolTable)) continue;
                childSuccess = false;
            }
            --childAttempts;
        }
        return childSuccess && success;
    }

    private DependencyNode breadthFirstSearchSortedByDistanceForHeadAndPath(DependencyStructure dg, DependencyNode start, DependencyNode avoid, String syntacticHeadDeprelCode, boolean[] nodePath, SymbolTable deprelSymbolTable) throws MaltChainedException {
        ArrayList<DependencyNode> nodes = new ArrayList<DependencyNode>();
        List<DependencyNode> newNodes = null;
        ArrayList<DependencyNode> secondChance = new ArrayList<DependencyNode>();
        nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, start, avoid, true, nodePath));
        while (nodes.size() > 0) {
            DependencyNode dependent = (DependencyNode)nodes.remove(0);
            newNodes = this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, dependent, avoid, true, nodePath);
            if (newNodes.size() == 0 && deprelSymbolTable.getSymbolCodeToString(dependent.getHeadEdge().getLabelCode(deprelSymbolTable)).equals(syntacticHeadDeprelCode)) {
                return dependent;
            }
            nodes.addAll(newNodes);
            if (!deprelSymbolTable.getSymbolCodeToString(dependent.getHeadEdge().getLabelCode(deprelSymbolTable)).equals(syntacticHeadDeprelCode) || newNodes.size() == 0) continue;
            secondChance.add(dependent);
        }
        if (secondChance.size() > 0) {
            return (DependencyNode)secondChance.get(0);
        }
        return null;
    }
}

