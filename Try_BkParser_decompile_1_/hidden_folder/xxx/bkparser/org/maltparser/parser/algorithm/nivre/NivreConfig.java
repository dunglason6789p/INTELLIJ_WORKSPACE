package org.maltparser.parser.algorithm.nivre;

import java.util.Stack;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParsingException;

public class NivreConfig extends ParserConfiguration {
   private final Stack<DependencyNode> stack = new Stack();
   private final Stack<DependencyNode> input = new Stack();
   private DependencyStructure dependencyGraph;
   private final boolean allowRoot;
   private final boolean allowReduce;
   private final boolean enforceTree;
   private boolean end;

   public NivreConfig(boolean allowRoot, boolean allowReduce, boolean enforceTree) throws MaltChainedException {
      this.allowRoot = allowRoot;
      this.allowReduce = allowReduce;
      this.enforceTree = enforceTree;
      this.end = false;
   }

   public Stack<DependencyNode> getStack() {
      return this.stack;
   }

   public Stack<DependencyNode> getInput() {
      return this.input;
   }

   public DependencyStructure getDependencyStructure() {
      return this.dependencyGraph;
   }

   public boolean isTerminalState() {
      if (!this.isEnforceTree()) {
         return this.input.isEmpty();
      } else {
         return this.input.isEmpty() && this.stack.size() == 1;
      }
   }

   public DependencyNode getStackNode(int index) throws MaltChainedException {
      if (index < 0) {
         throw new ParsingException("Stack index must be non-negative in feature specification. ");
      } else {
         return this.stack.size() - index > 0 ? (DependencyNode)this.stack.get(this.stack.size() - 1 - index) : null;
      }
   }

   public DependencyNode getInputNode(int index) throws MaltChainedException {
      if (index < 0) {
         throw new ParsingException("Input index must be non-negative in feature specification. ");
      } else {
         return this.input.size() - index > 0 ? (DependencyNode)this.input.get(this.input.size() - 1 - index) : null;
      }
   }

   public void setDependencyGraph(DependencyStructure source) throws MaltChainedException {
      this.dependencyGraph = source;
   }

   public DependencyStructure getDependencyGraph() {
      return this.dependencyGraph;
   }

   public void initialize(ParserConfiguration parserConfiguration) throws MaltChainedException {
      if (parserConfiguration != null) {
         NivreConfig nivreConfig = (NivreConfig)parserConfiguration;
         Stack<DependencyNode> sourceStack = nivreConfig.getStack();
         Stack<DependencyNode> sourceInput = nivreConfig.getInput();
         this.setDependencyGraph(nivreConfig.getDependencyGraph());
         int i = 0;

         int n;
         for(n = sourceStack.size(); i < n; ++i) {
            this.stack.add(this.dependencyGraph.getDependencyNode(((DependencyNode)sourceStack.get(i)).getIndex()));
         }

         i = 0;

         for(n = sourceInput.size(); i < n; ++i) {
            this.input.add(this.dependencyGraph.getDependencyNode(((DependencyNode)sourceInput.get(i)).getIndex()));
         }
      } else {
         this.stack.push(this.dependencyGraph.getDependencyRoot());

         for(int i = this.dependencyGraph.getHighestTokenIndex(); i > 0; --i) {
            DependencyNode node = this.dependencyGraph.getDependencyNode(i);
            if (node != null && !node.hasHead()) {
               this.input.push(node);
            }
         }
      }

   }

   public void initialize() throws MaltChainedException {
      this.stack.push(this.dependencyGraph.getDependencyRoot());

      for(int i = this.dependencyGraph.getHighestTokenIndex(); i > 0; --i) {
         DependencyNode node = this.dependencyGraph.getDependencyNode(i);
         if (node != null && !node.hasHead()) {
            this.input.push(node);
         }
      }

   }

   public void setEnd(boolean end) {
      this.end = end;
   }

   public boolean isEnd() {
      return this.end;
   }

   public boolean isAllowRoot() {
      return this.allowRoot;
   }

   public boolean isAllowReduce() {
      return this.allowReduce;
   }

   public boolean isEnforceTree() {
      return this.enforceTree;
   }

   public void clear() throws MaltChainedException {
      this.stack.clear();
      this.input.clear();
      this.historyNode = null;
      this.end = false;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         NivreConfig that = (NivreConfig)obj;
         if (this.stack.size() != that.getStack().size()) {
            return false;
         } else if (this.input.size() != that.getInput().size()) {
            return false;
         } else if (this.dependencyGraph.nEdges() != that.getDependencyGraph().nEdges()) {
            return false;
         } else {
            int i;
            for(i = 0; i < this.stack.size(); ++i) {
               if (((DependencyNode)this.stack.get(i)).getIndex() != ((DependencyNode)that.getStack().get(i)).getIndex()) {
                  return false;
               }
            }

            for(i = 0; i < this.input.size(); ++i) {
               if (((DependencyNode)this.input.get(i)).getIndex() != ((DependencyNode)that.getInput().get(i)).getIndex()) {
                  return false;
               }
            }

            return this.dependencyGraph.getEdges().equals(that.getDependencyGraph().getEdges());
         }
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.stack.size());
      sb.append(", ");
      sb.append(this.input.size());
      sb.append(", ");
      sb.append(this.dependencyGraph.nEdges());
      sb.append(", ");
      sb.append(this.allowRoot);
      sb.append(", ");
      sb.append(this.allowReduce);
      sb.append(", ");
      sb.append(this.enforceTree);
      return sb.toString();
   }
}
