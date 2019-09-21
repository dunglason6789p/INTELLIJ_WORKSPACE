package org.maltparser.parser.guide.instance;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.parser.guide.Model;
import org.maltparser.parser.history.action.SingleDecision;

public interface InstanceModel extends Model {
   void addInstance(FeatureVector var1, SingleDecision var2) throws MaltChainedException;

   boolean predict(FeatureVector var1, SingleDecision var2) throws MaltChainedException;

   FeatureVector predictExtract(FeatureVector var1, SingleDecision var2) throws MaltChainedException;

   FeatureVector extract(FeatureVector var1) throws MaltChainedException;

   void train() throws MaltChainedException;

   void increaseFrequency();

   void decreaseFrequency();
}
