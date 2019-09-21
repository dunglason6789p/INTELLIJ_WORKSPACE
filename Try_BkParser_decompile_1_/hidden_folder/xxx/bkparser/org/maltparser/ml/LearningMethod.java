package org.maltparser.ml;

import java.io.BufferedWriter;
import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.history.action.SingleDecision;

public interface LearningMethod {
   int BATCH = 0;
   int CLASSIFY = 1;

   void addInstance(SingleDecision var1, FeatureVector var2) throws MaltChainedException;

   void finalizeSentence(DependencyStructure var1) throws MaltChainedException;

   void noMoreInstances() throws MaltChainedException;

   void train() throws MaltChainedException;

   void moveAllInstances(LearningMethod var1, FeatureFunction var2, ArrayList<Integer> var3) throws MaltChainedException;

   void terminate() throws MaltChainedException;

   boolean predict(FeatureVector var1, SingleDecision var2) throws MaltChainedException;

   BufferedWriter getInstanceWriter();

   void increaseNumberOfInstances();

   void decreaseNumberOfInstances();
}
