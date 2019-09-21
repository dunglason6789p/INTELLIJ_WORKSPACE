package org.maltparser.parser.guide.decision;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.GuideException;
import org.maltparser.parser.guide.instance.AtomicModel;
import org.maltparser.parser.guide.instance.FeatureDivideModel;
import org.maltparser.parser.guide.instance.InstanceModel;
import org.maltparser.parser.history.action.GuideDecision;
import org.maltparser.parser.history.action.MultipleDecision;
import org.maltparser.parser.history.action.SingleDecision;

public class OneDecisionModel implements DecisionModel {
   private final ClassifierGuide guide;
   private final String modelName;
   private final int decisionIndex;
   private final DecisionModel prevDecisionModel;
   private final String branchedDecisionSymbols;
   private InstanceModel instanceModel;

   public OneDecisionModel(ClassifierGuide _guide) throws MaltChainedException {
      this.branchedDecisionSymbols = "";
      this.guide = _guide;
      this.decisionIndex = 0;
      if (this.guide.getGuideName() != null && !this.guide.getGuideName().equals("")) {
         this.modelName = this.guide.getGuideName() + ".odm" + this.decisionIndex;
      } else {
         this.modelName = "odm" + this.decisionIndex;
      }

      this.prevDecisionModel = null;
   }

   public OneDecisionModel(ClassifierGuide _guide, DecisionModel _prevDecisionModel, String _branchedDecisionSymbol) throws MaltChainedException {
      this.prevDecisionModel = _prevDecisionModel;
      this.decisionIndex = this.prevDecisionModel.getDecisionIndex() + 1;
      if (_branchedDecisionSymbol != null && _branchedDecisionSymbol.length() > 0) {
         this.branchedDecisionSymbols = _branchedDecisionSymbol;
         this.modelName = "odm" + this.decisionIndex + this.branchedDecisionSymbols;
      } else {
         this.branchedDecisionSymbols = "";
         this.modelName = "odm" + this.decisionIndex;
      }

      this.guide = _guide;
   }

   private final void initInstanceModel(FeatureModel featureModel, String subModelName) throws MaltChainedException {
      if (featureModel.hasDivideFeatureFunction()) {
         this.instanceModel = new FeatureDivideModel(this);
      } else {
         this.instanceModel = new AtomicModel(-1, this);
      }

   }

   public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
      if (this.instanceModel != null) {
         this.instanceModel.finalizeSentence(dependencyGraph);
      }

   }

   public void noMoreInstances(FeatureModel featureModel) throws MaltChainedException {
      if (this.guide.getGuideMode() == ClassifierGuide.GuideMode.CLASSIFY) {
         throw new GuideException("The decision model could not create it's model. ");
      } else {
         if (this.instanceModel != null) {
            this.instanceModel.noMoreInstances(featureModel);
            this.instanceModel.train();
         }

      }
   }

   public void terminate() throws MaltChainedException {
      if (this.instanceModel != null) {
         this.instanceModel.terminate();
         this.instanceModel = null;
      }

   }

   public void addInstance(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
      featureModel.update();
      SingleDecision singleDecision = decision instanceof SingleDecision ? (SingleDecision)decision : ((MultipleDecision)decision).getSingleDecision(this.decisionIndex);
      if (this.instanceModel == null) {
         this.initInstanceModel(featureModel, singleDecision.getTableContainer().getTableContainerName());
      }

      this.instanceModel.addInstance(featureModel.getFeatureVector(this.branchedDecisionSymbols, singleDecision.getTableContainer().getTableContainerName()), singleDecision);
   }

   public boolean predict(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
      featureModel.update();
      SingleDecision singleDecision = decision instanceof SingleDecision ? (SingleDecision)decision : ((MultipleDecision)decision).getSingleDecision(this.decisionIndex);
      if (this.instanceModel == null) {
         this.initInstanceModel(featureModel, singleDecision.getTableContainer().getTableContainerName());
      }

      return this.instanceModel.predict(featureModel.getFeatureVector(this.branchedDecisionSymbols, singleDecision.getTableContainer().getTableContainerName()), singleDecision);
   }

   public FeatureVector predictExtract(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
      featureModel.update();
      SingleDecision singleDecision = decision instanceof SingleDecision ? (SingleDecision)decision : ((MultipleDecision)decision).getSingleDecision(this.decisionIndex);
      if (this.instanceModel == null) {
         this.initInstanceModel(featureModel, singleDecision.getTableContainer().getTableContainerName());
      }

      return this.instanceModel.predictExtract(featureModel.getFeatureVector(this.branchedDecisionSymbols, singleDecision.getTableContainer().getTableContainerName()), singleDecision);
   }

   public FeatureVector extract(FeatureModel featureModel) throws MaltChainedException {
      featureModel.update();
      return null;
   }

   public boolean predictFromKBestList(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
      return decision instanceof SingleDecision ? ((SingleDecision)decision).updateFromKBestList() : ((MultipleDecision)decision).getSingleDecision(this.decisionIndex).updateFromKBestList();
   }

   public ClassifierGuide getGuide() {
      return this.guide;
   }

   public String getModelName() {
      return this.modelName;
   }

   public int getDecisionIndex() {
      return this.decisionIndex;
   }

   public DecisionModel getPrevDecisionModel() {
      return this.prevDecisionModel;
   }

   public String toString() {
      return this.modelName;
   }
}
