package org.maltparser.transform.pseudo;

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
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class PseudoProjectivity {
   static int id = 0;
   private PseudoProjectivity.PseudoProjectiveEncoding markingStrategy;
   private PseudoProjectivity.CoveredRootAttachment rootAttachment;
   private PseudoProjectivity.LiftingOrder liftingOrder;
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

   public PseudoProjectivity() {
   }

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
         this.markingStrategy = PseudoProjectivity.PseudoProjectiveEncoding.NONE;
      } else if (markingStrategyString.equalsIgnoreCase("baseline")) {
         this.markingStrategy = PseudoProjectivity.PseudoProjectiveEncoding.BASELINE;
      } else if (markingStrategyString.equalsIgnoreCase("head")) {
         this.markingStrategy = PseudoProjectivity.PseudoProjectiveEncoding.HEAD;
      } else if (markingStrategyString.equalsIgnoreCase("path")) {
         this.markingStrategy = PseudoProjectivity.PseudoProjectiveEncoding.PATH;
      } else if (markingStrategyString.equalsIgnoreCase("head+path")) {
         this.markingStrategy = PseudoProjectivity.PseudoProjectiveEncoding.HEADPATH;
      } else if (markingStrategyString.equalsIgnoreCase("trace")) {
         this.markingStrategy = PseudoProjectivity.PseudoProjectiveEncoding.TRACE;
      }

      this.deprelColumn = dataFormatInstance.getColumnDescriptionByName("DEPREL");
      this.deprelSymbolTable = symbolTables.getSymbolTable(this.deprelColumn.getName());
      if (this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.HEAD || this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.PATH || this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.HEADPATH) {
         this.ppliftedColumn = dataFormatInstance.addInternalColumnDescription(symbolTables, "PPLIFTED", "DEPENDENCY_EDGE_LABEL", "BOOLEAN", "", this.deprelColumn.getNullValueStrategy());
         this.ppliftedSymbolTable = symbolTables.getSymbolTable(this.ppliftedColumn.getName());
         if (this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.PATH) {
            this.ppliftedSymbolTable.addSymbol("#true#");
            this.ppliftedSymbolTable.addSymbol("#false#");
         } else {
            this.ppliftedSymbolTable.addSymbol("#false#");
         }
      }

      if (this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.PATH || this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.HEADPATH) {
         this.pppathColumn = dataFormatInstance.addInternalColumnDescription(symbolTables, "PPPATH", "DEPENDENCY_EDGE_LABEL", "BOOLEAN", "", this.deprelColumn.getNullValueStrategy());
         this.pppathSymbolTable = symbolTables.getSymbolTable(this.pppathColumn.getName());
         this.pppathSymbolTable.addSymbol("#true#");
         this.pppathSymbolTable.addSymbol("#false#");
      }

      if (coveredRoot.equalsIgnoreCase("none")) {
         this.rootAttachment = PseudoProjectivity.CoveredRootAttachment.NONE;
      } else if (coveredRoot.equalsIgnoreCase("ignore")) {
         this.rootAttachment = PseudoProjectivity.CoveredRootAttachment.IGNORE;
      } else if (coveredRoot.equalsIgnoreCase("left")) {
         this.rootAttachment = PseudoProjectivity.CoveredRootAttachment.LEFT;
      } else if (coveredRoot.equalsIgnoreCase("right")) {
         this.rootAttachment = PseudoProjectivity.CoveredRootAttachment.RIGHT;
      } else if (coveredRoot.equalsIgnoreCase("head")) {
         this.rootAttachment = PseudoProjectivity.CoveredRootAttachment.HEAD;
      }

      if (this.rootAttachment != PseudoProjectivity.CoveredRootAttachment.NONE) {
         this.ppcoveredRootColumn = dataFormatInstance.addInternalColumnDescription(symbolTables, "PPCOVERED", "DEPENDENCY_EDGE_LABEL", "BOOLEAN", "", this.deprelColumn.getNullValueStrategy());
         this.ppcoveredRootSymbolTable = symbolTables.getSymbolTable(this.ppcoveredRootColumn.getName());
         this.ppcoveredRootSymbolTable.addSymbol("#true#");
         this.ppcoveredRootSymbolTable.addSymbol("#false#");
      }

      if (liftingOrder.equalsIgnoreCase("shortest")) {
         this.liftingOrder = PseudoProjectivity.LiftingOrder.SHORTEST;
      } else if (liftingOrder.equalsIgnoreCase("deepest")) {
         this.liftingOrder = PseudoProjectivity.LiftingOrder.DEEPEST;
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

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         this.nodeLifted.add(false);
         this.nodeTrace.add(new Vector());
         this.headDeprel.add((Object)null);
         this.nodePath.add(false);
         this.isCoveredRoot.add(false);
         if (this.ppliftedSymbolTable != null && index != 0) {
            pdg.getDependencyNode(index).getHeadEdge().getLabelSet().put(this.ppliftedSymbolTable, this.ppliftedSymbolTable.getSymbolStringToCode("#false#"));
         }

         if (this.pppathSymbolTable != null && index != 0) {
            pdg.getDependencyNode(index).getHeadEdge().getLabelSet().put(this.pppathSymbolTable, this.pppathSymbolTable.getSymbolStringToCode("#false#"));
         }

         if (this.ppcoveredRootSymbolTable != null && index != 0) {
            pdg.getDependencyNode(index).getHeadEdge().getLabelSet().put(this.ppcoveredRootSymbolTable, this.ppcoveredRootSymbolTable.getSymbolStringToCode("#false#"));
         }
      }

      this.computeRelationLength(pdg);
   }

   public void projectivize(DependencyStructure pdg) throws MaltChainedException {
      ++id;
      if (!pdg.isTree()) {
         this.configLogger.info("\n[Warning: Sentence '" + id + "' cannot projectivize, because the dependency graph is not a tree]\n");
      } else {
         this.initProjectivization(pdg);
         DependencyNode deepestNonProjectiveNode;
         if (this.rootAttachment == PseudoProjectivity.CoveredRootAttachment.IGNORE) {
            if (this.markingStrategy != PseudoProjectivity.PseudoProjectiveEncoding.NONE) {
               while(!pdg.isProjective()) {
                  if (this.liftingOrder == PseudoProjectivity.LiftingOrder.DEEPEST) {
                     deepestNonProjectiveNode = this.getDeepestNonProjectiveNode(pdg);
                  } else {
                     deepestNonProjectiveNode = this.getShortestNonProjectiveNode(pdg);
                  }

                  if (!this.attachCoveredRoots(pdg, deepestNonProjectiveNode)) {
                     this.nodeLifted.set(deepestNonProjectiveNode.getIndex(), true);
                     this.setHeadDeprel(deepestNonProjectiveNode, deepestNonProjectiveNode.getHead());
                     this.setPath(deepestNonProjectiveNode.getHead());
                     pdg.moveDependencyEdge(pdg.getDependencyNode(deepestNonProjectiveNode.getHead().getHead().getIndex()).getIndex(), deepestNonProjectiveNode.getIndex());
                  }
               }

               this.deattachCoveredRootsForProjectivization(pdg);
            }
         } else {
            if (this.rootAttachment != PseudoProjectivity.CoveredRootAttachment.NONE) {
               Iterator i$ = pdg.getTokenIndices().iterator();

               while(i$.hasNext()) {
                  int index = (Integer)i$.next();
                  this.attachCoveredRoots(pdg, pdg.getTokenNode(index));
               }
            }

            if (this.markingStrategy != PseudoProjectivity.PseudoProjectiveEncoding.NONE) {
               while(!pdg.isProjective()) {
                  if (this.liftingOrder == PseudoProjectivity.LiftingOrder.DEEPEST) {
                     deepestNonProjectiveNode = this.getDeepestNonProjectiveNode(pdg);
                  } else {
                     deepestNonProjectiveNode = this.getShortestNonProjectiveNode(pdg);
                  }

                  this.nodeLifted.set(deepestNonProjectiveNode.getIndex(), true);
                  this.setHeadDeprel(deepestNonProjectiveNode, deepestNonProjectiveNode.getHead());
                  this.setPath(deepestNonProjectiveNode.getHead());
                  pdg.moveDependencyEdge(pdg.getDependencyNode(deepestNonProjectiveNode.getHead().getHead().getIndex()).getIndex(), deepestNonProjectiveNode.getIndex());
               }
            }
         }

         this.assignPseudoProjectiveDeprels(pdg);
      }
   }

   public void mergeArclabels(DependencyStructure pdg) throws MaltChainedException {
      this.assignPseudoProjectiveDeprelsForMerge(pdg);
   }

   public void splitArclabels(DependencyStructure pdg) throws MaltChainedException {
      int pathLabelIndex = true;
      int movedLabelIndex = true;
      this.initDeprojeciviztion(pdg);
      Iterator i$ = pdg.getTokenIndices().iterator();

      String label;
      int index;
      while(i$.hasNext()) {
         index = (Integer)i$.next();
         if (pdg.getTokenNode(index).getHeadEdge().hasLabel(this.deprelSymbolTable)) {
            label = this.deprelSymbolTable.getSymbolCodeToString(pdg.getTokenNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable));
            int pathLabelIndex;
            if (label != null && (pathLabelIndex = label.indexOf("%")) != -1) {
               label = label.substring(0, pathLabelIndex);
               this.setLabel(pdg.getTokenNode(index), label);
               pdg.getTokenNode(index).getHeadEdge().addLabel(this.pppathSymbolTable, this.pppathSymbolTable.getSymbolStringToCode("#true#"));
            }

            int movedLabelIndex;
            if (label != null && (movedLabelIndex = label.indexOf("|")) != -1 && label.indexOf("|null") == -1) {
               if (movedLabelIndex + 1 < label.length()) {
                  pdg.getTokenNode(index).getHeadEdge().addLabel(this.ppliftedSymbolTable, this.ppliftedSymbolTable.getSymbolStringToCode(label.substring(movedLabelIndex + 1)));
               } else {
                  pdg.getTokenNode(index).getHeadEdge().addLabel(this.ppliftedSymbolTable, this.ppliftedSymbolTable.getSymbolStringToCode("#true#"));
               }

               label = label.substring(0, movedLabelIndex);
               this.setLabel(pdg.getTokenNode(index), label);
            }
         }
      }

      i$ = pdg.getTokenIndices().iterator();

      while(i$.hasNext()) {
         index = (Integer)i$.next();
         if (pdg.getTokenNode(index).getHeadEdge().hasLabel(this.deprelSymbolTable)) {
            label = this.deprelSymbolTable.getSymbolCodeToString(pdg.getTokenNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable));
            int coveredArcLabelIndex;
            if ((coveredArcLabelIndex = label.indexOf("|null")) != -1) {
               label = label.substring(0, coveredArcLabelIndex);
               this.setLabel(pdg.getTokenNode(index), label);
               pdg.getTokenNode(index).getHeadEdge().addLabel(this.ppcoveredRootSymbolTable, this.ppcoveredRootSymbolTable.getSymbolStringToCode("#true#"));
            }
         }
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
      return (Boolean)this.isCoveredRoot.get(node.getIndex());
   }

   private void deattachCoveredRootsForProjectivization(DependencyStructure pdg) throws MaltChainedException {
      Iterator i$ = pdg.getTokenIndices().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         if (this.isCoveredRoot(pdg.getTokenNode(index))) {
            pdg.moveDependencyEdge(pdg.getDependencyRoot().getIndex(), pdg.getTokenNode(index).getIndex());
         }
      }

   }

   private boolean attachCoveredRoots(DependencyStructure pdg, DependencyNode deepest) throws MaltChainedException {
      boolean foundCoveredRoot = false;

      for(int i = Math.min(deepest.getIndex(), deepest.getHead().getIndex()) + 1; i < Math.max(deepest.getIndex(), deepest.getHead().getIndex()); ++i) {
         int leftMostIndex = pdg.getDependencyNode(i).getLeftmostProperDescendantIndex();
         if (leftMostIndex == -1) {
            leftMostIndex = i;
         }

         int rightMostIndex = pdg.getDependencyNode(i).getRightmostProperDescendantIndex();
         if (rightMostIndex == -1) {
            rightMostIndex = i;
         }

         if (!(Boolean)this.nodeLifted.get(i) && pdg.getDependencyNode(i).getHead().isRoot() && !deepest.getHead().isRoot() && Math.min(deepest.getIndex(), deepest.getHead().getIndex()) < leftMostIndex && rightMostIndex < Math.max(deepest.getIndex(), deepest.getHead().getIndex())) {
            DependencyNode coveredRootHead;
            if (this.rootAttachment == PseudoProjectivity.CoveredRootAttachment.LEFT) {
               if (deepest.getHead().getIndex() < deepest.getIndex()) {
                  coveredRootHead = deepest.getHead();
               } else {
                  coveredRootHead = deepest;
               }
            } else if (this.rootAttachment == PseudoProjectivity.CoveredRootAttachment.RIGHT) {
               if (deepest.getIndex() < deepest.getHead().getIndex()) {
                  coveredRootHead = deepest.getHead();
               } else {
                  coveredRootHead = deepest;
               }
            } else {
               coveredRootHead = deepest.getHead();
            }

            pdg.moveDependencyEdge(coveredRootHead.getIndex(), pdg.getDependencyNode(i).getIndex());
            this.setCoveredRoot(pdg.getDependencyNode(i));
            foundCoveredRoot = true;
         }
      }

      return foundCoveredRoot;
   }

   private void setCoveredRoot(DependencyNode node) {
      this.isCoveredRoot.set(node.getIndex(), true);
   }

   private DependencyNode getDeepestNonProjectiveNode(DependencyStructure pdg) throws MaltChainedException {
      DependencyNode deepestNonProjectiveNode = null;
      Iterator i$ = pdg.getDependencyIndices().iterator();

      while(true) {
         int index;
         do {
            do {
               if (!i$.hasNext()) {
                  return deepestNonProjectiveNode;
               }

               index = (Integer)i$.next();
            } while(pdg.getDependencyNode(index).isProjective());
         } while(deepestNonProjectiveNode != null && pdg.getDependencyNode(index).getDependencyNodeDepth() <= pdg.getDependencyNode(deepestNonProjectiveNode.getIndex()).getDependencyNodeDepth());

         deepestNonProjectiveNode = pdg.getDependencyNode(index);
      }
   }

   private DependencyNode getShortestNonProjectiveNode(DependencyStructure pdg) throws MaltChainedException {
      DependencyNode shortestNonProjectiveNode = null;
      Iterator i$ = pdg.getDependencyIndices().iterator();

      while(true) {
         int index;
         do {
            do {
               if (!i$.hasNext()) {
                  return shortestNonProjectiveNode;
               }

               index = (Integer)i$.next();
            } while(pdg.getDependencyNode(index).isProjective());
         } while(shortestNonProjectiveNode != null && (Integer)this.nodeRelationLength.get(index) >= (Integer)this.nodeRelationLength.get(shortestNonProjectiveNode.getIndex()));

         shortestNonProjectiveNode = pdg.getDependencyNode(index);
      }
   }

   private void computeRelationLength(DependencyStructure pdg) throws MaltChainedException {
      this.nodeRelationLength.add(0);
      Iterator i$ = pdg.getTokenIndices().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         this.nodeRelationLength.add(Math.abs(pdg.getDependencyNode(index).getIndex() - pdg.getDependencyNode(index).getHead().getIndex()));
      }

   }

   private void assignPseudoProjectiveDeprels(DependencyStructure pdg) throws MaltChainedException {
      Iterator i$ = pdg.getTokenIndices().iterator();

      while(true) {
         int newLabelCode;
         int index;
         label56:
         do {
            while(i$.hasNext()) {
               index = (Integer)i$.next();
               if (!this.isCoveredRoot(pdg.getDependencyNode(index))) {
                  if (this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.HEAD || this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.PATH || this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.HEADPATH) {
                     if (this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.PATH) {
                        if ((Boolean)this.nodeLifted.get(index)) {
                           newLabelCode = this.ppliftedSymbolTable.getSymbolStringToCode("#true#");
                        } else {
                           newLabelCode = this.ppliftedSymbolTable.getSymbolStringToCode("#false#");
                        }

                        pdg.getDependencyNode(index).getHeadEdge().addLabel(this.ppliftedSymbolTable, newLabelCode);
                     } else {
                        if ((Boolean)this.nodeLifted.get(index)) {
                           newLabelCode = this.ppliftedSymbolTable.addSymbol(this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(((DependencyNode)this.headDeprel.get(index)).getIndex()).getHeadEdge().getLabelCode(this.deprelSymbolTable)));
                        } else {
                           newLabelCode = this.ppliftedSymbolTable.getSymbolStringToCode("#false#");
                        }

                        pdg.getDependencyNode(index).getHeadEdge().addLabel(this.ppliftedSymbolTable, newLabelCode);
                     }
                  }
                  continue label56;
               }

               if (this.rootAttachment != PseudoProjectivity.CoveredRootAttachment.NONE && this.rootAttachment != PseudoProjectivity.CoveredRootAttachment.IGNORE) {
                  pdg.getDependencyNode(index).getHeadEdge().addLabel(this.ppcoveredRootSymbolTable, this.ppcoveredRootSymbolTable.getSymbolStringToCode("#true#"));
               }
            }

            return;
         } while(this.markingStrategy != PseudoProjectivity.PseudoProjectiveEncoding.PATH && this.markingStrategy != PseudoProjectivity.PseudoProjectiveEncoding.HEADPATH);

         if ((Boolean)this.nodePath.get(index)) {
            newLabelCode = this.pppathSymbolTable.getSymbolStringToCode("#true#");
         } else {
            newLabelCode = this.pppathSymbolTable.getSymbolStringToCode("#false#");
         }

         pdg.getDependencyNode(index).getHeadEdge().addLabel(this.pppathSymbolTable, newLabelCode);
      }
   }

   private void setLabel(DependencyNode node, String label) throws MaltChainedException {
      node.getHeadEdge().getLabelSet().put(this.deprelSymbolTable, this.deprelSymbolTable.addSymbol(label));
   }

   private void assignPseudoProjectiveDeprelsForMerge(DependencyStructure pdg) throws MaltChainedException {
      Vector<String> originalDeprel = new Vector();
      originalDeprel.add((Object)null);
      Iterator i$ = pdg.getTokenIndices().iterator();

      int index;
      while(i$.hasNext()) {
         index = (Integer)i$.next();
         originalDeprel.add(this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)));
      }

      i$ = pdg.getTokenIndices().iterator();

      while(i$.hasNext()) {
         index = (Integer)i$.next();
         String newLabel = null;
         if (!this.isCoveredRoot(pdg.getDependencyNode(index))) {
            if (this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.HEAD) {
               if ((Boolean)this.nodeLifted.get(index)) {
                  newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|" + (String)originalDeprel.get(((DependencyNode)this.headDeprel.get(index)).getIndex());
               }
            } else if (this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.PATH) {
               if ((Boolean)this.nodeLifted.get(index) && (Boolean)this.nodePath.get(index)) {
                  newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|%";
               } else if ((Boolean)this.nodeLifted.get(index) && !(Boolean)this.nodePath.get(index)) {
                  newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|";
               } else if (!(Boolean)this.nodeLifted.get(index) && (Boolean)this.nodePath.get(index)) {
                  newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "%";
               }
            } else if (this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.HEADPATH) {
               if ((Boolean)this.nodeLifted.get(index) && (Boolean)this.nodePath.get(index)) {
                  newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|" + (String)originalDeprel.get(((DependencyNode)this.headDeprel.get(index)).getIndex()) + "%";
               } else if ((Boolean)this.nodeLifted.get(index) && !(Boolean)this.nodePath.get(index)) {
                  newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|" + (String)originalDeprel.get(((DependencyNode)this.headDeprel.get(index)).getIndex());
               } else if (!(Boolean)this.nodeLifted.get(index) && (Boolean)this.nodePath.get(index)) {
                  newLabel = (String)originalDeprel.get(pdg.getDependencyNode(index).getIndex()) + "%";
               }
            } else if (this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.TRACE && (Boolean)this.nodeLifted.get(index)) {
               newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|";
            }
         } else if (this.rootAttachment != PseudoProjectivity.CoveredRootAttachment.NONE && this.rootAttachment != PseudoProjectivity.CoveredRootAttachment.IGNORE) {
            newLabel = this.deprelSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.deprelSymbolTable)) + "|null";
         }

         if (newLabel != null) {
            this.setLabel(pdg.getDependencyNode(index), newLabel);
         }
      }

   }

   public void deprojectivize(DependencyStructure pdg) throws MaltChainedException {
      this.initDeprojeciviztion(pdg);
      Iterator i$ = pdg.getTokenIndices().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         if (pdg.getDependencyNode(index).getHeadEdge().hasLabel(this.deprelSymbolTable)) {
            if (pdg.getDependencyNode(index).getHeadEdge().hasLabel(this.pppathSymbolTable) && this.pppathSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.pppathSymbolTable)).equals("#true#")) {
               this.setPath(pdg.getDependencyNode(index));
            }

            if (pdg.getDependencyNode(index).getHeadEdge().hasLabel(this.ppliftedSymbolTable) && !this.ppliftedSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.ppliftedSymbolTable)).equals("#false#")) {
               this.nodeLifted.set(index, true);
               if (!this.ppliftedSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.ppliftedSymbolTable)).equals("#true#")) {
                  this.synacticHeadDeprel.set(index, this.ppliftedSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.ppliftedSymbolTable)));
               }
            }
         }
      }

      this.deattachCoveredRootsForDeprojectivization(pdg);
      if (this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.HEAD && this.needsDeprojectivizeWithHead(pdg)) {
         this.deprojectivizeWithHead(pdg, pdg.getDependencyRoot());
      } else if (this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.PATH) {
         this.deprojectivizeWithPath(pdg, pdg.getDependencyRoot());
      } else if (this.markingStrategy == PseudoProjectivity.PseudoProjectiveEncoding.HEADPATH) {
         this.deprojectivizeWithHeadAndPath(pdg, pdg.getDependencyRoot());
      }

   }

   private void initDeprojeciviztion(DependencyStructure pdg) {
      this.nodeLifted.clear();
      this.nodePath.clear();
      this.synacticHeadDeprel.clear();
      Iterator i$ = pdg.getDependencyIndices().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         this.nodeLifted.add(false);
         this.nodePath.add(false);
         this.synacticHeadDeprel.add((Object)null);
      }

   }

   private void deattachCoveredRootsForDeprojectivization(DependencyStructure pdg) throws MaltChainedException {
      Iterator i$ = pdg.getTokenIndices().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         if (pdg.getDependencyNode(index).getHeadEdge().hasLabel(this.deprelSymbolTable) && pdg.getDependencyNode(index).getHeadEdge().hasLabel(this.ppcoveredRootSymbolTable) && this.ppcoveredRootSymbolTable.getSymbolCodeToString(pdg.getDependencyNode(index).getHeadEdge().getLabelCode(this.ppcoveredRootSymbolTable)).equals("#true#")) {
            pdg.moveDependencyEdge(pdg.getDependencyRoot().getIndex(), pdg.getDependencyNode(index).getIndex());
         }
      }

   }

   private boolean needsDeprojectivizeWithHead(DependencyStructure pdg) throws MaltChainedException {
      Iterator i$ = pdg.getDependencyIndices().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         if ((Boolean)this.nodeLifted.get(index)) {
            DependencyNode node = pdg.getDependencyNode(index);
            if (this.breadthFirstSearchSortedByDistanceForHead(pdg, node.getHead(), node, (String)this.synacticHeadDeprel.get(index)) != null) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean deprojectivizeWithHead(DependencyStructure pdg, DependencyNode node) throws MaltChainedException {
      boolean success = true;
      boolean childSuccess = false;
      int childAttempts = 2;
      if ((Boolean)this.nodeLifted.get(node.getIndex())) {
         String syntacticHeadDeprel = (String)this.synacticHeadDeprel.get(node.getIndex());
         DependencyNode possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForHead(pdg, node.getHead(), node, syntacticHeadDeprel);
         if (possibleSyntacticHead != null) {
            pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
            this.nodeLifted.set(node.getIndex(), false);
         } else {
            success = false;
         }
      }

      while(!childSuccess && childAttempts > 0) {
         childSuccess = true;
         Vector<DependencyNode> children = new Vector();

         int i;
         DependencyNode child;
         for(i = 0; (child = node.getLeftDependent(i)) != null; ++i) {
            children.add(child);
         }

         for(i = 0; (child = node.getRightDependent(i)) != null; ++i) {
            children.add(child);
         }

         for(i = 0; i < children.size(); ++i) {
            child = (DependencyNode)children.get(i);
            if (!this.deprojectivizeWithHead(pdg, child)) {
               childSuccess = false;
            }
         }

         --childAttempts;
      }

      return childSuccess && success;
   }

   private DependencyNode breadthFirstSearchSortedByDistanceForHead(DependencyStructure dg, DependencyNode start, DependencyNode avoid, String syntacticHeadDeprel) throws MaltChainedException {
      Vector<DependencyNode> nodes = new Vector();
      nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, start, avoid, false));

      DependencyNode dependent;
      for(; nodes.size() > 0; nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, dependent, avoid, false))) {
         dependent = (DependencyNode)nodes.remove(0);
         if (dependent.getHeadEdge().hasLabel(this.deprelSymbolTable)) {
            String dependentDeprel = this.deprelSymbolTable.getSymbolCodeToString(dependent.getHeadEdge().getLabelCode(this.deprelSymbolTable));
            if (dependentDeprel.equals(syntacticHeadDeprel)) {
               return dependent;
            }
         }
      }

      return null;
   }

   private Vector<DependencyNode> findAllDependentsVectorSortedByDistanceToPProjNode(DependencyStructure dg, DependencyNode governor, DependencyNode avoid, boolean percentOnly) {
      Vector<DependencyNode> output = new Vector();
      SortedSet<DependencyNode> dependents = new TreeSet();
      dependents.addAll(governor.getLeftDependents());
      dependents.addAll(governor.getRightDependents());
      DependencyNode[] deps = new DependencyNode[dependents.size()];
      int[] distances = new int[dependents.size()];
      int i = 0;

      for(Iterator i$ = dependents.iterator(); i$.hasNext(); ++i) {
         DependencyNode dep = (DependencyNode)i$.next();
         distances[i] = Math.abs(dep.getIndex() - avoid.getIndex());
         deps[i] = dep;
      }

      if (distances.length > 1) {
         int n = distances.length;

         for(i = 0; i < n; ++i) {
            int smallest = i;

            for(int j = i; j < n; ++j) {
               if (distances[j] < distances[smallest]) {
                  smallest = j;
               }
            }

            if (smallest != i) {
               int tmpDist = distances[smallest];
               distances[smallest] = distances[i];
               distances[i] = tmpDist;
               DependencyNode tmpDep = deps[smallest];
               deps[smallest] = deps[i];
               deps[i] = tmpDep;
            }
         }
      }

      for(i = 0; i < distances.length; ++i) {
         if (deps[i] != avoid && (!percentOnly || percentOnly && (Boolean)this.nodePath.get(deps[i].getIndex()))) {
            output.add(deps[i]);
         }
      }

      return output;
   }

   private Vector<DependencyNode> findAllDependentsVectorSortedByDistanceToPProjNode2(DependencyStructure dg, DependencyNode governor, DependencyNode avoid, boolean percentOnly) {
      Vector<DependencyNode> dependents = new Vector();
      int i = governor.getLeftDependentCount() - 1;
      int j = 0;
      DependencyNode leftChild = governor.getLeftDependent(i--);
      int var10 = j + 1;
      DependencyNode rightChild = governor.getRightDependent(j);

      while(leftChild != null && rightChild != null) {
         if (leftChild == avoid) {
            leftChild = governor.getLeftDependent(i--);
         } else if (rightChild == avoid) {
            rightChild = governor.getRightDependent(var10++);
         } else if (Math.abs(leftChild.getIndex() - avoid.getIndex()) < Math.abs(rightChild.getIndex() - avoid.getIndex())) {
            if (!percentOnly || percentOnly && (Boolean)this.nodePath.get(leftChild.getIndex())) {
               dependents.add(leftChild);
            }

            leftChild = governor.getLeftDependent(i--);
         } else {
            if (!percentOnly || percentOnly && (Boolean)this.nodePath.get(rightChild.getIndex())) {
               dependents.add(rightChild);
            }

            rightChild = governor.getRightDependent(var10++);
         }
      }

      for(; leftChild != null; leftChild = governor.getLeftDependent(i--)) {
         if (leftChild != avoid && (!percentOnly || percentOnly && (Boolean)this.nodePath.get(leftChild.getIndex()))) {
            dependents.add(leftChild);
         }
      }

      for(; rightChild != null; rightChild = governor.getRightDependent(var10++)) {
         if (rightChild != avoid && (!percentOnly || percentOnly && (Boolean)this.nodePath.get(rightChild.getIndex()))) {
            dependents.add(rightChild);
         }
      }

      return dependents;
   }

   private boolean deprojectivizeWithPath(DependencyStructure pdg, DependencyNode node) throws MaltChainedException {
      boolean success = true;
      boolean childSuccess = false;
      int childAttempts = 2;
      DependencyNode possibleSyntacticHead;
      if (node.hasHead() && node.getHeadEdge().isLabeled() && (Boolean)this.nodeLifted.get(node.getIndex()) && (Boolean)this.nodePath.get(node.getIndex())) {
         possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForPath(pdg, node.getHead(), node);
         if (possibleSyntacticHead != null) {
            pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
            this.nodeLifted.set(node.getIndex(), false);
         } else {
            success = false;
         }
      }

      if (node.hasHead() && node.getHeadEdge().isLabeled() && (Boolean)this.nodeLifted.get(node.getIndex())) {
         possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForPath(pdg, node.getHead(), node);
         if (possibleSyntacticHead != null) {
            pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
            this.nodeLifted.set(node.getIndex(), false);
         } else {
            success = false;
         }
      }

      while(!childSuccess && childAttempts > 0) {
         childSuccess = true;
         Vector<DependencyNode> children = new Vector();

         int i;
         DependencyNode child;
         for(i = 0; (child = node.getLeftDependent(i)) != null; ++i) {
            children.add(child);
         }

         for(i = 0; (child = node.getRightDependent(i)) != null; ++i) {
            children.add(child);
         }

         for(i = 0; i < children.size(); ++i) {
            child = (DependencyNode)children.get(i);
            if (!this.deprojectivizeWithPath(pdg, child)) {
               childSuccess = false;
            }
         }

         --childAttempts;
      }

      return childSuccess && success;
   }

   private DependencyNode breadthFirstSearchSortedByDistanceForPath(DependencyStructure dg, DependencyNode start, DependencyNode avoid) {
      Vector<DependencyNode> nodes = new Vector();
      nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, start, avoid, true));

      while(nodes.size() > 0) {
         DependencyNode dependent = (DependencyNode)nodes.remove(0);
         Vector newNodes;
         if ((newNodes = this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, dependent, avoid, true)).size() == 0) {
            return dependent;
         }

         nodes.addAll(newNodes);
      }

      return null;
   }

   private boolean deprojectivizeWithHeadAndPath(DependencyStructure pdg, DependencyNode node) throws MaltChainedException {
      boolean success = true;
      boolean childSuccess = false;
      int childAttempts = 2;
      DependencyNode possibleSyntacticHead;
      if (node.hasHead() && node.getHeadEdge().isLabeled() && (Boolean)this.nodeLifted.get(node.getIndex()) && (Boolean)this.nodePath.get(node.getIndex())) {
         possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForHeadAndPath(pdg, node.getHead(), node, (String)this.synacticHeadDeprel.get(node.getIndex()));
         if (possibleSyntacticHead != null) {
            pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
            this.nodeLifted.set(node.getIndex(), false);
         } else {
            success = false;
         }
      }

      if (node.hasHead() && node.getHeadEdge().isLabeled() && (Boolean)this.nodeLifted.get(node.getIndex())) {
         possibleSyntacticHead = this.breadthFirstSearchSortedByDistanceForHeadAndPath(pdg, node.getHead(), node, (String)this.synacticHeadDeprel.get(node.getIndex()));
         if (possibleSyntacticHead != null) {
            pdg.moveDependencyEdge(possibleSyntacticHead.getIndex(), node.getIndex());
            this.nodeLifted.set(node.getIndex(), false);
         } else {
            success = false;
         }
      }

      while(!childSuccess && childAttempts > 0) {
         childSuccess = true;
         Vector<DependencyNode> children = new Vector();

         int i;
         DependencyNode child;
         for(i = 0; (child = node.getLeftDependent(i)) != null; ++i) {
            children.add(child);
         }

         for(i = 0; (child = node.getRightDependent(i)) != null; ++i) {
            children.add(child);
         }

         for(i = 0; i < children.size(); ++i) {
            child = (DependencyNode)children.get(i);
            if (!this.deprojectivizeWithHeadAndPath(pdg, child)) {
               childSuccess = false;
            }
         }

         --childAttempts;
      }

      return childSuccess && success;
   }

   private DependencyNode breadthFirstSearchSortedByDistanceForHeadAndPath(DependencyStructure dg, DependencyNode start, DependencyNode avoid, String syntacticHeadDeprelCode) throws MaltChainedException {
      Vector<DependencyNode> nodes = new Vector();
      Vector<DependencyNode> newNodes = null;
      Vector<DependencyNode> secondChance = new Vector();
      nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, start, avoid, true));

      while(nodes.size() > 0) {
         DependencyNode dependent = (DependencyNode)nodes.remove(0);
         if ((newNodes = this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, dependent, avoid, true)).size() == 0 && this.deprelSymbolTable.getSymbolCodeToString(dependent.getHeadEdge().getLabelCode(this.deprelSymbolTable)).equals(syntacticHeadDeprelCode)) {
            return dependent;
         }

         nodes.addAll(newNodes);
         if (this.deprelSymbolTable.getSymbolCodeToString(dependent.getHeadEdge().getLabelCode(this.deprelSymbolTable)).equals(syntacticHeadDeprelCode) && newNodes.size() != 0) {
            secondChance.add(dependent);
         }
      }

      if (secondChance.size() > 0) {
         return (DependencyNode)secondChance.firstElement();
      } else {
         return null;
      }
   }

   private static enum LiftingOrder {
      SHORTEST,
      DEEPEST;

      private LiftingOrder() {
      }
   }

   private static enum CoveredRootAttachment {
      NONE,
      IGNORE,
      LEFT,
      RIGHT,
      HEAD;

      private CoveredRootAttachment() {
      }
   }

   private static enum PseudoProjectiveEncoding {
      NONE,
      BASELINE,
      HEAD,
      PATH,
      HEADPATH,
      TRACE;

      private PseudoProjectiveEncoding() {
      }
   }
}
