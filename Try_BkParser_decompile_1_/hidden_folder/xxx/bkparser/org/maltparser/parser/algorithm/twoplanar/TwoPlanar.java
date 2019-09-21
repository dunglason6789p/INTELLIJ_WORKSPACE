package org.maltparser.parser.algorithm.twoplanar;

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

public class TwoPlanar extends TransitionSystem {
   protected static final int SHIFT = 1;
   protected static final int SWITCH = 2;
   protected static final int RIGHTARC = 3;
   protected static final int LEFTARC = 4;
   protected static final int REDUCE = 5;
   protected static final int REDUCEBOTH = 6;

   public TwoPlanar(PropagationManager propagationManager) throws MaltChainedException {
      super(propagationManager);
   }

   public void apply(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
      TwoPlanarConfig planarConfig = (TwoPlanarConfig)config;
      Stack<DependencyNode> activeStack = planarConfig.getActiveStack();
      Stack<DependencyNode> inactiveStack = planarConfig.getInactiveStack();
      Stack<DependencyNode> input = planarConfig.getInput();
      currentAction.getAction(this.actionContainers);
      Edge e = null;
      int actionCode = this.transActionContainer.getActionCode();
      switch(actionCode) {
      case 2:
         planarConfig.switchStacks();
         if (planarConfig.reduceAfterSwitch()) {
            planarConfig.getActiveStack().pop();
         }
         break;
      case 3:
         e = planarConfig.getDependencyStructure().addDependencyEdge(((DependencyNode)activeStack.peek()).getIndex(), ((DependencyNode)input.peek()).getIndex());
         this.addEdgeLabels(e);
         break;
      case 4:
         e = planarConfig.getDependencyStructure().addDependencyEdge(((DependencyNode)input.peek()).getIndex(), ((DependencyNode)activeStack.peek()).getIndex());
         this.addEdgeLabels(e);
         break;
      case 5:
         activeStack.pop();
         break;
      case 6:
         activeStack.pop();
         inactiveStack.pop();
         break;
      default:
         DependencyNode n = (DependencyNode)input.pop();
         activeStack.push(n);
         inactiveStack.push(n);
      }

      planarConfig.setLastAction(actionCode);
   }

   public GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException {
      TwoPlanarConfig theConfig = (TwoPlanarConfig)config;
      return theConfig.getRootHandling() != 1 && ((DependencyNode)theConfig.getActiveStack().peek()).isRoot() ? this.updateActionContainers(history, 1, (LabelSet)null) : null;
   }

   protected void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException {
      ttable.addTransition(1, "SH", false, (TransitionTable)null);
      ttable.addTransition(2, "SW", false, (TransitionTable)null);
      ttable.addTransition(5, "RE", false, (TransitionTable)null);
      ttable.addTransition(6, "RB", false, (TransitionTable)null);
      ttable.addTransition(3, "RA", true, (TransitionTable)null);
      ttable.addTransition(4, "LA", true, (TransitionTable)null);
   }

   protected void initWithDefaultTransitions(GuideUserHistory history) throws MaltChainedException {
      GuideUserAction currentAction = new ComplexDecisionAction(history);
      this.transActionContainer.setAction(1);
      this.transActionContainer.setAction(5);
      this.transActionContainer.setAction(2);
      this.transActionContainer.setAction(6);

      for(int i = 0; i < this.arcLabelActionContainers.length; ++i) {
         this.arcLabelActionContainers[i].setAction(-1);
      }

      currentAction.addAction(this.actionContainers);
   }

   public String getName() {
      return "two-planar arc-eager";
   }

   public boolean permissible(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
      currentAction.getAction(this.actionContainers);
      int trans = this.transActionContainer.getActionCode();
      TwoPlanarConfig planarConfig = (TwoPlanarConfig)config;
      DependencyNode activeStackPeek = (DependencyNode)planarConfig.getActiveStack().peek();
      DependencyNode inactiveStackPeek = (DependencyNode)planarConfig.getInactiveStack().peek();
      DependencyNode inputPeek = (DependencyNode)planarConfig.getInput().peek();
      DependencyStructure dg = planarConfig.getDependencyGraph();
      boolean singleHeadConstraint = planarConfig.requiresSingleHead();
      boolean noCoveredRootsConstraint = planarConfig.requiresNoCoveredRoots();
      boolean acyclicityConstraint = planarConfig.requiresAcyclicity();
      if ((trans == 4 || trans == 3) && !this.isActionContainersLabeled()) {
         return false;
      } else {
         if (trans == 4) {
            if (activeStackPeek.isRoot()) {
               return false;
            }

            if (activeStackPeek.hasHead() && singleHeadConstraint) {
               return false;
            }

            if (activeStackPeek.hasHead() && dg.getTokenNode(activeStackPeek.getIndex()).getHead().getIndex() == inputPeek.getIndex()) {
               return false;
            }

            if (acyclicityConstraint && activeStackPeek.findComponent().getIndex() == inputPeek.findComponent().getIndex()) {
               return false;
            }
         }

         if (trans == 3) {
            if (inputPeek.hasHead() && singleHeadConstraint) {
               return false;
            }

            if (inputPeek.hasHead() && dg.getTokenNode(inputPeek.getIndex()).getHead().getIndex() == activeStackPeek.getIndex()) {
               return false;
            }

            if (acyclicityConstraint && activeStackPeek.findComponent().getIndex() == inputPeek.findComponent().getIndex()) {
               return false;
            }
         }

         if (trans == 5) {
            if (activeStackPeek.isRoot()) {
               return false;
            }

            if (!activeStackPeek.hasHead() && noCoveredRootsConstraint) {
               return false;
            }
         }

         if (trans == 1) {
         }

         if (trans != 6) {
            if (trans == 2) {
               if (planarConfig.reduceAfterSwitch()) {
                  if (inactiveStackPeek.isRoot()) {
                     return false;
                  }

                  if (!inactiveStackPeek.hasHead() && noCoveredRootsConstraint) {
                     return false;
                  }
               } else if (planarConfig.getLastAction() == 2) {
                  return false;
               }
            }

            return true;
         } else if (!activeStackPeek.isRoot() && !inactiveStackPeek.isRoot()) {
            return (!activeStackPeek.hasHead() || inactiveStackPeek.hasHead()) && noCoveredRootsConstraint ? false : false;
         } else {
            return false;
         }
      }
   }

   public GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException {
      return this.updateActionContainers(history, 1, (LabelSet)null);
   }
}
