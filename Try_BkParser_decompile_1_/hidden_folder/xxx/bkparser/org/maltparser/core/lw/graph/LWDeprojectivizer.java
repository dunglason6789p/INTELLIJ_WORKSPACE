package org.maltparser.core.lw.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
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

   public LWDeprojectivizer() {
   }

   public static int getMarkingStrategyInt(String markingStrategyString) {
      if (markingStrategyString.equalsIgnoreCase("none")) {
         return 0;
      } else if (markingStrategyString.equalsIgnoreCase("baseline")) {
         return 1;
      } else if (markingStrategyString.equalsIgnoreCase("head")) {
         return 1;
      } else if (markingStrategyString.equalsIgnoreCase("path")) {
         return 1;
      } else if (markingStrategyString.equalsIgnoreCase("head+path")) {
         return 1;
      } else {
         return markingStrategyString.equalsIgnoreCase("trace") ? 1 : 0;
      }
   }

   public void deprojectivize(DependencyStructure pdg, int markingStrategy) throws MaltChainedException {
      SymbolTable deprelSymbolTable = pdg.getSymbolTables().getSymbolTable("DEPREL");
      SymbolTable ppliftedSymbolTable = pdg.getSymbolTables().getSymbolTable("PPLIFTED");
      SymbolTable pppathSymbolTable = pdg.getSymbolTables().getSymbolTable("PPPATH");
      boolean[] nodeLifted = new boolean[pdg.nDependencyNode()];
      Arrays.fill(nodeLifted, false);
      boolean[] nodePath = new boolean[pdg.nDependencyNode()];
      Arrays.fill(nodePath, false);
      String[] synacticHeadDeprel = new String[pdg.nDependencyNode()];
      Arrays.fill(synacticHeadDeprel, (Object)null);
      Iterator i$ = pdg.getTokenIndices().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         Edge e = pdg.getDependencyNode(index).getHeadEdge();
         if (e.hasLabel(deprelSymbolTable)) {
            if (e.hasLabel(pppathSymbolTable) && pppathSymbolTable.getSymbolCodeToString(e.getLabelCode(pppathSymbolTable)).equals("#true#")) {
               nodePath[pdg.getDependencyNode(index).getIndex()] = true;
            }

            if (e.hasLabel(ppliftedSymbolTable) && !ppliftedSymbolTable.getSymbolCodeToString(e.getLabelCode(ppliftedSymbolTable)).equals("#false#")) {
               nodeLifted[index] = true;
               if (!ppliftedSymbolTable.getSymbolCodeToString(e.getLabelCode(ppliftedSymbolTable)).equals("#true#")) {
                  synacticHeadDeprel[index] = ppliftedSymbolTable.getSymbolCodeToString(e.getLabelCode(ppliftedSymbolTable));
               }
            }
         }
      }

      this.deattachCoveredRootsForDeprojectivization(pdg, deprelSymbolTable);
      if (markingStrategy == 1 && this.needsDeprojectivizeWithHead(pdg, nodeLifted, nodePath, synacticHeadDeprel, deprelSymbolTable)) {
         this.deprojectivizeWithHead(pdg, pdg.getDependencyRoot(), nodeLifted, nodePath, synacticHeadDeprel, deprelSymbolTable);
      } else if (markingStrategy == 1) {
         this.deprojectivizeWithPath(pdg, pdg.getDependencyRoot(), nodeLifted, nodePath);
      } else if (markingStrategy == 1) {
         this.deprojectivizeWithHeadAndPath(pdg, pdg.getDependencyRoot(), nodeLifted, nodePath, synacticHeadDeprel, deprelSymbolTable);
      }

   }

   private void deattachCoveredRootsForDeprojectivization(DependencyStructure pdg, SymbolTable deprelSymbolTable) throws MaltChainedException {
      SymbolTable ppcoveredRootSymbolTable = pdg.getSymbolTables().getSymbolTable("PPCOVERED");
      Iterator i$ = pdg.getTokenIndices().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         Edge e = pdg.getDependencyNode(index).getHeadEdge();
         if (e.hasLabel(deprelSymbolTable) && e.hasLabel(ppcoveredRootSymbolTable) && ppcoveredRootSymbolTable.getSymbolCodeToString(e.getLabelCode(ppcoveredRootSymbolTable)).equals("#true#")) {
            pdg.moveDependencyEdge(pdg.getDependencyRoot().getIndex(), pdg.getDependencyNode(index).getIndex());
         }
      }

   }

   private boolean needsDeprojectivizeWithHead(DependencyStructure pdg, boolean[] nodeLifted, boolean[] nodePath, String[] synacticHeadDeprel, SymbolTable deprelSymbolTable) throws MaltChainedException {
      Iterator i$ = pdg.getDependencyIndices().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         if (nodeLifted[index]) {
            DependencyNode node = pdg.getDependencyNode(index);
            if (this.breadthFirstSearchSortedByDistanceForHead(pdg, node.getHead(), node, synacticHeadDeprel[index], nodePath, deprelSymbolTable) != null) {
               return true;
            }
         }
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

      while(!childSuccess && childAttempts > 0) {
         childSuccess = true;
         List<DependencyNode> children = node.getListOfDependents();

         for(int i = 0; i < children.size(); ++i) {
            if (!this.deprojectivizeWithHead(pdg, (DependencyNode)children.get(i), nodeLifted, nodePath, synacticHeadDeprel, deprelSymbolTable)) {
               childSuccess = false;
            }
         }

         --childAttempts;
      }

      return childSuccess && success;
   }

   private DependencyNode breadthFirstSearchSortedByDistanceForHead(DependencyStructure dg, DependencyNode start, DependencyNode avoid, String syntacticHeadDeprel, boolean[] nodePath, SymbolTable deprelSymbolTable) throws MaltChainedException {
      List<DependencyNode> nodes = new ArrayList();
      nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, start, avoid, false, nodePath));

      DependencyNode dependent;
      for(; nodes.size() > 0; nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, dependent, avoid, false, nodePath))) {
         dependent = (DependencyNode)nodes.remove(0);
         if (dependent.getHeadEdge().hasLabel(deprelSymbolTable)) {
            String dependentDeprel = deprelSymbolTable.getSymbolCodeToString(dependent.getHeadEdge().getLabelCode(deprelSymbolTable));
            if (dependentDeprel.equals(syntacticHeadDeprel)) {
               return dependent;
            }
         }
      }

      return null;
   }

   private List<DependencyNode> findAllDependentsVectorSortedByDistanceToPProjNode(DependencyStructure dg, DependencyNode governor, DependencyNode avoid, boolean percentOnly, boolean[] nodePath) {
      List<DependencyNode> output = new ArrayList();
      List<DependencyNode> dependents = governor.getListOfDependents();
      DependencyNode[] deps = new DependencyNode[dependents.size()];
      int[] distances = new int[dependents.size()];

      int smallest;
      for(smallest = 0; smallest < dependents.size(); ++smallest) {
         distances[smallest] = Math.abs(((DependencyNode)dependents.get(smallest)).getIndex() - avoid.getIndex());
         deps[smallest] = (DependencyNode)dependents.get(smallest);
      }

      if (distances.length > 1) {
         int n = distances.length;

         for(int i = 0; i < n; ++i) {
            smallest = i;

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

      for(smallest = 0; smallest < distances.length; ++smallest) {
         if (deps[smallest] != avoid && (!percentOnly || percentOnly && nodePath[deps[smallest].getIndex()])) {
            output.add(deps[smallest]);
         }
      }

      return output;
   }

   private boolean deprojectivizeWithPath(DependencyStructure pdg, DependencyNode node, boolean[] nodeLifted, boolean[] nodePath) throws MaltChainedException {
      boolean success = true;
      boolean childSuccess = false;
      int childAttempts = 2;
      DependencyNode possibleSyntacticHead;
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

      while(!childSuccess && childAttempts > 0) {
         childSuccess = true;
         List<DependencyNode> children = node.getListOfDependents();

         for(int i = 0; i < children.size(); ++i) {
            if (!this.deprojectivizeWithPath(pdg, (DependencyNode)children.get(i), nodeLifted, nodePath)) {
               childSuccess = false;
            }
         }

         --childAttempts;
      }

      return childSuccess && success;
   }

   private DependencyNode breadthFirstSearchSortedByDistanceForPath(DependencyStructure dg, DependencyNode start, DependencyNode avoid, boolean[] nodePath) {
      List<DependencyNode> nodes = new ArrayList();
      nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, start, avoid, true, nodePath));

      while(nodes.size() > 0) {
         DependencyNode dependent = (DependencyNode)nodes.remove(0);
         List newNodes;
         if ((newNodes = this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, dependent, avoid, true, nodePath)).size() == 0) {
            return dependent;
         }

         nodes.addAll(newNodes);
      }

      return null;
   }

   private boolean deprojectivizeWithHeadAndPath(DependencyStructure pdg, DependencyNode node, boolean[] nodeLifted, boolean[] nodePath, String[] synacticHeadDeprel, SymbolTable deprelSymbolTable) throws MaltChainedException {
      boolean success = true;
      boolean childSuccess = false;
      int childAttempts = 2;
      DependencyNode possibleSyntacticHead;
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

      while(!childSuccess && childAttempts > 0) {
         childSuccess = true;
         List<DependencyNode> children = node.getListOfDependents();

         for(int i = 0; i < children.size(); ++i) {
            if (!this.deprojectivizeWithHeadAndPath(pdg, (DependencyNode)children.get(i), nodeLifted, nodePath, synacticHeadDeprel, deprelSymbolTable)) {
               childSuccess = false;
            }
         }

         --childAttempts;
      }

      return childSuccess && success;
   }

   private DependencyNode breadthFirstSearchSortedByDistanceForHeadAndPath(DependencyStructure dg, DependencyNode start, DependencyNode avoid, String syntacticHeadDeprelCode, boolean[] nodePath, SymbolTable deprelSymbolTable) throws MaltChainedException {
      List<DependencyNode> nodes = new ArrayList();
      List<DependencyNode> newNodes = null;
      List<DependencyNode> secondChance = new ArrayList();
      nodes.addAll(this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, start, avoid, true, nodePath));

      while(nodes.size() > 0) {
         DependencyNode dependent = (DependencyNode)nodes.remove(0);
         if ((newNodes = this.findAllDependentsVectorSortedByDistanceToPProjNode(dg, dependent, avoid, true, nodePath)).size() == 0 && deprelSymbolTable.getSymbolCodeToString(dependent.getHeadEdge().getLabelCode(deprelSymbolTable)).equals(syntacticHeadDeprelCode)) {
            return dependent;
         }

         nodes.addAll(newNodes);
         if (deprelSymbolTable.getSymbolCodeToString(dependent.getHeadEdge().getLabelCode(deprelSymbolTable)).equals(syntacticHeadDeprelCode) && newNodes.size() != 0) {
            secondChance.add(dependent);
         }
      }

      if (secondChance.size() > 0) {
         return (DependencyNode)secondChance.get(0);
      } else {
         return null;
      }
   }
}
