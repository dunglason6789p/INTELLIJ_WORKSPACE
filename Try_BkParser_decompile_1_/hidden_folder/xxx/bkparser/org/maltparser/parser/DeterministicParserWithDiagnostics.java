package org.maltparser.parser;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModel;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.guide.ClassifierGuide;
import org.maltparser.parser.guide.SingleGuide;
import org.maltparser.parser.history.action.GuideDecision;
import org.maltparser.parser.history.action.GuideUserAction;

public class DeterministicParserWithDiagnostics extends Parser {
   private final Diagnostics diagnostics;
   private int parseCount;
   private final FeatureModel featureModel;

   public DeterministicParserWithDiagnostics(DependencyParserConfig manager, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
      super(manager, symbolTableHandler);
      this.diagnostics = new Diagnostics(manager.getOptionValue("singlemalt", "diafile").toString());
      this.registry.setAlgorithm(this);
      this.setGuide(new SingleGuide(this, ClassifierGuide.GuideMode.CLASSIFY));
      String featureModelFileName = manager.getOptionValue("guide", "features").toString().trim();
      if (manager.isLoggerInfoEnabled()) {
         manager.logDebugMessage("  Feature model        : " + featureModelFileName + "\n");
         manager.logDebugMessage("  Classifier           : " + manager.getOptionValueString("guide", "learner") + "\n");
      }

      String dataSplitColumn = manager.getOptionValue("guide", "data_split_column").toString().trim();
      String dataSplitStructure = manager.getOptionValue("guide", "data_split_structure").toString().trim();
      this.featureModel = manager.getFeatureModelManager().getFeatureModel(SingleGuide.findURL(featureModelFileName, manager), 0, this.getParserRegistry(), dataSplitColumn, dataSplitStructure);
   }

   public DependencyStructure parse(DependencyStructure parseDependencyGraph) throws MaltChainedException {
      this.parserState.clear();
      this.parserState.initialize(parseDependencyGraph);
      this.currentParserConfiguration = this.parserState.getConfiguration();
      ++this.parseCount;
      this.diagnostics.writeToDiaFile(this.parseCount + "");

      while(!this.parserState.isTerminalState()) {
         GuideUserAction action = this.parserState.getTransitionSystem().getDeterministicAction(this.parserState.getHistory(), this.currentParserConfiguration);
         if (action == null) {
            action = this.predict();
         } else {
            this.diagnostics.writeToDiaFile(" *");
         }

         this.diagnostics.writeToDiaFile(" " + this.parserState.getTransitionSystem().getActionString(action));
         this.parserState.apply(action);
      }

      this.copyEdges(this.currentParserConfiguration.getDependencyGraph(), parseDependencyGraph);
      this.copyDynamicInput(this.currentParserConfiguration.getDependencyGraph(), parseDependencyGraph);
      parseDependencyGraph.linkAllTreesToRoot();
      this.diagnostics.writeToDiaFile("\n");
      return parseDependencyGraph;
   }

   private GuideUserAction predict() throws MaltChainedException {
      GuideUserAction currentAction = this.parserState.getHistory().getEmptyGuideUserAction();

      try {
         this.classifierGuide.predict(this.featureModel, (GuideDecision)currentAction);

         while(!this.parserState.permissible(currentAction)) {
            if (!this.classifierGuide.predictFromKBestList(this.featureModel, (GuideDecision)currentAction)) {
               currentAction = this.getParserState().getTransitionSystem().defaultAction(this.parserState.getHistory(), this.currentParserConfiguration);
               break;
            }
         }

         return currentAction;
      } catch (NullPointerException var3) {
         throw new MaltChainedException("The guide cannot be found. ", var3);
      }
   }

   public void terminate() throws MaltChainedException {
      this.diagnostics.closeDiaWriter();
   }
}
