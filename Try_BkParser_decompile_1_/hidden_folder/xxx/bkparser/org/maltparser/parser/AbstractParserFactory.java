package org.maltparser.parser;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.AbstractFeatureFactory;
import org.maltparser.parser.guide.OracleGuide;
import org.maltparser.parser.history.GuideUserHistory;

public interface AbstractParserFactory extends AbstractFeatureFactory {
   ParserConfiguration makeParserConfiguration() throws MaltChainedException;

   TransitionSystem makeTransitionSystem() throws MaltChainedException;

   OracleGuide makeOracleGuide(GuideUserHistory var1) throws MaltChainedException;
}
