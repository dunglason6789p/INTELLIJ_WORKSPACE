package org.maltparser.parser.algorithm.nivre;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.Oracle;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideUserAction;

public class ArcEagerOracle extends Oracle {
   public ArcEagerOracle(DependencyParserConfig manager, GuideUserHistory history) throws MaltChainedException {
      super(manager, history);
      this.setGuideName("ArcEager");
   }

   public GuideUserAction predict(DependencyStructure gold, ParserConfiguration config) throws MaltChainedException {
      NivreConfig nivreConfig = (NivreConfig)config;
      DependencyNode stackPeek = (DependencyNode)nivreConfig.getStack().peek();
      int stackPeekIndex = stackPeek.getIndex();
      int inputPeekIndex = ((DependencyNode)nivreConfig.getInput().peek()).getIndex();
      if (!stackPeek.isRoot() && gold.getTokenNode(stackPeekIndex).getHead().getIndex() == inputPeekIndex) {
         return this.updateActionContainers(4, gold.getTokenNode(stackPeekIndex).getHeadEdge().getLabelSet());
      } else if (gold.getTokenNode(inputPeekIndex).getHead().getIndex() == stackPeekIndex) {
         return this.updateActionContainers(3, gold.getTokenNode(inputPeekIndex).getHeadEdge().getLabelSet());
      } else if (!nivreConfig.isAllowReduce() && !stackPeek.hasHead()) {
         return this.updateActionContainers(1, (LabelSet)null);
      } else if (gold.getTokenNode(inputPeekIndex).hasLeftDependent() && gold.getTokenNode(inputPeekIndex).getLeftmostDependent().getIndex() < stackPeekIndex) {
         return this.updateActionContainers(2, (LabelSet)null);
      } else {
         return gold.getTokenNode(inputPeekIndex).getHead().getIndex() < stackPeekIndex && (!gold.getTokenNode(inputPeekIndex).getHead().isRoot() || nivreConfig.isAllowRoot()) ? this.updateActionContainers(2, (LabelSet)null) : this.updateActionContainers(1, (LabelSet)null);
      }
   }

   public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
   }

   public void terminate() throws MaltChainedException {
   }
}
