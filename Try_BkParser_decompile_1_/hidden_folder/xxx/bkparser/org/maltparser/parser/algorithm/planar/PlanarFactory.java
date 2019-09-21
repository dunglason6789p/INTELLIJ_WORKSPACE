package org.maltparser.parser.algorithm.planar;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.feature.function.Function;
import org.maltparser.parser.AbstractParserFactory;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParserRegistry;

public abstract class PlanarFactory implements AbstractParserFactory {
   protected final DependencyParserConfig manager;

   public PlanarFactory(DependencyParserConfig _manager) {
      this.manager = _manager;
   }

   public ParserConfiguration makeParserConfiguration() throws MaltChainedException {
      if (this.manager.isLoggerInfoEnabled()) {
         this.manager.logInfoMessage("  Parser configuration : Planar with no_covered_roots = " + this.manager.getOptionValue("planar", "no_covered_roots").toString().toUpperCase() + ", " + "acyclicity = " + this.manager.getOptionValue("planar", "acyclicity").toString().toUpperCase() + ", connectedness = " + this.manager.getOptionValue("planar", "connectedness").toString().toUpperCase() + ", planar root handling = " + this.manager.getOptionValue("2planar", "planar_root_handling").toString().toUpperCase() + "\n");
      }

      return new PlanarConfig(this.manager.getOptionValue("planar", "no_covered_roots").toString(), this.manager.getOptionValue("planar", "acyclicity").toString(), this.manager.getOptionValue("planar", "connectedness").toString(), this.manager.getOptionValue("multiplanar", "planar_root_handling").toString());
   }

   public Function makeFunction(String subFunctionName, FeatureRegistry registry) throws MaltChainedException {
      AlgoritmInterface algorithm = ((ParserRegistry)registry).getAlgorithm();
      return new PlanarAddressFunction(subFunctionName, algorithm);
   }
}
