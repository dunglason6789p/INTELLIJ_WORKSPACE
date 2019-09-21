package org.maltparser.parser.guide;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.DependencyParserConfig;

public interface Guide {
   void finalizeSentence(DependencyStructure var1) throws MaltChainedException;

   void terminate() throws MaltChainedException;

   DependencyParserConfig getConfiguration();

   String getGuideName();

   void setGuideName(String var1);
}
