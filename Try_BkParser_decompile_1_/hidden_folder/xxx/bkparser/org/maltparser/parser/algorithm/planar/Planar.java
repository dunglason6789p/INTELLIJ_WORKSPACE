package org.maltparser.parser.algorithm.planar;

import java.util.Stack;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.ComplexDecisionAction;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.transition.TransitionTable;

public class Planar extends TransitionSystem {
   protected static final int SHIFT = 1;
   protected static final int REDUCE = 2;
   protected static final int RIGHTARC = 3;
   protected static final int LEFTARC = 4;

   public Planar(PropagationManager propagationManager) throws MaltChainedException {
      super(propagationManager);
   }

   public void apply(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
      PlanarConfig planarConfig = (PlanarConfig)config;
      Stack<DependencyNode> stack = planarConfig.getStack();
      Stack<DependencyNode> input = planarConfig.getInput();
      currentAction.getAction(this.actionContainers);
      Edge e = null;
      switch(this.transActionContainer.getActionCode()) {
      case 2:
         stack.pop();
         break;
      case 3:
         e = planarConfig.getDependencyStructure().addDependencyEdge(((DependencyNode)stack.peek()).getIndex(), ((DependencyNode)input.peek()).getIndex());
         this.addEdgeLabels(e);
         break;
      case 4:
         e = planarConfig.getDependencyStructure().addDependencyEdge(((DependencyNode)input.peek()).getIndex(), ((DependencyNode)stack.peek()).getIndex());
         this.addEdgeLabels(e);
         break;
      default:
         stack.push(input.pop());
      }

   }

   public GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException {
      PlanarConfig planarConfig = (PlanarConfig)config;
      return planarConfig.getRootHandling() != 1 && ((DependencyNode)planarConfig.getStack().peek()).isRoot() ? this.updateActionContainers(history, 1, (LabelSet)null) : null;
   }

   protected void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException {
      ttable.addTransition(1, "SH", false, (TransitionTable)null);
      ttable.addTransition(2, "RE", false, (TransitionTable)null);
      ttable.addTransition(3, "RA", true, (TransitionTable)null);
      ttable.addTransition(4, "LA", true, (TransitionTable)null);
   }

   protected void initWithDefaultTransitions(GuideUserHistory history) throws MaltChainedException {
      GuideUserAction currentAction = new ComplexDecisionAction(history);
      this.transActionContainer.setAction(1);
      this.transActionContainer.setAction(2);

      for(int i = 0; i < this.arcLabelActionContainers.length; ++i) {
         this.arcLabelActionContainers[i].setAction(-1);
      }

      currentAction.addAction(this.actionContainers);
   }

   public String getName() {
      return "planar arc-eager";
   }

   public boolean permissible(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
      currentAction.getAction(this.actionContainers);
      int trans = this.transActionContainer.getActionCode();
      PlanarConfig planarConfig = (PlanarConfig)config;
      DependencyNode stackPeek = (DependencyNode)planarConfig.getStack().peek();
      DependencyNode inputPeek = (DependencyNode)planarConfig.getInput().peek();
      DependencyStructure dg = planarConfig.getDependencyGraph();
      boolean singleHeadConstraint = planarConfig.requiresSingleHead();
      boolean noCoveredRootsConstraint = planarConfig.requiresNoCoveredRoots();
      boolean acyclicityConstraint = planarConfig.requiresAcyclicity();
      boolean connectednessConstraintOnReduce = planarConfig.requiresConnectednessCheckOnReduce();
      boolean connectednessConstraintOnShift = planarConfig.requiresConnectednessCheckOnShift();
      if ((trans == 4 || trans == 3) && !this.isActionContainersLabeled()) {
         return false;
      } else {
         if (trans == 4) {
            if (stackPeek.isRoot()) {
               return false;
            }

            if (stackPeek.hasHead() && singleHeadConstraint) {
               return false;
            }

            if (stackPeek.hasHead() && dg.getTokenNode(stackPeek.getIndex()).getHead().getIndex() == inputPeek.getIndex()) {
               return false;
            }

            if (acyclicityConstraint && stackPeek.findComponent().getIndex() == inputPeek.findComponent().getIndex()) {
               return false;
            }
         }

         if (trans == 3) {
            if (inputPeek.hasHead() && singleHeadConstraint) {
               return false;
            }

            if (inputPeek.hasHead() && dg.getTokenNode(inputPeek.getIndex()).getHead().getIndex() == stackPeek.getIndex()) {
               return false;
            }

            if (acyclicityConstraint && stackPeek.findComponent().getIndex() == inputPeek.findComponent().getIndex()) {
               return false;
            }
         }

         boolean path1;
         if (trans == 2) {
            if (stackPeek.isRoot()) {
               return false;
            }

            if (!stackPeek.hasHead() && noCoveredRootsConstraint) {
               return false;
            }

            if (connectednessConstraintOnReduce) {
               path1 = stackPeek.findComponent().getIndex() == inputPeek.findComponent().getIndex();
               boolean path2;
               if (planarConfig.getStack().size() < 2) {
                  path2 = false;
               } else {
                  DependencyNode stackPrev = (DependencyNode)planarConfig.getStack().get(planarConfig.getStack().size() - 2);
                  path2 = stackPrev.findComponent().getIndex() == stackPeek.findComponent().getIndex();
               }

               return path1 || path2;
            }
         }

         if (trans == 1 && connectednessConstraintOnShift && planarConfig.getInput().size() == 1) {
            path1 = planarConfig.getDependencyGraph().getTokenNode(1).findComponent().getIndex() == inputPeek.findComponent().getIndex();
            return path1;
         } else {
            return true;
         }
      }
   }

   public GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException {
      return this.updateActionContainers(history, 1, (LabelSet)null);
   }
}
