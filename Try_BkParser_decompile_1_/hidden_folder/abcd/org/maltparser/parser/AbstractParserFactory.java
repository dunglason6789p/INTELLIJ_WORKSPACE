/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.AbstractFeatureFactory;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.guide.OracleGuide;
import org.maltparser.parser.history.GuideUserHistory;

public interface AbstractParserFactory
extends AbstractFeatureFactory {
    public ParserConfiguration makeParserConfiguration() throws MaltChainedException;

    public TransitionSystem makeTransitionSystem() throws MaltChainedException;

    public OracleGuide makeOracleGuide(GuideUserHistory var1) throws MaltChainedException;
}

