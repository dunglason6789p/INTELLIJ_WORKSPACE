/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.stack;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.algorithm.stack.NonProjective;
import org.maltparser.parser.algorithm.stack.StackFactory;
import org.maltparser.parser.algorithm.stack.SwapLazyOracle;
import org.maltparser.parser.guide.OracleGuide;
import org.maltparser.parser.history.GuideUserHistory;

public class StackSwapLazyFactory
extends StackFactory {
    public StackSwapLazyFactory(DependencyParserConfig _manager) {
        super(_manager);
    }

    @Override
    public TransitionSystem makeTransitionSystem() throws MaltChainedException {
        if (this.manager.isLoggerInfoEnabled()) {
            this.manager.logInfoMessage("  Transition system    : Non-Projective\n");
        }
        return new NonProjective(this.manager.getPropagationManager());
    }

    @Override
    public OracleGuide makeOracleGuide(GuideUserHistory history) throws MaltChainedException {
        if (this.manager.isLoggerInfoEnabled()) {
            this.manager.logInfoMessage("  Oracle               : Swap-Lazy\n");
        }
        return new SwapLazyOracle(this.manager, history);
    }
}

