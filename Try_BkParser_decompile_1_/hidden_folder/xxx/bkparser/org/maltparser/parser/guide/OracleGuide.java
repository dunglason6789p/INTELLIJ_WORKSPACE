package org.maltparser.parser.guide;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.history.action.GuideUserAction;

public interface OracleGuide extends Guide {
   GuideUserAction predict(DependencyStructure var1, ParserConfiguration var2) throws MaltChainedException;
}
