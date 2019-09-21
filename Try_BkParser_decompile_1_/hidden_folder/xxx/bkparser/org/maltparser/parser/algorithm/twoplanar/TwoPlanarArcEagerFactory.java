package org.maltparser.parser.algorithm.twoplanar;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.guide.OracleGuide;
import org.maltparser.parser.history.GuideUserHistory;

public class TwoPlanarArcEagerFactory extends TwoPlanarFactory {
   public TwoPlanarArcEagerFactory(DependencyParserConfig _manager) {
      super(_manager);
   }

   public TransitionSystem makeTransitionSystem() throws MaltChainedException {
      if (this.manager.isLoggerInfoEnabled()) {
         this.manager.logInfoMessage("  Transition system    : 2-Planar Arc-Eager\n");
      }

      return new TwoPlanar(this.manager.getPropagationManager());
   }

   public OracleGuide makeOracleGuide(GuideUserHistory history) throws MaltChainedException {
      if (this.manager.isLoggerInfoEnabled()) {
         this.manager.logInfoMessage("  Oracle               : 2-Planar Arc-Eager\n");
      }

      return new TwoPlanarArcEagerOracle(this.manager, history);
   }
}
