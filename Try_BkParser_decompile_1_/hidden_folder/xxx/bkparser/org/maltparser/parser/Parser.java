package org.maltparser.parser;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;

public abstract class Parser extends ParsingAlgorithm {
   public Parser(DependencyParserConfig manager, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
      super(manager, symbolTableHandler);
   }

   public abstract DependencyStructure parse(DependencyStructure var1) throws MaltChainedException;
}
