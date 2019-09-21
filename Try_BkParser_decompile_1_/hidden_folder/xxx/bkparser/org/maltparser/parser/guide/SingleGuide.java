package org.maltparser.parser.guide;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.feature.FeatureVector;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.guide.decision.BranchedDecisionModel;
import org.maltparser.parser.guide.decision.DecisionModel;
import org.maltparser.parser.guide.decision.OneDecisionModel;
import org.maltparser.parser.guide.decision.SeqDecisionModel;
import org.maltparser.parser.history.action.GuideDecision;
import org.maltparser.parser.history.action.MultipleDecision;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.container.TableContainer;

public class SingleGuide implements ClassifierGuide {
   private final DependencyParserConfig configuration;
   private final ClassifierGuide.GuideMode guideMode;
   private final FeatureModel featureModel2;
   private DecisionModel decisionModel = null;
   private String guideName;

   public SingleGuide(AlgoritmInterface algorithm, ClassifierGuide.GuideMode guideMode) throws MaltChainedException {
      this.configuration = algorithm.getManager();
      this.guideMode = guideMode;
      String featureModelFileName = this.getConfiguration().getOptionValue("guide", "features").toString().trim();
      String dataSplitColumn = this.getConfiguration().getOptionValue("guide", "data_split_column").toString().trim();
      String dataSplitStructure = this.getConfiguration().getOptionValue("guide", "data_split_structure").toString().trim();
      this.featureModel2 = this.getConfiguration().getFeatureModelManager().getFeatureModel(findURL(featureModelFileName, this.getConfiguration()), 0, algorithm.getParserRegistry(), dataSplitColumn, dataSplitStructure);
   }

   public void addInstance(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
      if (this.decisionModel == null) {
         if (decision instanceof SingleDecision) {
            this.initDecisionModel((SingleDecision)decision);
         } else if (decision instanceof MultipleDecision && decision.numberOfDecisions() > 0) {
            this.initDecisionModel(((MultipleDecision)decision).getSingleDecision(0));
         }
      }

      this.decisionModel.addInstance(featureModel, decision);
   }

   public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
      if (this.decisionModel != null) {
         this.decisionModel.finalizeSentence(dependencyGraph);
      }

   }

   public void noMoreInstances() throws MaltChainedException {
      if (this.decisionModel != null) {
         this.decisionModel.noMoreInstances(this.featureModel2);
      } else {
         this.configuration.logDebugMessage("The guide cannot create any models because there is no decision model. ");
      }

   }

   public void terminate() throws MaltChainedException {
      if (this.decisionModel != null) {
         this.decisionModel.terminate();
         this.decisionModel = null;
      }

   }

   public void predict(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
      if (this.decisionModel == null) {
         if (decision instanceof SingleDecision) {
            this.initDecisionModel((SingleDecision)decision);
         } else if (decision instanceof MultipleDecision && decision.numberOfDecisions() > 0) {
            this.initDecisionModel(((MultipleDecision)decision).getSingleDecision(0));
         }
      }

      this.decisionModel.predict(featureModel, decision);
   }

   public FeatureVector predictExtract(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
      if (this.decisionModel == null) {
         if (decision instanceof SingleDecision) {
            this.initDecisionModel((SingleDecision)decision);
         } else if (decision instanceof MultipleDecision && decision.numberOfDecisions() > 0) {
            this.initDecisionModel(((MultipleDecision)decision).getSingleDecision(0));
         }
      }

      return this.decisionModel.predictExtract(featureModel, decision);
   }

   public FeatureVector extract(FeatureModel featureModel) throws MaltChainedException {
      return this.decisionModel.extract(featureModel);
   }

   public boolean predictFromKBestList(FeatureModel featureModel, GuideDecision decision) throws MaltChainedException {
      if (this.decisionModel != null) {
         return this.decisionModel.predictFromKBestList(featureModel, decision);
      } else {
         throw new GuideException("The decision model cannot be found. ");
      }
   }

   public DecisionModel getDecisionModel() {
      return this.decisionModel;
   }

   public DependencyParserConfig getConfiguration() {
      return this.configuration;
   }

   public ClassifierGuide.GuideMode getGuideMode() {
      return this.guideMode;
   }

   protected void initDecisionModel(SingleDecision decision) throws MaltChainedException {
      if (decision.getRelationToNextDecision() == TableContainer.RelationToNextDecision.SEQUANTIAL) {
         this.decisionModel = new SeqDecisionModel(this);
      } else if (decision.getRelationToNextDecision() == TableContainer.RelationToNextDecision.BRANCHED) {
         this.decisionModel = new BranchedDecisionModel(this);
      } else if (decision.getRelationToNextDecision() == TableContainer.RelationToNextDecision.NONE) {
         this.decisionModel = new OneDecisionModel(this);
      }

   }

   public String getGuideName() {
      return this.guideName;
   }

   public void setGuideName(String guideName) {
      this.guideName = guideName;
   }

   public static URL findURL(String specModelFileName, DependencyParserConfig config) throws MaltChainedException {
      URL url = null;
      File specFile = config.getFile(specModelFileName);
      if (specFile != null && specFile.exists()) {
         try {
            url = new URL("file:///" + specFile.getAbsolutePath());
         } catch (MalformedURLException var5) {
            throw new MaltChainedException("Malformed URL: " + specFile, var5);
         }
      } else {
         url = config.getConfigFileEntryURL(specModelFileName);
      }

      return url;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      return sb.toString();
   }
}
