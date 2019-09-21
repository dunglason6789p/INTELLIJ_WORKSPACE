package org.maltparser.parser.algorithm.covington;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.guide.OracleGuide;
import org.maltparser.parser.history.GuideUserHistory;

public class CovingtonNonProjFactory extends CovingtonFactory {
   public CovingtonNonProjFactory(DependencyParserConfig _manager) {
      super(_manager);
   }

   public TransitionSystem makeTransitionSystem() throws MaltChainedException {
      if (this.manager.isLoggerInfoEnabled()) {
         this.manager.logInfoMessage("  Transition system    : Non-Projective\n");
      }

      return new NonProjective(this.manager.getPropagationManager());
   }

   public OracleGuide makeOracleGuide(GuideUserHistory history) throws MaltChainedException {
      if (this.manager.isLoggerInfoEnabled()) {
         this.manager.logInfoMessage("  Oracle               : Covington\n");
      }

      return new CovingtonOracle(this.manager, history);
   }
}
