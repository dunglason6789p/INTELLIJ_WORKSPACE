/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.transform.pseudo;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class PseudoProjectivity {
    static int id = 0;
    private PseudoProjectiveEncoding markingStrategy;
    private CoveredRootAttachment rootAttachment;
    private LiftingOrder liftingOrder;
    private Logger configLogger;
    private SymbolTable deprelSymbolTable;
    private SymbolTable pppathSymbolTable;
    private SymbolTable ppliftedSymbolTable;
    private SymbolTable ppcoveredRootSymbolTable;
    private ColumnDescription deprelColumn;
    private ColumnDescription pppathColumn;
    private ColumnDescription ppliftedColumn;
    private ColumnDescription ppcoveredRootColumn;
    private Vector<Boolean> nodeLifted;
    private Vector<Vector<DependencyNode>> nodeTrace;
    private Vector<DependencyNode> headDeprel;
    private Vector<Boolean> nodePath;
    private Vector<Boolean> isCoveredRoot;
    private Vector<Integer> nodeRelationLength;
    private Vector<String> synacticHeadDeprel;

    public void initialize(String markingStrategyString, String coveredRoot, String liftingOrder, Logger configLogger, DataFormatInstance dataFormatInstance, SymbolTableHandler symbolTables) throws MaltChainedException {
        this.nodeLifted = new Vector();
        this.nodeTrace = new Vector();
        this.headDeprel = new Vector();
        this.nodePath = new Vector();
        this.isCoveredRoot = new Vector();
        this.nodeRelationLength = new Vector();
        this.synacticHeadDeprel = new Vector();
        this.configLogger = configLogger;
        if (markingStrategyString.equalsIgnoreCase("none")) {
            this.markingStrategy = PseudoProjectiveEncoding.NONE;
        } else if (markingStrategyString.equalsIgnoreCase("baseline")) {
            this.markingStrategy = PseudoProjectiveEncoding.BASELINE;
        } else if (markingStrategyString.equalsIgnoreCase("head")) {
            this.markingStrategy = PseudoProjectiveEncoding.HEAD;
        } else if (markingStrategyString.equalsIgnoreCase("path")) {
            this.markingStrategy = PseudoProjectiveEncoding.PATH;
        } else if (markingStrategyString.equalsIgnoreCase("head+path")) {
            this.markingStrategy = PseudoProjectiveEncoding.HEADPATH;
        } else if (markingStrategyString.equalsIgnoreCase("trace")) {
            this.markingStrategy = PseudoProjectiveEncoding.TRACE;
        }
        this.deprelColumn = dataFormatInstance.getColumnDescriptionByName("DEPREL");
        this.deprelSymbolTable = symbolTables.getSymbolTable(this.deprelColumn.getName());
        if (this.markingStrategy == PseudoProjectiveEncoding.HEAD || this.markingStrategy == PseudoProjectiveEncoding.PATH || this.markingStrategy == PseudoProjectiveEncoding.HEADPATH) {
            this.ppliftedColumn = dataFormatInstance.addInternalColumnDescription(symbolTables, "PPLIFTED", "DEPENDENCY_EDGE_LABEL", "BOOLEAN", "", this.deprelColumn.getNullValueStrategy());
            this.ppliftedSymbolTable = symbolTables.getSymbolTable(this.ppliftedColumn.getName());
            if (this.markingStrategy == PseudoProjectiveEncoding.PATH) {
                this.ppliftedSymbolTable.addSymbol("#true#");
                this.ppliftedSymbolTable.addSymbol("#false#");
            } else {
                this.ppliftedSymbolTable.addSymbol("#false#");
            }
        }
        if (this.markingStrategy == PseudoProjectiveEncoding.PATH || this.markingStrategy == PseudoProjectiveEncoding.HEADPATH) {
            this.pppathColumn = dataFormatInstance.addInternalColumnDescription(symbolTables, "PPPATH", "DEPENDENCY_EDGE_LABEL", "BOOLEAN", "", this.deprelColumn.getNullValueStrategy());
            this.pppathSymbolTable = symbolTables.getSymbolTable(this.pppathColumn.getName());
            this.pppathSymbolTable.addSymbol("#true#");
            this.pppathSymbolTable.addSymbol("#false#");
        }
        if (coveredRoot.equalsIgnoreCase("none")) {
            this.rootAttachment = CoveredRootAttachment.NONE;
        } else if (coveredRoot.equalsIgnoreCase("ignore")) {
            this.rootAttachment = CoveredRootAttachment.IGNORE;
        } else if (coveredRoot.equalsIgnoreCase("left")) {
            this.rootAttachment = CoveredRootAttachment.LEFT;
        } else if (coveredRoot.equalsIgnoreCase("right")) {
            this.rootAttachment = CoveredRootAttachment.RIGHT;
        } else if (coveredRoot.equalsIgnoreCase("head")) {
            this.rootAttachment = CoveredRootAttachment.HEAD;
        }
        if (this.rootAttachment != CoveredRootAttachment.NONE) {
            this.ppcoveredRootColumn = dataFormatInstance.addInternalColumnDescription(symbolTables, "PPCOVERED", "DEPENDENCY_EDGE_LABEL", "BOOLEAN", "", this.deprelColumn.getNullValueStrategy());
            this.ppcoveredRootSymbolTable = symbolTables.getSymbolTable(this.ppcoveredRootColumn.getName());
            this.ppcoveredRootSymbolTable.addSymbol("#true#");
            this.ppcoveredRootSymbolTable.addSymbol("#false#");
        }
        if (liftingOrder.equalsIgnoreCase("shortest")) {
            this.liftingOrder = LiftingOrder.SHORTEST;
        } else if (liftingOrder.equalsIgnoreCase("deepest")) {
            this.liftingOrder = LiftingOrder.DEEPEST;
        }
    }

    private void initProjectivization(DependencyStructure pdg) throws MaltChainedException {
        this.nodeLifted.clear();
        this.nodeTrace.clear();
        this.headDeprel.clear();
        this.nodePath.clear();
        this.isCoveredRoot.clear();
        this.nodeRelationLength.clear();
        Iterator i$ = pdg.getDependencyIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            this.nodeLifted.add(false);
            this.nodeTrace.add(new Vector());
            this.headDeprel.add(null);
            this.nodePath.add(false);
            this.isCoveredRoot.add(false);
            if (this.ppliftedSymbolTable != null && index != 0) {
                pdg.getDependencyNode(index).getHeadEdge().getLabelSet().put(this.ppliftedSymbolTable, this.ppliftedSymbolTable.getSymbolStringToCode("#false#"));
            }
            if (this.pppathSymbolTable != null && index != 0) {
                pdg.getDependencyNode(index).getHeadEdge().getLabelSet().put(this.pppathSymbolTable, this.pppathSymbolTable.getSymbolStringToCode("#false#"));
            }
            if (this.ppcoveredRootSymbolTable == null || index == 0) continue;
            pdg.getDependencyNode(index).getHeadEdge().getLabelSet().put(this.ppcoveredRootSymbolTable, this.ppcoveredRootSymbolTable.getSymbolStringToCode("#false#"));
        }
        this.computeRelationLength(pdg);
    }

    public void projectivize(DependencyStructure pdg) throws MaltChainedException {
        ++id;
        if (!pdg.isTree()) {
            this.configLogger.info("\n[Warning: Sentence '" + id + "' cannot projectivize, because the dependency graph is not a tree]\n");
            return;
        }
        this.initProjectivization(pdg);
        if (this.rootAttachment == CoveredRootAttachment.IGNORE) {
            if (this.markingStrategy != PseudoProjectiveEncoding.NONE) {
                while (!pdg.isProjective()) {
                    DependencyNode deepestNonProjectiveNode = this.liftingOrder == LiftingOrder.DEEPEST ? this.getDeepestNonProjectiveNode(pdg) : this.getShortestNonProjectiveNode(pdg);
                    if (this.attachCoveredRoots(pdg, deepestNonProjectiveNode)) continue;
                    this.nodeLifted.set(deepestNonProjectiveNode.getIndex(), true);
                    this.setHeadDeprel(deepestNonProjectiveNode, deepestNonProjectiveNode.getHead());
                    this.setPath(deepestNonProjectiveNode.getHead());
                    pdg.moveDependencyEdge(pdg.getDependencyNode(deepestNonProjectiveNode.getHead().getHead().getIndex()).getIndex(), deepestNonProjectiveNode.getIndex());
                }
                this.deattachCoveredRootsForProjectivization(pdg);
            }
        } else {
            if (this.rootAttachment != CoveredRootAttachment.NONE) {
                Iterator i$ = pdg.getTokenIndices().iterator();
                while (i$.hasNext()) {
                    int index = (Integer)i$.next();
                    this.attachCoveredRoots(pdg, pdg.getTokenNode(index));
                }
            }
            if (this.markingStrategy != PseudoProjectiveEncoding.NONE) {
                while (!pdg.isProjective()) {
                    DependencyNode deepestNonProjectiveNode = this.liftingOrder == LiftingOrder.DEEPEST ? this.getDeepestNonProjectiveNode(pdg) : this.getShortestNonProjectiveNode(pdg);
                    this.nodeLifted.set(deepestNonProjectiveNode.getIndex(), true);
                    this.setHeadDeprel(deepestNonProjectiveNode, deepestNonProjectiveNode.getHead());
                    this.setPath(deepestNonProjectiveNode.getHead());
                    pdg.moveDependencyEdge(pdg.getDependencyNode(deepestNonProjectiveNode.getHead().getHead().getIndex()).getIndex(), deepestNonProjectiveNode.getIndex());
                }
            }
        }
        this.assignPseudoProjectiveDeprels(pdg);
    }

    public void mergeArclabels(DependencyStructure pdg) throws MaltChainedException {
        this.assignPseudoProjectiveDeprelsForMerge(pdg);
    }

    public void splitArclabels(DependencyStructure pdg) throws MaltChainedException {
        String label;
        int index;
        int pathLabelIndex = -1;
        int movedLabelIndex = -1;
        this.initDeprojeciviztion(pdg);
        Iterator i$ = pdg.getTokenIndices().iterator();
        while (i$.hasNext()) {
            index = (Integer)i$.next();
            if (!pdg.getTokenNode(index).getHeadEdge().hasLabel(this.deprelSymbolTable)) continue;
            label = this.deprelSymbolTable.getSymbolCodeToString(pdg.getTokenNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable));
            if (label != null && (pathLabelIndex = label.indexOf("%")) != -1) {
                label = label.substring(0, pathLabelIndex);
                this.setLabel(pdg.getTokenNode(index), label);
                pdg.getTokenNode(index).getHeadEdge().addLabel(this.pppathSymbolTable, this.pppathSymbolTable.getSymbolStringToCode("#true#"));
            }
            if (label == null || (movedLabelIndex = label.indexOf("|")) == -1 || label.indexOf("|null") != -1) continue;
            if (movedLabelIndex + 1 < label.length()) {
                pdg.getTokenNode(index).getHeadEdge().addLabel(this.ppliftedSymbolTable, this.ppliftedSymbolTable.getSymbolStringToCode(label.substring(movedLabelIndex + 1)));
            } else {
                pdg.getTokenNode(index).getHeadEdge().addLabel(this.ppliftedSymbolTable, this.ppliftedSymbolTable.getSymbolStringToCode("#true#"));
            }
            label = label.substring(0, movedLabelIndex);
            this.setLabel(pdg.getTokenNode(index), label);
        }
        i$ = pdg.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int coveredArcLabelIndex;
            index = (Integer)i$.next();
            if (!pdg.getTokenNode(index).getHeadEdge().hasLabel(this.deprelSymbolTable) || (coveredArcLabelIndex = (label = this.deprelSymbolTable.getSymbolCodeToString(pdg.getTokenNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable))).indexOf("|null")) == -1) continue;
            label = label.substring(0, coveredArcLabelIndex);
            this.setLabel(pdg.getTokenNode(index), label);
            pdg.getTokenNode(index).getHeadEdge().addLabel(this.ppcoveredRootSymbolTable, this.ppcoveredRootSymbolTable.getSymbolStringToCode("#true#"));
        }
    }

    private void setHeadDeprel(DependencyNode node, DependencyNode parent) {
        if (this.headDeprel.get(node.getIndex()) == null) {
            this.headDeprel.set(node.getIndex(), parent);
        }
        this.nodeTrace.set(node.getIndex(), this.headDeprel);
    }

    private void setPath(DependencyNode node) {
        this.nodePath.set(node.getIndex(), true);
    }

    private boolean isCoveredRoot(DependencyNode node) {
        return this.isCoveredRoot.get(node.getIndex());
    }

    private void deattachCoveredRootsForProjectivization(DependencyStructure pdg) throws MaltChainedException {
        Iterator i$ = pdg.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            if (!this.isCoveredRoot(pdg.getTokenNode(index))) continue;
            pdg.moveDependencyEdge(pdg.getDependencyRoot().getIndex(), pdg.getTokenNode(index).getIndex());
        }
    }

    private boolean attachCoveredRoots(DependencyStructure pdg, DependencyNode deepest) throws MaltChainedException {
        boolean foundCoveredRoot = false;
        for (int i = Math.min((int)deepest.getIndex(), (int)deepest.getHead().getIndex()) + 1; i < Math.max(deepest.getIndex(), deepest.getHead().getIndex()); ++i) {
            int rightMostIndex;
            int leftMostIndex = pdg.getDependencyNode(i).getLeftmostProperDescendantIndex();
            if (leftMostIndex == -1) {
                leftMostIndex = i;
            }
            if ((rightMostIndex = pdg.getDependencyNode(i).getRightmostProperDescendantIndex()) == -1) {
                rightMostIndex = i;
            }
            if (this.nodeLifted.get(i).booleanValue() || !pdg.getDependencyNode(i).getHead().isRoot() || deepest.getHead().isRoot() || Math.min(deepest.getIndex(), deepest.getHead().getIndex()) >= leftMostIndex || rightMostIndex >= Math.max(deepest.getIndex(), deepest.getHead().getIndex())) continue;
            DependencyNode coveredRootHead = this.rootAttachment == CoveredRootAttachment.LEFT ? (deepest.getHead().getIndex() < deepest.getIndex() ? deepest.getHead() : deepest) : (this.rootAttachment == CoveredRootAttachment.RIGHT ? (deepest.getIndex() < deepest.getHead().getIndex() ? deepest.getHead() : deepest) : deepest.getHead());
            pdg.moveDependencyEdge(coveredRootHead.getIndex(), pdg.getDependencyNode(i).getIndex());
            this.setCoveredRoot(pdg.getDependencyNode(i));
            foundCoveredRoot = true;
        }
        return foundCoveredRoot;
    }

    private void setCoveredRoot(DependencyNode node) {
        this.isCoveredRoot.set(node.getIndex(), true);
    }

    private DependencyNode getDeepestNonProjectiveNode(DependencyStructure pdg) throws MaltChainedException {
        DependencyNode deepestNonProjectiveNode = null;
        Iterator i$ = pdg.getDependencyIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            if (pdg.getDependencyNode(index).isProjective() || deepestNonProjectiveNode != null && pdg.getDependencyNode(index).getDependencyNodeDepth() <= pdg.getDependencyNode(deepestNonProjectiveNode.getIndex()).getDependencyNodeDepth()) continue;
            deepestNonProjectiveNode = pdg.getDependencyNode(index);
        }
        return deepestNonProjectiveNode;
    }

    private DependencyNode getShortestNonProjectiveNode(DependencyStructure pdg) throws MaltChainedException {
        DependencyNode shortestNonProjectiveNode = null;
        Iterator i$ = pdg.getDependencyIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            if (pdg.getDependencyNode(index).isProjective() || shortestNonProjectiveNode != null && this.nodeRelationLength.get(index) >= this.nodeRelationLength.get(shortestNonProjectiveNode.getIndex())) continue;
            shortestNonProjectiveNode = pdg.getDependencyNode(index);
        }
        return shortestNonProjectiveNode;
    }

    private void computeRelationLength(DependencyStructure pdg) throws MaltChainedException {
        this.nodeRelationLength.add(0);
        Iterator i$ = pdg.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            this.nodeRelationLength.add(Math.abs(pdg.getDependencyNode(index).getIndex() - pdg.getDependencyNode(index).getHead().getIndex()));
        }
    }

    private void assignPseudoProjectiveDeprels(DependencyStructure pdg) throws MaltChainedException {
        Iterator i$ = pdg.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            if (!this.isCoveredRoot(pdg.getDependencyNode(index))) {
                int newLabelCode;
                if (this.markingStrategy == PseudoProjectiveEncoding.HEAD || this.markingStrategy == PseudoProjectiveEncoding.PATH || this.markingStrategy == PseudoProjectiveEncoding.HEADPATH) {
                    if (this.markingStrategy == PseudoProjectiveEncoding.PATH) {
                        newLabelCode = this.nodeLifted.get(index) != false ? this.ppliftedSymbolTable.getSymbolStringToCode("#true#") : this.ppliftedSymbolTable.getSymbolStringToCode("#false#");
                        pdg.getDependencyNode(index).getHeadEdge().addLabel(this.ppliftedSymbolTable, newLabelCode);
                    } else {
                        newLabelCode = this.nodeLifted.get(index) != false ? this.ppliftedSymbolTable.addSymbol(this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(this.headDeprel.get(index).getIndex()).getHeadEdge().getLabelCode(this.deprelSymbolTable))) : this.ppliftedSymbolTable.getSymbolStringToCode("#false#");
                        pdg.getDependencyNode(index).getHeadEdge().addLabel(this.ppliftedSymbolTable, newLabelCode);
                    }
                }
                if (this.markingStrategy != PseudoProjectiveEncoding.PATH && this.markingStrategy != PseudoProjectiveEncoding.HEADPATH) continue;
                newLabelCode = this.nodePath.get(index) != false ? this.pppathSymbolTable.getSymbolStringToCode("#true#") : this.pppathSymbolTable.getSymbolStringToCode("#false#");
                pdg.getDependencyNode(index).getHeadEdge().addLabel(this.pppathSymbolTable, newLabelCode);
                continue;
            }
            if (this.rootAttachment == CoveredRootAttachment.NONE || this.rootAttachment == CoveredRootAttachment.IGNORE) continue;
            pdg.getDependencyNode(index).getHeadEdge().addLabel(this.ppcoveredRootSymbolTable, this.ppcoveredRootSymbolTable.getSymbolStringToCode("#true#"));
        }
    }

    private void setLabel(DependencyNode node, String label) throws MaltChainedException {
        node.getHeadEdge().getLabelSet().put(this.deprelSymbolTable, this.deprelSymbolTable.addSymbol(label));
    }

    private void assignPseudoProjectiveDeprelsForMerge(DependencyStructure pdg) throws MaltChainedException {
        int index;
        Vector<String> originalDeprel = new Vector<String>();
        originalDeprel.add(null);
        Iterator i$ = pdg.getTokenIndices().iterator();
        while (i$.hasNext()) {
            index = (Integer)i$.next();
            originalDeprel.add(this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)));
        }
        i$ = pdg.getTokenIndices().iterator();
        while (i$.hasNext()) {
            index = (Integer)i$.next();
            String newLabel = null;
            if (!this.isCoveredRoot(pdg.getDependencyNode(index))) {
                if (this.markingStrategy == PseudoProjectiveEncoding.HEAD) {
                    if (this.nodeLifted.get(index).booleanValue()) {
                        newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|" + (String)originalDeprel.get(this.headDeprel.get(index).getIndex());
                    }
                } else if (this.markingStrategy == PseudoProjectiveEncoding.PATH) {
                    if (this.nodeLifted.get(index).booleanValue() && this.nodePath.get(index).booleanValue()) {
                        newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|%";
                    } else if (this.nodeLifted.get(index).booleanValue() && !this.nodePath.get(index).booleanValue()) {
                        newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|";
                    } else if (!this.nodeLifted.get(index).booleanValue() && this.nodePath.get(index).booleanValue()) {
                        newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "%";
                    }
                } else if (this.markingStrategy == PseudoProjectiveEncoding.HEADPATH) {
                    if (this.nodeLifted.get(index).booleanValue() && this.nodePath.get(index).booleanValue()) {
                        newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|" + (String)originalDeprel.get(this.headDeprel.get(index).getIndex()) + "%";
                    } else if (this.nodeLifted.get(index).booleanValue() && !this.nodePath.get(index).booleanValue()) {
                        newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|" + (String)originalDeprel.get(this.headDeprel.get(index).getIndex());
                    } else if (!this.nodeLifted.get(index).booleanValue() && this.nodePath.get(index).booleanValue()) {
                        newLabel = (String)originalDeprel.get(pdg.getDependencyNode(index).getIndex()) + "%";
                    }
                } else if (this.markingStrategy == PseudoProjectiveEncoding.TRACE && this.nodeLifted.get(index).booleanValue()) {
                    newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|";
                }
            } else if (this.rootAttachment != CoveredRootAttachment.NONE && this.rootAttachment != CoveredRootAttachment.IGNORE) {
                newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|null";
            }
            if (newLabel == null) continue;
            this.setLabel(pdg.getDependencyNode(index), newLabel);
        }
    }

    public void deprojectivize(DependencyStructure pdg) throws MaltChainedException {
        this.initDeprojeciviztion(pdg);
        Iterator i$ = pdg.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            if (!pdg.getDependencyNode(index).getHeadEdge().hasLabel(this.deprelSymbolTable)) continue;
            if (pdg.getDependencyNode(index).getHeadEdge().hasLabel(this.pppathSymbolTable) && this.pppathSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.pppathSymbolTable)).equals("#true#")) {
                this.setPath(pdg.getDependencyNode(index));
            }
            if (!pdg.getDependencyNode(index).getHeadEdge().hasLabel(this.ppliftedSymbolTable) || this.ppliftedSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.ppliftedSymbolTable)).equals("#false#")) continue;
            this.nodeLifted.set(index, true);
            if (this.ppliftedSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.ppliftedSymbolTable)).equals("#true#")) continue;
            this.synacticHeadDeprel.set(index, this.ppliftedSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.ppliftedSymbolTable)));
        }
        this.deattachCoveredRootsForDeprojectivization(pdg);
        if (this.markingStrategy == PseudoProjectiveEncoding.HEAD && this.needsDeprojectivizeWithHead(pdg)) {
            this.deprojectivizeWithHead(pdg, pdg.getDependencyRoot());
        } else if (this.markingStrategy == PseudoProjectiveEncoding.PATH) {
            this.deprojectivizeWithPath(pdg, pdg.getDependencyRoot());
        } else if (this.markingStrategy == PseudoProjectiveEncoding.HEADPATH) {
            this.deprojectivizeWithHeadAndPath(pdg, pdg.getDependencyRoot());
        }
    }

    private void initDeprojeciviztion(DependencyStructure pdg) {
        this.nodeLifted.clear();
        this.nodePath.clear();
        this.synacticHeadDeprel.clear();
        Iterator i$ = pdg.getDependencyIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            this.nodeLifted.add(false);
            this.nodePath.add(false);
            this.synacticHeadDeprel.add(null);
        }
    }

    private void deattachCoveredRootsForDeprojectivization(DependencyStructure pdg) throws MaltChainedException {
        Iterator i$ = pdg.getTokenIndices().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            if (!pdg.getDependencyNode(index).getHeadEdge().hasLabel(this.deprelSymbolTable) || !pdg.getDependencyNode(index).getHeadEdge().hasLabel(this.ppcoveredRootSymbolTable) || !this.ppcoveredRootSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.ppcoveredRootSymbolTable)).equals("#true#")) continue;
            pdg.moveDependencyEdge(pdg.getDependencyRoot().getIndex(), pdg.getDependencyNode(index).getIndex());
        }
    }

    private boolean needsDeprojectivizeWithHead(DependencyStructure pdg) throws MaltChainedException {
        Iterator i$ = pdg.getDependencyIndices().iterator();
        while (i$.hasNext()) {
            DependencyNode node;
            int index = (Integer)i$.next();
            if (!this.nodeLifted.get(index).booleanValue() || this.breadthFirstSearchSortedByDistanceForHead(pdg, (node = pdg.getDependencyNode(index)).getHead(), node, this.synacticHeadDeprel.get(index)) == null) continue;
            return true;
        }
        return false;
    }

    private boolean deprojectivizeWithHead(DependencyStructure pdg, DependencyNode node) throws MaltChainedException {
        boolean success = true;
        boolean childSuccess = false;
        int childAttempts = 2;
        if (this.nodeLifted.get(node.getIndex()).booleanValue()) {
            String syntacticHeadDeprel = this.synacticHeadDeprel.get(node.getIndex());
            DependencyNode possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForHead(pdg, node.getHead(), node, syntacticHeadDeprel);
            if (possibleSyntacticHead != null) {
                pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
                this.nodeLifted.set(node.getIndex(), false);
            } else {
                success = false;
            }
        }
        while (!childSuccess && childAttempts > 0) {
            DependencyNode child;
            childSuccess = true;
            Vector<DependencyNode> children = new Vector<DependencyNode>();
            int i = 0;
            while ((child = node.getLeftDependent(i)) != null) {
                children.add(child);
                ++i;
            }
            i = 0;
            while ((child = node.getRightDependent(i)) != null) {
                children.add(child);
                ++i;
            }
            for (i = 0; i < children.size(); ++i) {
                child = (DependencyNode)children.get(i);
                if (this.deprojectivizeWithHead(pdg, child)) continue;
                childSuccess = false;
            }
            --childAttempts;
        }
        return childSuccess && success;
    }

    private DependencyNode breadthFirstSearchSortedByDistanceForHead(DependencyStructure dg, DependencyNode start, DependencyNode avoid, String syntacticHeadDeprel) throws MaltChainedException {
        Vector<DependencyNode> nodes = new Vector<DependencyNode>();
        nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, start, avoid, false));
        while (nodes.size() > 0) {
            String dependentDeprel;
            DependencyNode dependent = (DependencyNode)nodes.remove(0);
            if (dependent.getHeadEdge().hasLabel(this.deprelSymbolTable) && (dependentDeprel = this.deprelSymbolTable.getSymbolCodeToString(dependent.getHeadEdge().getLabelCode(this.deprelSymbolTable))).equals(syntacticHeadDeprel)) {
                return dependent;
            }
            nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, dependent, avoid, false));
        }
        return null;
    }

    private Vector<DependencyNode> findAllDependentsVectorSortedByDistanceToPProjNode(DependencyStructure dg, DependencyNode governor, DependencyNode avoid, boolean percentOnly) {
        Vector<DependencyNode> output = new Vector<DependencyNode>();
        TreeSet<DependencyNode> dependents = new TreeSet<DependencyNode>();
        dependents.addAll(governor.getLeftDependents());
        dependents.addAll(governor.getRightDependents());
        DependencyNode[] deps = new DependencyNode[dependents.size()];
        int[] distances = new int[dependents.size()];
        int i = 0;
        for (DependencyNode dep : dependents) {
            distances[i] = Math.abs(dep.getIndex() - avoid.getIndex());
            deps[i] = dep;
            ++i;
        }
        if (distances.length > 1) {
            int n = distances.length;
            for (i = 0; i < n; ++i) {
                int smallest = i;
                for (int j = i; j < n; ++j) {
                    if (distances[j] >= distances[smallest]) continue;
                    smallest = j;
                }
                if (smallest == i) continue;
                int tmpDist = distances[smallest];
                distances[smallest] = distances[i];
                distances[i] = tmpDist;
                DependencyNode tmpDep = deps[smallest];
                deps[smallest] = deps[i];
                deps[i] = tmpDep;
            }
        }
        for (i = 0; i < distances.length; ++i) {
            if (deps[i] == avoid || percentOnly && (!percentOnly || !this.nodePath.get(deps[i].getIndex()).booleanValue())) continue;
            output.add(deps[i]);
        }
        return output;
    }

    private Vector<DependencyNode> findAllDependentsVectorSortedByDistanceToPProjNode2(DependencyStructure dg, DependencyNode governor, DependencyNode avoid, boolean percentOnly) {
        Vector<DependencyNode> dependents = new Vector<DependencyNode>();
        int i = governor.getLeftDependentCount() - 1;
        int j = 0;
        DependencyNode leftChild = governor.getLeftDependent(i--);
        DependencyNode rightChild = governor.getRightDependent(j++);
        while (leftChild != null && rightChild != null) {
            if (leftChild == avoid) {
                leftChild = governor.getLeftDependent(i--);
                continue;
            }
            if (rightChild == avoid) {
                rightChild = governor.getRightDependent(j++);
                continue;
            }
            if (Math.abs(leftChild.getIndex() - avoid.getIndex()) < Math.abs(rightChild.getIndex() - avoid.getIndex())) {
                if (!percentOnly || percentOnly && this.nodePath.get(leftChild.getIndex()).booleanValue()) {
                    dependents.add(leftChild);
                }
                leftChild = governor.getLeftDependent(i--);
                continue;
            }
            if (!percentOnly || percentOnly && this.nodePath.get(rightChild.getIndex()).booleanValue()) {
                dependents.add(rightChild);
            }
            rightChild = governor.getRightDependent(j++);
        }
        while (leftChild != null) {
            if (leftChild != avoid && (!percentOnly || percentOnly && this.nodePath.get(leftChild.getIndex()).booleanValue())) {
                dependents.add(leftChild);
            }
            leftChild = governor.getLeftDependent(i--);
        }
        while (rightChild != null) {
            if (rightChild != avoid && (!percentOnly || percentOnly && this.nodePath.get(rightChild.getIndex()).booleanValue())) {
                dependents.add(rightChild);
            }
            rightChild = governor.getRightDependent(j++);
        }
        return dependents;
    }

    private boolean deprojectivizeWithPath(DependencyStructure pdg, DependencyNode node) throws MaltChainedException {
        DependencyNode possibleSyntacticHead;
        boolean success = true;
        boolean childSuccess = false;
        int childAttempts = 2;
        if (node.hasHead() && node.getHeadEdge().isLabeled() && this.nodeLifted.get(node.getIndex()).booleanValue() && this.nodePath.get(node.getIndex()).booleanValue()) {
            possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForPath(pdg, node.getHead(), node);
            if (possibleSyntacticHead != null) {
                pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
                this.nodeLifted.set(node.getIndex(), false);
            } else {
                success = false;
            }
        }
        if (node.hasHead() && node.getHeadEdge().isLabeled() && this.nodeLifted.get(node.getIndex()).booleanValue()) {
            possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForPath(pdg, node.getHead(), node);
            if (possibleSyntacticHead != null) {
                pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
                this.nodeLifted.set(node.getIndex(), false);
            } else {
                success = false;
            }
        }
        while (!childSuccess && childAttempts > 0) {
            DependencyNode child;
            childSuccess = true;
            Vector<DependencyNode> children = new Vector<DependencyNode>();
            int i = 0;
            while ((child = node.getLeftDependent(i)) != null) {
                children.add(child);
                ++i;
            }
            i = 0;
            while ((child = node.getRightDependent(i)) != null) {
                children.add(child);
                ++i;
            }
            for (i = 0; i < children.size(); ++i) {
                child = (DependencyNode)children.get(i);
                if (this.deprojectivizeWithPath(pdg, child)) continue;
                childSuccess = false;
            }
            --childAttempts;
        }
        return childSuccess && success;
    }

    private DependencyNode breadthFirstSearchSortedByDistanceForPath(DependencyStructure dg, DependencyNode start, DependencyNode avoid) {
        Vector<DependencyNode> nodes = new Vector<DependencyNode>();
        nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, start, avoid, true));
        while (nodes.size() > 0) {
            DependencyNode dependent = (DependencyNode)nodes.remove(0);
            Vector<DependencyNode> newNodes = this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, dependent, avoid, true);
            if (newNodes.size() == 0) {
                return dependent;
            }
            nodes.addAll(newNodes);
        }
        return null;
    }

    private boolean deprojectivizeWithHeadAndPath(DependencyStructure pdg, DependencyNode node) throws MaltChainedException {
        DependencyNode possibleSyntacticHead;
        boolean success = true;
        boolean childSuccess = false;
        int childAttempts = 2;
        if (node.hasHead() && node.getHeadEdge().isLabeled() && this.nodeLifted.get(node.getIndex()).booleanValue() && this.nodePath.get(node.getIndex()).booleanValue()) {
            possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForHeadAndPath(pdg, node.getHead(), node, this.synacticHeadDeprel.get(node.getIndex()));
            if (possibleSyntacticHead != null) {
                pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
                this.nodeLifted.set(node.getIndex(), false);
            } else {
                success = false;
            }
        }
        if (node.hasHead() && node.getHeadEdge().isLabeled() && this.nodeLifted.get(node.getIndex()).booleanValue()) {
            possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForHeadAndPath(pdg, node.getHead(), node, this.synacticHeadDeprel.get(node.getIndex()));
            if (possibleSyntacticHead != null) {
                pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
                this.nodeLifted.set(node.getIndex(), false);
            } else {
                success = false;
            }
        }
        while (!childSuccess && childAttempts > 0) {
            DependencyNode child;
            childSuccess = true;
            Vector<DependencyNode> children = new Vector<DependencyNode>();
            int i = 0;
            while ((child = node.getLeftDependent(i)) != null) {
                children.add(child);
                ++i;
            }
            i = 0;
            while ((child = node.getRightDependent(i)) != null) {
                children.add(child);
                ++i;
            }
            for (i = 0; i < children.size(); ++i) {
                child = (DependencyNode)children.get(i);
                if (this.deprojectivizeWithHeadAndPath(pdg, child)) continue;
                childSuccess = false;
            }
            --childAttempts;
        }
        return childSuccess && success;
    }

    private DependencyNode breadthFirstSearchSortedByDistanceForHeadAndPath(DependencyStructure dg, DependencyNode start, DependencyNode avoid, String syntacticHeadDeprelCode) throws MaltChainedException {
        Vector<DependencyNode> nodes = new Vector<DependencyNode>();
        Vector<DependencyNode> newNodes = null;
        Vector<DependencyNode> secondChance = new Vector<DependencyNode>();
        nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, start, avoid, true));
        while (nodes.size() > 0) {
            DependencyNode dependent = (DependencyNode)nodes.remove(0);
            newNodes = this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, dependent, avoid, true);
            if (newNodes.size() == 0 && this.deprelSymbolTable.getSymbolCodeToString(dependent.getHeadEdge().getLabelCode(this.deprelSymbolTable)).equals(syntacticHeadDeprelCode)) {
                return dependent;
            }
            nodes.addAll(newNodes);
            if (!this.deprelSymbolTable.getSymbolCodeToString(dependent.getHeadEdge().getLabelCode(this.deprelSymbolTable)).equals(syntacticHeadDeprelCode) || newNodes.size() == 0) continue;
            secondChance.add(dependent);
        }
        if (secondChance.size() > 0) {
            return (DependencyNode)secondChance.firstElement();
        }
        return null;
    }

    private static enum LiftingOrder {
        SHORTEST,
        DEEPEST;
        
    }

    private static enum CoveredRootAttachment {
        NONE,
        IGNORE,
        LEFT,
        RIGHT,
        HEAD;
        
    }

    private static enum PseudoProjectiveEncoding {
        NONE,
        BASELINE,
        HEAD,
        PATH,
        HEADPATH,
        TRACE;
        
    }

}

