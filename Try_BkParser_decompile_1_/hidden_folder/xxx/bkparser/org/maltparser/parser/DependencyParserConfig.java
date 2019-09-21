package org.maltparser.parser;

import org.maltparser.core.config.Configuration;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModelManager;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.syntaxgraph.DependencyStructure;

public interface DependencyParserConfig extends Configuration {
   void parse(DependencyStructure var1) throws MaltChainedException;

   void oracleParse(DependencyStructure var1, DependencyStructure var2) throws MaltChainedException;

   DataFormatInstance getDataFormatInstance();

   FeatureModelManager getFeatureModelManager();

   PropagationManager getPropagationManager();

   AbstractParserFactory getParserFactory();
}
