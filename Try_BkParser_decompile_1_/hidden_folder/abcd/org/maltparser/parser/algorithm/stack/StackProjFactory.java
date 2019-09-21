/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.stack;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.algorithm.stack.Projective;
import org.maltparser.parser.algorithm.stack.ProjectiveOracle;
import org.maltparser.parser.algorithm.stack.StackFactory;
import org.maltparser.parser.guide.OracleGuide;
import org.maltparser.parser.history.GuideUserHistory;

public class StackProjFactory
extends StackFactory {
    public StackProjFactory(DependencyParserConfig _manager) {
        super(_manager);
    }

    @Override
    public TransitionSystem makeTransitionSystem() throws MaltChainedException {
        if (this.manager.isLoggerInfoEnabled()) {
            this.manager.logInfoMessage("  Transition system    : Projective\n");
        }
        return new Projective(this.manager.getPropagationManager());
    }

    @Override
    public OracleGuide makeOracleGuide(GuideUserHistory history) throws MaltChainedException {
        if (this.manager.isLoggerInfoEnabled()) {
            this.manager.logInfoMessage("  Oracle               : Projective\n");
        }
        return new ProjectiveOracle(this.manager, history);
    }
}

