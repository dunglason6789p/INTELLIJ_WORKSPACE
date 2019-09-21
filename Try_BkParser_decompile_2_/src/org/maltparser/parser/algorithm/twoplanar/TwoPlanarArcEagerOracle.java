/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.twoplanar;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.core.syntaxgraph.node.TokenNode;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.Oracle;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.algorithm.twoplanar.TwoPlanarConfig;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideUserAction;

public class TwoPlanarArcEagerOracle
extends Oracle {
    private static final int ANY_PLANE = 0;
    private static final int FIRST_PLANE = 1;
    private static final int SECOND_PLANE = 2;
    private static final int NO_PLANE = 3;
    private Map<Edge, Integer> linksToPlanes = new IdentityHashMap<Edge, Integer>();
    private Map<Edge, List<Edge>> crossingsGraph = null;

    public TwoPlanarArcEagerOracle(DependencyParserConfig manager, GuideUserHistory history) throws MaltChainedException {
        super(manager, history);
        this.setGuideName("Two-Planar");
    }

    @Override
    public GuideUserAction predict(DependencyStructure gold, ParserConfiguration config) throws MaltChainedException {
        TwoPlanarConfig planarConfig = (TwoPlanarConfig)config;
        DependencyStructure dg = planarConfig.getDependencyGraph();
        DependencyNode activeStackPeek = planarConfig.getActiveStack().peek();
        DependencyNode inactiveStackPeek = planarConfig.getInactiveStack().peek();
        int activeStackPeekIndex = activeStackPeek.getIndex();
        int inactiveStackPeekIndex = inactiveStackPeek.getIndex();
        int inputPeekIndex = planarConfig.getInput().peek().getIndex();
        if (this.crossingsGraph == null) {
            this.initCrossingsGraph(gold);
        }
        if (!activeStackPeek.isRoot() && gold.getTokenNode(activeStackPeekIndex).getHead().getIndex() == inputPeekIndex && !this.checkIfArcExists(dg, inputPeekIndex, activeStackPeekIndex)) {
            if (!planarConfig.getStackActivityState()) {
                this.propagatePlaneConstraint(gold.getTokenNode(activeStackPeekIndex).getHeadEdge(), 1);
            } else {
                this.propagatePlaneConstraint(gold.getTokenNode(activeStackPeekIndex).getHeadEdge(), 2);
            }
            return this.updateActionContainers(4, gold.getTokenNode(activeStackPeekIndex).getHeadEdge().getLabelSet());
        }
        if (gold.getTokenNode(inputPeekIndex).getHead().getIndex() == activeStackPeekIndex && !this.checkIfArcExists(dg, activeStackPeekIndex, inputPeekIndex)) {
            if (!planarConfig.getStackActivityState()) {
                this.propagatePlaneConstraint(gold.getTokenNode(inputPeekIndex).getHeadEdge(), 1);
            } else {
                this.propagatePlaneConstraint(gold.getTokenNode(inputPeekIndex).getHeadEdge(), 2);
            }
            return this.updateActionContainers(3, gold.getTokenNode(inputPeekIndex).getHeadEdge().getLabelSet());
        }
        if (!inactiveStackPeek.isRoot() && gold.getTokenNode(inactiveStackPeekIndex).getHead().getIndex() == inputPeekIndex && !this.checkIfArcExists(dg, inputPeekIndex, inactiveStackPeekIndex)) {
            return this.updateActionContainers(2, null);
        }
        if (gold.getTokenNode(inputPeekIndex).getHead().getIndex() == inactiveStackPeekIndex && !this.checkIfArcExists(dg, inactiveStackPeekIndex, inputPeekIndex)) {
            return this.updateActionContainers(2, null);
        }
        if (this.getFirstPendingLinkOnActivePlane(planarConfig, gold) != null) {
            return this.updateActionContainers(5, null);
        }
        if (this.getFirstPendingLinkOnInactivePlane(planarConfig, gold) != null) {
            return this.updateActionContainers(2, null);
        }
        return this.updateActionContainers(1, null);
    }

    private boolean checkIfArcExists(DependencyStructure dg, int index1, int index2) throws MaltChainedException {
        return dg.getTokenNode(index2).hasHead() && dg.getTokenNode(index2).getHead().getIndex() == index1;
    }

    @Override
    public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
        this.crossingsGraph = null;
        this.linksToPlanes.clear();
    }

    @Override
    public void terminate() throws MaltChainedException {
    }

    private static boolean cross(Edge e1, Edge e2) {
        int xSource = e1.getSource().getIndex();
        int xTarget = e1.getTarget().getIndex();
        int ySource = e2.getSource().getIndex();
        int yTarget = e2.getTarget().getIndex();
        int xMin = Math.min(xSource, xTarget);
        int xMax = Math.max(xSource, xTarget);
        int yMin = Math.min(ySource, yTarget);
        int yMax = Math.max(ySource, yTarget);
        return xMin < yMin && yMin < xMax && xMax < yMax || yMin < xMin && xMin < yMax && yMax < xMax;
    }

    private void initCrossingsGraph(DependencyStructure dg) {
        this.crossingsGraph = new IdentityHashMap<Edge, List<Edge>>();
        SortedSet<Edge> edges = dg.getEdges();
        for (Edge edge1 : edges) {
            for (Edge edge2 : edges) {
                if (edge1.getSource().getIndex() >= edge2.getSource().getIndex() || !TwoPlanarArcEagerOracle.cross(edge1, edge2)) continue;
                List<Edge> crossingEdge1 = this.crossingsGraph.get(edge1);
                if (crossingEdge1 == null) {
                    crossingEdge1 = new LinkedList<Edge>();
                    this.crossingsGraph.put(edge1, crossingEdge1);
                }
                crossingEdge1.add(edge2);
                List<Edge> crossingEdge2 = this.crossingsGraph.get(edge2);
                if (crossingEdge2 == null) {
                    crossingEdge2 = new LinkedList<Edge>();
                    this.crossingsGraph.put(edge2, crossingEdge2);
                }
                crossingEdge2.add(edge1);
            }
        }
    }

    private List<Edge> getCrossingEdges(Edge e) {
        return this.crossingsGraph.get(e);
    }

    private void setPlaneConstraint(Edge e, int requiredPlane) {
        this.linksToPlanes.put(e, requiredPlane);
    }

    private int getPlaneConstraint(Edge e) {
        Integer constr = this.linksToPlanes.get(e);
        if (constr == null) {
            this.setPlaneConstraint(e, 0);
            return 0;
        }
        return constr;
    }

    private void propagatePlaneConstraint(Edge e, int requiredPlane) {
        List<Edge> crossingEdges;
        this.setPlaneConstraint(e, requiredPlane);
        if ((requiredPlane == 1 || requiredPlane == 2) && (crossingEdges = this.getCrossingEdges(e)) != null) {
            for (Edge crossingEdge : crossingEdges) {
                assert (requiredPlane == 1 || requiredPlane == 2);
                int crossingEdgeConstraint = this.getPlaneConstraint(crossingEdge);
                if (crossingEdgeConstraint == 0) {
                    if (requiredPlane == 1) {
                        this.propagatePlaneConstraint(crossingEdge, 2);
                        continue;
                    }
                    if (requiredPlane != 2) continue;
                    this.propagatePlaneConstraint(crossingEdge, 1);
                    continue;
                }
                if (crossingEdgeConstraint == 3) continue;
                if (crossingEdgeConstraint == 1) {
                    if (requiredPlane != 1) continue;
                    this.propagatePlaneConstraint(crossingEdge, 3);
                    continue;
                }
                if (crossingEdgeConstraint != 2 || requiredPlane != 2) continue;
                this.propagatePlaneConstraint(crossingEdge, 3);
            }
        }
    }

    private int getLinkDecision(Edge e, TwoPlanarConfig config) {
        int constraint = this.getPlaneConstraint(e);
        if (constraint == 0) {
            if (!config.getStackActivityState()) {
                return 1;
            }
            return 2;
        }
        return constraint;
    }

    private Edge getFirstPendingLinkOnActivePlane(TwoPlanarConfig config, DependencyStructure gold) throws MaltChainedException {
        return this.getFirstPendingLinkOnPlane(config, gold, !config.getStackActivityState() ? 1 : 2, config.getActiveStack().peek().getIndex());
    }

    private Edge getFirstPendingLinkOnInactivePlane(TwoPlanarConfig config, DependencyStructure gold) throws MaltChainedException {
        return this.getFirstPendingLinkOnPlane(config, gold, !config.getStackActivityState() ? 2 : 1, config.getInactiveStack().peek().getIndex());
    }

    private Edge getFirstPendingLinkOnAnyPlane(TwoPlanarConfig config, DependencyStructure gold) throws MaltChainedException {
        int left2;
        Edge e1 = this.getFirstPendingLinkOnActivePlane(config, gold);
        Edge e2 = this.getFirstPendingLinkOnInactivePlane(config, gold);
        int left1 = Math.min(e1.getSource().getIndex(), e1.getTarget().getIndex());
        if (left1 > (left2 = Math.min(e2.getSource().getIndex(), e2.getTarget().getIndex()))) {
            return e1;
        }
        return e2;
    }

    private Edge getFirstPendingLinkOnPlane(TwoPlanarConfig config, DependencyStructure gold, int plane, int rightmostLimit) throws MaltChainedException {
        TwoPlanarConfig planarConfig = config;
        int inputPeekIndex = planarConfig.getInput().peek().getIndex();
        Edge current = null;
        int maxIndex = planarConfig.getRootHandling() == 1 ? -1 : 0;
        if (gold.getTokenNode(inputPeekIndex).hasLeftDependent() && gold.getTokenNode(inputPeekIndex).getLeftmostDependent().getIndex() < rightmostLimit) {
            SortedSet<DependencyNode> dependents = gold.getTokenNode(inputPeekIndex).getLeftDependents();
            for (DependencyNode dependent : dependents) {
                if (dependent.getIndex() <= maxIndex || dependent.getIndex() >= rightmostLimit || this.getLinkDecision(dependent.getHeadEdge(), config) != plane) continue;
                maxIndex = dependent.getIndex();
                current = dependent.getHeadEdge();
            }
        }
        if (gold.getTokenNode(inputPeekIndex).getHead().getIndex() < rightmostLimit && gold.getTokenNode(inputPeekIndex).getHead().getIndex() > maxIndex && this.getLinkDecision(gold.getTokenNode(inputPeekIndex).getHeadEdge(), config) == plane) {
            current = gold.getTokenNode(inputPeekIndex).getHeadEdge();
        }
        return current;
    }
}

