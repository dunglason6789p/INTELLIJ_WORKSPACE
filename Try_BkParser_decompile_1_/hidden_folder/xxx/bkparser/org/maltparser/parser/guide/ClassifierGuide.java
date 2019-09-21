package org.maltparser.parser.guide;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.parser.history.action.GuideDecision;

public interface ClassifierGuide extends Guide {
   void addInstance(FeatureModel var1, GuideDecision var2) throws MaltChainedException;

   void noMoreInstances() throws MaltChainedException;

   void predict(FeatureModel var1, GuideDecision var2) throws MaltChainedException;

   FeatureVector predictExtract(FeatureModel var1, GuideDecision var2) throws MaltChainedException;

   FeatureVector extract(FeatureModel var1) throws MaltChainedException;

   boolean predictFromKBestList(FeatureModel var1, GuideDecision var2) throws MaltChainedException;

   ClassifierGuide.GuideMode getGuideMode();

   public static enum GuideMode {
      BATCH,
      CLASSIFY;

      private GuideMode() {
      }
   }
}
