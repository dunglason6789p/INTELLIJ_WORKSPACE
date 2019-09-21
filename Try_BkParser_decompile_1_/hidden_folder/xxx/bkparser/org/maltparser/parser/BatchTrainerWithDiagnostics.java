package org.maltparser.parser;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.OracleGuide;
import org.maltparser.parser.guide.SingleGuide;
import org.maltparser.parser.history.action.GuideDecision;
import org.maltparser.parser.history.action.GuideUserAction;

public class BatchTrainerWithDiagnostics extends Trainer {
   private final Diagnostics diagnostics;
   private final OracleGuide oracleGuide;
   private int parseCount;
   private final FeatureModel featureModel;

   public BatchTrainerWithDiagnostics(DependencyParserConfig manager, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
      super(manager, symbolTableHandler);
      this.diagnostics = new Diagnostics(manager.getOptionValue("singlemalt", "diafile").toString());
      this.registry.setAlgorithm(this);
      this.setGuide(new SingleGuide(this, ClassifierGuide.GuideMode.BATCH));
      String featureModelFileName = manager.getOptionValue("guide", "features").toString().trim();
      if (manager.isLoggerInfoEnabled()) {
         manager.logDebugMessage("  Feature model        : " + featureModelFileName + "\n");
         manager.logDebugMessage("  Learner              : " + manager.getOptionValueString("guide", "learner").toString() + "\n");
      }

      String dataSplitColumn = manager.getOptionValue("guide", "data_split_column").toString().trim();
      String dataSplitStructure = manager.getOptionValue("guide", "data_split_structure").toString().trim();
      this.featureModel = manager.getFeatureModelManager().getFeatureModel(SingleGuide.findURL(featureModelFileName, manager), 0, this.getParserRegistry(), dataSplitColumn, dataSplitStructure);
      manager.writeInfoToConfigFile("\nFEATURE MODEL\n");
      manager.writeInfoToConfigFile(this.featureModel.toString());
      this.oracleGuide = this.parserState.getFactory().makeOracleGuide(this.parserState.getHistory());
   }

   public DependencyStructure parse(DependencyStructure goldDependencyGraph, DependencyStructure parseDependencyGraph) throws MaltChainedException {
      this.parserState.clear();
      this.parserState.initialize(parseDependencyGraph);
      this.currentParserConfiguration = this.parserState.getConfiguration();
      ++this.parseCount;
      this.diagnostics.writeToDiaFile(this.parseCount + "");
      TransitionSystem transitionSystem = this.parserState.getTransitionSystem();

      while(!this.parserState.isTerminalState()) {
         GuideUserAction action = transitionSystem.getDeterministicAction(this.parserState.getHistory(), this.currentParserConfiguration);
         if (action == null) {
            action = this.oracleGuide.predict(goldDependencyGraph, this.currentParserConfiguration);

            try {
               this.classifierGuide.addInstance(this.featureModel, (GuideDecision)action);
            } catch (NullPointerException var6) {
               throw new MaltChainedException("The guide cannot be found. ", var6);
            }
         } else {
            this.diagnostics.writeToDiaFile(" *");
         }

         this.diagnostics.writeToDiaFile(" " + transitionSystem.getActionString(action));
         this.parserState.apply(action);
      }

      this.copyEdges(this.currentParserConfiguration.getDependencyGraph(), parseDependencyGraph);
      parseDependencyGraph.linkAllTreesToRoot();
      this.oracleGuide.finalizeSentence(parseDependencyGraph);
      this.diagnostics.writeToDiaFile("\n");
      return parseDependencyGraph;
   }

   public OracleGuide getOracleGuide() {
      return this.oracleGuide;
   }

   public void train() throws MaltChainedException {
   }

   public void terminate() throws MaltChainedException {
      this.diagnostics.closeDiaWriter();
   }
}
