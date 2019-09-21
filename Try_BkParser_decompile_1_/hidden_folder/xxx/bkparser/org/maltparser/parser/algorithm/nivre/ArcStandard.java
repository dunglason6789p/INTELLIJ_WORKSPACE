package org.maltparser.parser.algorithm.nivre;

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

public class ArcStandard extends TransitionSystem {
   protected static final int SHIFT = 1;
   protected static final int RIGHTARC = 2;
   protected static final int LEFTARC = 3;

   public ArcStandard(PropagationManager propagationManager) throws MaltChainedException {
      super(propagationManager);
   }

   public void apply(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
      NivreConfig nivreConfig = (NivreConfig)config;
      Stack<DependencyNode> stack = nivreConfig.getStack();
      Stack<DependencyNode> input = nivreConfig.getInput();
      currentAction.getAction(this.actionContainers);
      Edge e = null;
      switch(this.transActionContainer.getActionCode()) {
      case 2:
         e = nivreConfig.getDependencyStructure().addDependencyEdge(((DependencyNode)stack.peek()).getIndex(), ((DependencyNode)input.peek()).getIndex());
         this.addEdgeLabels(e);
         input.pop();
         if (!((DependencyNode)stack.peek()).isRoot()) {
            input.push(stack.pop());
         }
         break;
      case 3:
         e = nivreConfig.getDependencyStructure().addDependencyEdge(((DependencyNode)input.peek()).getIndex(), ((DependencyNode)stack.peek()).getIndex());
         this.addEdgeLabels(e);
         stack.pop();
         break;
      default:
         stack.push(input.pop());
      }

   }

   public GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException {
      NivreConfig nivreConfig = (NivreConfig)config;
      return !nivreConfig.isAllowRoot() && ((DependencyNode)nivreConfig.getStack().peek()).isRoot() ? this.updateActionContainers(history, 1, (LabelSet)null) : null;
   }

   protected void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException {
      ttable.addTransition(1, "SH", false, (TransitionTable)null);
      ttable.addTransition(2, "RA", true, (TransitionTable)null);
      ttable.addTransition(3, "LA", true, (TransitionTable)null);
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
      return "nivrestandard";
   }

   public boolean permissible(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
      currentAction.getAction(this.actionContainers);
      int trans = this.transActionContainer.getActionCode();
      if ((trans == 3 || trans == 2) && !this.isActionContainersLabeled()) {
         return false;
      } else {
         DependencyNode stackTop = (DependencyNode)((NivreConfig)config).getStack().peek();
         if (!((NivreConfig)config).isAllowRoot() && stackTop.isRoot() && trans != 1) {
            return false;
         } else {
            return trans != 3 || !stackTop.isRoot();
         }
      }
   }

   public GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException {
      return this.updateActionContainers(history, 1, (LabelSet)null);
   }
}
