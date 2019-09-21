package org.maltparser.parser.algorithm.nivre;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.feature.function.Function;
import org.maltparser.parser.AbstractParserFactory;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParserRegistry;

public abstract class NivreFactory implements AbstractParserFactory {
   protected final DependencyParserConfig manager;

   public NivreFactory(DependencyParserConfig _manager) {
      this.manager = _manager;
   }

   public ParserConfiguration makeParserConfiguration() throws MaltChainedException {
      boolean allowRoot = (Boolean)this.manager.getOptionValue("nivre", "allow_root");
      boolean allowReduce = (Boolean)this.manager.getOptionValue("nivre", "allow_reduce");
      boolean enforceTree = (Boolean)this.manager.getOptionValue("nivre", "enforce_tree") && this.manager.getOptionValueString("config", "flowchart").equals("parse");
      if (this.manager.isLoggerInfoEnabled()) {
         this.manager.logInfoMessage("  Parser configuration : Nivre with allow_root=" + allowRoot + ", allow_reduce=" + allowReduce + " and enforce_tree=" + enforceTree + "\n");
      }

      return new NivreConfig(allowRoot, allowReduce, enforceTree);
   }

   public Function makeFunction(String subFunctionName, FeatureRegistry registry) throws MaltChainedException {
      AlgoritmInterface algorithm = ((ParserRegistry)registry).getAlgorithm();
      return new NivreAddressFunction(subFunctionName, algorithm);
   }
}
