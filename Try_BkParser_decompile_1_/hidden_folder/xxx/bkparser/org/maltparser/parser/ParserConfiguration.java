package org.maltparser.parser;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.history.HistoryNode;

public abstract class ParserConfiguration {
   protected HistoryNode historyNode;

   public ParserConfiguration() {
      this.setHistoryNode((HistoryNode)null);
   }

   public HistoryNode getHistoryNode() {
      return this.historyNode;
   }

   public void setHistoryNode(HistoryNode historyNode) {
      this.historyNode = historyNode;
   }

   public abstract void setDependencyGraph(DependencyStructure var1) throws MaltChainedException;

   public abstract boolean isTerminalState() throws MaltChainedException;

   public abstract DependencyStructure getDependencyGraph();

   public abstract void clear() throws MaltChainedException;

   public abstract void initialize() throws MaltChainedException;
}
