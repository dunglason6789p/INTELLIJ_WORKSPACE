package org.maltparser.parser.algorithm.covington;

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

public class NonProjective extends TransitionSystem {
   protected static final int SHIFT = 1;
   protected static final int NOARC = 2;
   protected static final int RIGHTARC = 3;
   protected static final int LEFTARC = 4;

   public NonProjective(PropagationManager propagationManager) throws MaltChainedException {
      super(propagationManager);
   }

   public void apply(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
      CovingtonConfig covingtonConfig = (CovingtonConfig)config;
      currentAction.getAction(this.actionContainers);
      Edge e = null;
      switch(this.transActionContainer.getActionCode()) {
      case 3:
         e = covingtonConfig.getDependencyGraph().addDependencyEdge(covingtonConfig.getLeftTarget().getIndex(), covingtonConfig.getRightTarget().getIndex());
         this.addEdgeLabels(e);
         break;
      case 4:
         e = covingtonConfig.getDependencyGraph().addDependencyEdge(covingtonConfig.getRightTarget().getIndex(), covingtonConfig.getLeftTarget().getIndex());
         this.addEdgeLabels(e);
      }

      this.update(covingtonConfig, this.transActionContainer.getActionCode());
   }

   private void update(CovingtonConfig covingtonConfig, int trans) {
      if (trans == 1) {
         covingtonConfig.setRight(covingtonConfig.getRight() + 1);
         covingtonConfig.setLeft(covingtonConfig.getRight() - 1);
      } else {
         DependencyNode rightNode = covingtonConfig.getRightTarget();
         int leftstop = covingtonConfig.getLeftstop();
         int left = covingtonConfig.getLeft();
         --left;

         for(DependencyNode leftNode = null; left >= leftstop; --left) {
            leftNode = (DependencyNode)covingtonConfig.getInput().get(left);
            if (rightNode.findComponent().getIndex() != leftNode.findComponent().getIndex() && (!leftNode.hasHead() || !rightNode.hasHead())) {
               break;
            }
         }

         if (left < leftstop) {
            covingtonConfig.setRight(covingtonConfig.getRight() + 1);
            covingtonConfig.setLeft(covingtonConfig.getRight() - 1);
         } else {
            covingtonConfig.setLeft(left);
         }
      }

   }

   public GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException {
      CovingtonConfig covingtonConfig = (CovingtonConfig)config;
      return !covingtonConfig.isAllowRoot() && covingtonConfig.getLeftTarget().isRoot() ? this.updateActionContainers(history, 2, (LabelSet)null) : null;
   }

   protected void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException {
      ttable.addTransition(1, "SH", false, (TransitionTable)null);
      ttable.addTransition(2, "NA", false, (TransitionTable)null);
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
      return "covnonproj";
   }

   public boolean permissible(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
      CovingtonConfig covingtonConfig = (CovingtonConfig)config;
      DependencyNode leftTarget = covingtonConfig.getLeftTarget();
      DependencyNode rightTarget = covingtonConfig.getRightTarget();
      DependencyStructure dg = covingtonConfig.getDependencyGraph();
      currentAction.getAction(this.actionContainers);
      int trans = this.transActionContainer.getActionCode();
      if (trans == 1 && !covingtonConfig.isAllowShift()) {
         return false;
      } else if ((trans == 4 || trans == 3) && !this.isActionContainersLabeled()) {
         return false;
      } else if (trans == 4 && leftTarget.isRoot()) {
         return false;
      } else if (trans == 4 && dg.hasLabeledDependency(leftTarget.getIndex())) {
         return false;
      } else {
         return trans != 3 || !dg.hasLabeledDependency(rightTarget.getIndex());
      }
   }

   public GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException {
      return this.updateActionContainers(history, 2, (LabelSet)null);
   }
}
