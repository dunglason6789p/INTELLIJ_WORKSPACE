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

public class SwapLazyOracle extends Oracle {
   private ArrayList<Integer> swapArray;
   private boolean swapArrayActive = false;

   public SwapLazyOracle(DependencyParserConfig manager, GuideUserHistory history) throws MaltChainedException {
      super(manager, history);
      this.setGuideName("swaplazy");
      this.swapArray = new ArrayList();
   }

   public GuideUserAction predict(DependencyStructure gold, ParserConfiguration configuration) throws MaltChainedException {
      StackConfig config = (StackConfig)configuration;
      Stack<DependencyNode> stack = config.getStack();
      if (!this.swapArrayActive) {
         this.createSwapArray(gold);
         this.swapArrayActive = true;
      }

      if (stack.size() < 2) {
         return this.updateActionContainers(1, (LabelSet)null);
      } else {
         DependencyNode left = (DependencyNode)stack.get(stack.size() - 2);
         DependencyNode right = (DependencyNode)stack.get(stack.size() - 1);
         int leftIndex = left.getIndex();
         int rightIndex = right.getIndex();
         if ((Integer)this.swapArray.get(leftIndex) > (Integer)this.swapArray.get(rightIndex) && this.necessarySwap(gold, config.getDependencyGraph(), right, config.getInput())) {
            return this.updateActionContainers(2, (LabelSet)null);
         } else if (!left.isRoot() && gold.getTokenNode(leftIndex).getHead().getIndex() == rightIndex && this.nodeComplete(gold, config.getDependencyGraph(), leftIndex)) {
            return this.updateActionContainers(4, gold.getTokenNode(leftIndex).getHeadEdge().getLabelSet());
         } else {
            return gold.getTokenNode(rightIndex).getHead().getIndex() == leftIndex && this.nodeComplete(gold, config.getDependencyGraph(), rightIndex) ? this.updateActionContainers(3, gold.getTokenNode(rightIndex).getHeadEdge().getLabelSet()) : this.updateActionContainers(1, (LabelSet)null);
         }
      }
   }

   private boolean nodeComplete(DependencyStructure gold, DependencyStructure parseDependencyGraph, int nodeIndex) {
      DependencyNode goldNode = gold.getTokenNode(nodeIndex);
      DependencyNode parseNode = parseDependencyGraph.getTokenNode(nodeIndex);
      if (goldNode.hasLeftDependent()) {
         if (!parseNode.hasLeftDependent()) {
            return false;
         }

         if (goldNode.getLeftmostDependent().getIndex() != parseNode.getLeftmostDependent().getIndex()) {
            return false;
         }
      }

      if (goldNode.hasRightDependent()) {
         if (!parseNode.hasRightDependent()) {
            return false;
         }

         if (goldNode.getRightmostDependent().getIndex() != parseNode.getRightmostDependent().getIndex()) {
            return false;
         }
      }

      return true;
   }

   private boolean necessarySwap(DependencyStructure gold, DependencyStructure parse, DependencyNode node, Stack<DependencyNode> input) throws MaltChainedException {
      DependencyNode left = node;
      int index = input.size() - 1;
      if (index < 0) {
         return true;
      } else {
         DependencyNode right = (DependencyNode)input.peek();
         int rc = -1;

         while(true) {
            if (this.projectiveInterval(parse, left, right)) {
               if (rc == right.getIndex()) {
                  return false;
               }

               if (gold.getDependencyNode(node.getIndex()).getHead().getIndex() == right.getIndex()) {
                  return !this.leftComplete(gold, node);
               }

               if (gold.getDependencyNode(right.getIndex()).getHead().getIndex() == node.getIndex()) {
                  if (!gold.getDependencyNode(right.getIndex()).hasRightDependent()) {
                     return false;
                  }

                  rc = gold.getDependencyNode(right.getIndex()).getRightmostProperDescendantIndex();
               }

               if (index > 0) {
                  left = right;
                  --index;
                  right = (DependencyNode)input.get(index);
                  continue;
               }
            }

            return true;
         }
      }
   }

   private boolean projectiveInterval(DependencyStructure parse, DependencyNode left, DependencyNode right) throws MaltChainedException {
      int l = (Integer)this.swapArray.get(left.getIndex());
      int r = (Integer)this.swapArray.get(right.getIndex());
      DependencyNode node = null;
      if (l > r) {
         return false;
      } else {
         for(int i = l + 1; i < r; ++i) {
            for(int j = 0; j < this.swapArray.size(); ++j) {
               if ((Integer)this.swapArray.get(j) == i) {
                  node = parse.getDependencyNode(j);
                  break;
               }
            }

            while(node.hasHead()) {
               node = node.getHead();
            }

            if (node != left && node != right) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean leftComplete(DependencyStructure gold, DependencyNode right) throws MaltChainedException {
      DependencyNode goldNode = gold.getDependencyNode(right.getIndex());
      if (!goldNode.hasLeftDependent()) {
         return true;
      } else if (!right.hasLeftDependent()) {
         return false;
      } else {
         return goldNode.getLeftmostDependent().getIndex() == right.getLeftmostDependent().getIndex();
      }
   }

   public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
      this.swapArrayActive = false;
   }

   public void terminate() throws MaltChainedException {
   }

   private void createSwapArray(DependencyStructure goldDependencyGraph) throws MaltChainedException {
      this.swapArray.clear();
      int n = goldDependencyGraph.getHighestDependencyNodeIndex();

      for(int i = 0; i <= n; ++i) {
         this.swapArray.add(new Integer(i));
      }

      this.createSwapArray(goldDependencyGraph.getDependencyRoot(), 0);
   }

   private int createSwapArray(DependencyNode node, int order) {
      int o = order;
      if (node != null) {
         int i;
         for(i = 0; i < node.getLeftDependentCount(); ++i) {
            o = this.createSwapArray(node.getLeftDependent(i), o);
         }

         this.swapArray.set(node.getIndex(), o++);

         for(i = node.getRightDependentCount(); i >= 0; --i) {
            o = this.createSwapArray(node.getRightDependent(i), o);
         }
      }

      return o;
   }
}
