package org.maltparser.parser.algorithm.stack;

import java.util.Stack;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.ComplexDecisionAction;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.transition.TransitionTable;

public class NonProjective extends TransitionSystem {
   protected static final int SHIFT = 1;
   protected static final int SWAP = 2;
   protected static final int RIGHTARC = 3;
   protected static final int LEFTARC = 4;

   public NonProjective(PropagationManager propagationManager) throws MaltChainedException {
      super(propagationManager);
   }

   public void apply(GuideUserAction currentAction, ParserConfiguration configuration) throws MaltChainedException {
      StackConfig config = (StackConfig)configuration;
      Stack<DependencyNode> stack = config.getStack();
      Stack<DependencyNode> input = config.getInput();
      currentAction.getAction(this.actionContainers);
      Edge e = null;
      DependencyNode head = null;
      DependencyNode dep = null;
      switch(this.transActionContainer.getActionCode()) {
      case 2:
         dep = (DependencyNode)stack.pop();
         input.push(stack.pop());
         stack.push(dep);
         config.lookaheadIncrement();
         break;
      case 3:
         dep = (DependencyNode)stack.pop();
         e = config.getDependencyStructure().addDependencyEdge(((DependencyNode)stack.peek()).getIndex(), dep.getIndex());
         this.addEdgeLabels(e);
         break;
      case 4:
         head = (DependencyNode)stack.pop();
         dep = (DependencyNode)stack.pop();
         e = config.getDependencyStructure().addDependencyEdge(head.getIndex(), dep.getIndex());
         this.addEdgeLabels(e);
         stack.push(head);
         break;
      default:
         if (input.isEmpty()) {
            stack.pop();
         } else {
            stack.push(input.pop());
         }

         config.lookaheadDecrement();
      }

   }

   public boolean permissible(GuideUserAction currentAction, ParserConfiguration configuration) throws MaltChainedException {
      currentAction.getAction(this.actionContainers);
      int trans = this.transActionContainer.getActionCode();
      if ((trans == 4 || trans == 3) && !this.isActionContainersLabeled()) {
         return false;
      } else {
         StackConfig config = (StackConfig)configuration;
         Stack<DependencyNode> stack = config.getStack();
         if ((trans == 4 || trans == 3 || trans == 2) && stack.size() < 2) {
            return false;
         } else if ((trans == 4 || trans == 2) && ((DependencyNode)stack.get(stack.size() - 2)).isRoot()) {
            return false;
         } else {
            Stack<DependencyNode> input = config.getInput();
            if (trans == 1 && input.isEmpty()) {
               return false;
            } else {
               return trans != 2 || ((DependencyNode)stack.get(stack.size() - 2)).getIndex() <= ((DependencyNode)stack.get(stack.size() - 1)).getIndex();
            }
         }
      }
   }

   public GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException {
      return null;
   }

   protected void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException {
      ttable.addTransition(1, "SH", false, (TransitionTable)null);
      ttable.addTransition(2, "SW", false, (TransitionTable)null);
      ttable.addTransition(3, "RA", true, (TransitionTable)null);
      ttable.addTransition(4, "LA", true, (TransitionTable)null);
   }

   protected void initWithDefaultTransitions(GuideUserHistory history) throws MaltChainedException {
      GuideUserAction currentAction = new ComplexDecisionAction(history);
      this.transActionContainer.setAction(1);

      for(int i = 0; i < this.arcLabelActionContainers.length; ++i) {
         this.arcLabelActionContainers[i].setAction(-1);
      }

      currentAction.addAction(this.actionContainers);
   }

   public String getName() {
      return "nonprojective";
   }

   public GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException {
      return ((StackConfig)configuration).getInput().isEmpty() ? this.updateActionContainers(history, 3, ((StackConfig)configuration).getDependencyGraph().getDefaultRootEdgeLabels()) : this.updateActionContainers(history, 1, (LabelSet)null);
   }
}
