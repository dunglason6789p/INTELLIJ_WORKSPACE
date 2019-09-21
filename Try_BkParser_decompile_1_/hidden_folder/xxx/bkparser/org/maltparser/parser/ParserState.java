package org.maltparser.parser;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.History;
import org.maltparser.parser.history.HistoryList;
import org.maltparser.parser.history.HistoryStructure;
import org.maltparser.parser.history.action.GuideUserAction;

public class ParserState {
   private final AbstractParserFactory factory;
   private final GuideUserHistory history;
   private final TransitionSystem transitionSystem;
   private final HistoryStructure historyStructure;
   private final ParserConfiguration config;

   public ParserState(DependencyParserConfig manager, SymbolTableHandler symbolTableHandler, AbstractParserFactory factory) throws MaltChainedException {
      this.factory = factory;
      this.historyStructure = new HistoryList();
      this.transitionSystem = factory.makeTransitionSystem();
      String decisionSettings = manager.getOptionValue("guide", "decision_settings").toString().trim();
      this.getTransitionSystem().initTableHandlers(decisionSettings, symbolTableHandler);
      int kBestSize = (Integer)manager.getOptionValue("guide", "kbest");
      String classitem_separator = manager.getOptionValue("guide", "classitem_separator").toString();
      this.history = new History(decisionSettings, classitem_separator, this.getTransitionSystem().getTableHandlers(), kBestSize);
      this.getTransitionSystem().initTransitionSystem(this.history);
      this.config = factory.makeParserConfiguration();
   }

   public void clear() throws MaltChainedException {
      this.history.clear();
      this.historyStructure.clear();
   }

   public GuideUserHistory getHistory() {
      return this.history;
   }

   public TransitionSystem getTransitionSystem() {
      return this.transitionSystem;
   }

   public HistoryStructure getHistoryStructure() {
      return this.historyStructure;
   }

   public void initialize(DependencyStructure dependencyStructure) throws MaltChainedException {
      this.config.clear();
      this.config.setDependencyGraph(dependencyStructure);
      this.config.initialize();
   }

   public boolean isTerminalState() throws MaltChainedException {
      return this.config.isTerminalState();
   }

   public boolean permissible(GuideUserAction currentAction) throws MaltChainedException {
      return this.transitionSystem.permissible(currentAction, this.config);
   }

   public void apply(GuideUserAction currentAction) throws MaltChainedException {
      this.transitionSystem.apply(currentAction, this.config);
   }

   public int nConfigurations() throws MaltChainedException {
      return 1;
   }

   public ParserConfiguration getConfiguration() {
      return this.config;
   }

   public AbstractParserFactory getFactory() {
      return this.factory;
   }
}
