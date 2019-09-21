package org.maltparser.parser.guide;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.syntaxgraph.DependencyStructure;

public interface Model {
   void finalizeSentence(DependencyStructure var1) throws MaltChainedException;

   void noMoreInstances(FeatureModel var1) throws MaltChainedException;

   void terminate() throws MaltChainedException;

   ClassifierGuide getGuide();

   String getModelName() throws MaltChainedException;
}
