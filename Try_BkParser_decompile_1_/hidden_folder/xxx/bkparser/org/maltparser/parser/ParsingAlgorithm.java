package org.maltparser.parser;

import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.guide.ClassifierGuide;

public abstract class ParsingAlgorithm implements AlgoritmInterface {
   protected final DependencyParserConfig manager;
   protected final ParserRegistry registry;
   protected ClassifierGuide classifierGuide;
   protected final ParserState parserState;
   protected ParserConfiguration currentParserConfiguration;

   public ParsingAlgorithm(DependencyParserConfig _manager, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
      this.manager = _manager;
      this.registry = new ParserRegistry();
      this.registry.setSymbolTableHandler(symbolTableHandler);
      this.registry.setDataFormatInstance(this.manager.getDataFormatInstance());
      this.registry.setAbstractParserFeatureFactory(this.manager.getParserFactory());
      this.parserState = new ParserState(this.manager, symbolTableHandler, this.manager.getParserFactory());
   }

   public abstract void terminate() throws MaltChainedException;

   public ParserRegistry getParserRegistry() {
      return this.registry;
   }

   public ClassifierGuide getGuide() {
      return this.classifierGuide;
   }

   public void setGuide(ClassifierGuide guide) {
      this.classifierGuide = guide;
   }

   public ParserConfiguration getCurrentParserConfiguration() {
      return this.currentParserConfiguration;
   }

   protected void setCurrentParserConfiguration(ParserConfiguration currentParserConfiguration) {
      this.currentParserConfiguration = currentParserConfiguration;
   }

   public ParserState getParserState() {
      return this.parserState;
   }

   public DependencyParserConfig getManager() {
      return this.manager;
   }

   protected void copyEdges(DependencyStructure source, DependencyStructure target) throws MaltChainedException {
      Iterator i$ = source.getTokenIndices().iterator();

      while(true) {
         DependencyNode snode;
         do {
            if (!i$.hasNext()) {
               return;
            }

            int index = (Integer)i$.next();
            snode = source.getDependencyNode(index);
         } while(!snode.hasHead());

         Edge s = snode.getHeadEdge();
         Edge t = target.addDependencyEdge(s.getSource().getIndex(), s.getTarget().getIndex());
         Iterator i$ = s.getLabelTypes().iterator();

         while(i$.hasNext()) {
            SymbolTable table = (SymbolTable)i$.next();
            t.addLabel(table, s.getLabelSymbol(table));
         }
      }
   }

   protected void copyDynamicInput(DependencyStructure source, DependencyStructure target) throws MaltChainedException {
      Iterator i$ = source.getTokenIndices().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         DependencyNode snode = source.getDependencyNode(index);
         DependencyNode tnode = target.getDependencyNode(index);
         Iterator i$ = snode.getLabelTypes().iterator();

         while(i$.hasNext()) {
            SymbolTable table = (SymbolTable)i$.next();
            if (!tnode.hasLabel(table)) {
               tnode.addLabel(table, snode.getLabelSymbol(table));
            }
         }
      }

   }
}
