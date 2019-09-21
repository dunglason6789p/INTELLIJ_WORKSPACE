package org.maltparser.parser.guide.decision;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.parser.guide.Model;
import org.maltparser.parser.history.action.GuideDecision;

public interface DecisionModel extends Model {
   void addInstance(FeatureModel var1, GuideDecision var2) throws MaltChainedException;

   boolean predict(FeatureModel var1, GuideDecision var2) throws MaltChainedException;

   FeatureVector predictExtract(FeatureModel var1, GuideDecision var2) throws MaltChainedException;

   FeatureVector extract(FeatureModel var1) throws MaltChainedException;

   boolean predictFromKBestList(FeatureModel var1, GuideDecision var2) throws MaltChainedException;

   int getDecisionIndex();
}
