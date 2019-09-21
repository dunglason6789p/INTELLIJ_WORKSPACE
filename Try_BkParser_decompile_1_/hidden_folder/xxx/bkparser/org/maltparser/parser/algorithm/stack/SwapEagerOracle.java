package org.maltparser.parser.algorithm.stack;

import java.util.ArrayList;
import java.util.Stack;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.Oracle;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideUserAction;

public class SwapEagerOracle extends Oracle {
   private ArrayList<Integer> swapArray;
   private boolean swapArrayActive = false;

   public SwapEagerOracle(DependencyParserConfig manager, GuideUserHistory history) throws MaltChainedException {
      super(manager, history);
      this.setGuideName("swapeager");
      this.swapArray = new ArrayList();
   }

   public GuideUserAction predict(DependencyStructure gold, ParserConfiguration configuration) throws MaltChainedException {
      StackConfig config = (StackConfig)configuration;
      Stack<DependencyNode> stack = config.getStack();
      if (!this.swapArrayActive) {
         this.createSwapArray(gold);
         this.swapArrayActive = true;
      }

      GuideUserAction action = null;
      if (stack.size() < 2) {
         action = this.updateActionContainers(1, (LabelSet)null);
      } else {
         DependencyNode left = (DependencyNode)stack.get(stack.size() - 2);
         int leftIndex = left.getIndex();
         int rightIndex = ((DependencyNode)stack.get(stack.size() - 1)).getIndex();
         if ((Integer)this.swapArray.get(leftIndex) > (Integer)this.swapArray.get(rightIndex)) {
            action = this.updateActionContainers(2, (LabelSet)null);
         } else if (!left.isRoot() && gold.getTokenNode(leftIndex).getHead().getIndex() == rightIndex && this.nodeComplete(gold, config.getDependencyGraph(), leftIndex)) {
            action = this.updateActionContainers(4, gold.getTokenNode(leftIndex).getHeadEdge().getLabelSet());
         } else if (gold.getTokenNode(rightIndex).getHead().getIndex() == leftIndex && this.nodeComplete(gold, config.getDependencyGraph(), rightIndex)) {
            action = this.updateActionContainers(3, gold.getTokenNode(rightIndex).getHeadEdge().getLabelSet());
         } else {
            action = this.updateActionContainers(1, (LabelSet)null);
         }
      }

      return action;
   }

   private boolean nodeComplete(DependencyStructure gold, DependencyStructure parseDependencyGraph, int nodeIndex) {
      if (gold.getTokenNode(nodeIndex).hasLeftDependent()) {
         if (!parseDependencyGraph.getTokenNode(nodeIndex).hasLeftDependent()) {
            return false;
         }

         if (gold.getTokenNode(nodeIndex).getLeftmostDependent().getIndex() != parseDependencyGraph.getTokenNode(nodeIndex).getLeftmostDependent().getIndex()) {
            return false;
         }
      }

      if (gold.getTokenNode(nodeIndex).hasRightDependent()) {
         if (!parseDependencyGraph.getTokenNode(nodeIndex).hasRightDependent()) {
            return false;
         }

         if (gold.getTokenNode(nodeIndex).getRightmostDependent().getIndex() != parseDependencyGraph.getTokenNode(nodeIndex).getRightmostDependent().getIndex()) {
            return false;
         }
      }

      return true;
   }

   public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
      this.swapArrayActive = false;
   }

   public void terminate() throws MaltChainedException {
   }

   private void createSwapArray(DependencyStructure goldDependencyGraph) throws MaltChainedException {
      this.swapArray.clear();

      for(int i = 0; i <= goldDependencyGraph.getHighestDependencyNodeIndex(); ++i) {
         this.swapArray.add(new Integer(i));
      }

      this.createSwapArray(goldDependencyGraph.getDependencyRoot(), 0);
   }

   private int createSwapArray(DependencyNode n, int order) {
      int o = order;
      if (n != null) {
         int i;
         for(i = 0; i < n.getLeftDependentCount(); ++i) {
            o = this.createSwapArray(n.getLeftDependent(i), o);
         }

         this.swapArray.set(n.getIndex(), o++);

         for(i = n.getRightDependentCount(); i >= 0; --i) {
            o = this.createSwapArray(n.getRightDependent(i), o);
         }
      }

      return o;
   }
}
