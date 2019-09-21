package org.maltparser.core.syntaxgraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.node.Token;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class Sentence extends SyntaxGraph implements TokenStructure {
   protected final ObjectPoolList<Token> terminalPool = new ObjectPoolList<Token>() {
      protected Token create() throws MaltChainedException {
         return new Token();
      }

      public void resetObject(Token o) throws MaltChainedException {
         o.clear();
      }
   };
   protected final SortedMap<Integer, Token> terminalNodes = new TreeMap();
   protected final HashMap<Integer, ArrayList<String>> comments = new HashMap();
   protected int sentenceID;

   public Sentence(SymbolTableHandler symbolTables) throws MaltChainedException {
      super(symbolTables);
   }

   public TokenNode addTokenNode(int index) throws MaltChainedException {
      return index > 0 ? this.getOrAddTerminalNode(index) : null;
   }

   public TokenNode addTokenNode() throws MaltChainedException {
      int index = this.getHighestTokenIndex();
      return index > 0 ? this.getOrAddTerminalNode(index + 1) : this.getOrAddTerminalNode(1);
   }

   public void addComment(String comment, int at_index) {
      ArrayList<String> commentList = (ArrayList)this.comments.get(at_index);
      if (commentList == null) {
         commentList = new ArrayList();
         this.comments.put(at_index, commentList);
      }

      commentList.add(comment);
   }

   public ArrayList<String> getComment(int at_index) {
      return (ArrayList)this.comments.get(at_index);
   }

   public boolean hasComments() {
      return this.comments.size() > 0;
   }

   public int nTokenNode() {
      return this.terminalNodes.size();
   }

   public boolean hasTokens() {
      return !this.terminalNodes.isEmpty();
   }

   protected Token getOrAddTerminalNode(int index) throws MaltChainedException {
      Token node = (Token)this.terminalNodes.get(index);
      if (node == null) {
         if (index > 0) {
            node = (Token)this.terminalPool.checkOut();
            node.setIndex(index);
            node.setBelongsToGraph(this);
            if (index > 1) {
               Token prev = (Token)this.terminalNodes.get(index - 1);
               if (prev == null) {
                  try {
                     prev = (Token)this.terminalNodes.get(this.terminalNodes.headMap(index).lastKey());
                  } catch (NoSuchElementException var7) {
                  }
               }

               if (prev != null) {
                  prev.setSuccessor(node);
                  node.setPredecessor(prev);
               }

               if ((Integer)this.terminalNodes.lastKey() > index) {
                  Token succ = (Token)this.terminalNodes.get(index + 1);
                  if (succ == null) {
                     try {
                        succ = (Token)this.terminalNodes.get(this.terminalNodes.tailMap(index).firstKey());
                     } catch (NoSuchElementException var6) {
                     }
                  }

                  if (succ != null) {
                     succ.setPredecessor(node);
                     node.setSuccessor(succ);
                  }
               }
            }
         }

         this.terminalNodes.put(index, node);
         ++this.numberOfComponents;
      }

      return node;
   }

   public SortedSet<Integer> getTokenIndices() {
      return new TreeSet(this.terminalNodes.keySet());
   }

   public int getHighestTokenIndex() {
      try {
         return (Integer)this.terminalNodes.lastKey();
      } catch (NoSuchElementException var2) {
         return 0;
      }
   }

   public TokenNode getTokenNode(int index) {
      return index > 0 ? (TokenNode)this.terminalNodes.get(index) : null;
   }

   public int getSentenceID() {
      return this.sentenceID;
   }

   public void setSentenceID(int sentenceID) {
      this.sentenceID = sentenceID;
   }

   public void clear() throws MaltChainedException {
      this.terminalPool.checkInAll();
      this.terminalNodes.clear();
      this.comments.clear();
      this.sentenceID = 0;
      super.clear();
   }

   public void update(Observable o, Object str) {
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.terminalNodes.keySet().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         sb.append(((Token)this.terminalNodes.get(index)).toString().trim());
         sb.append('\n');
      }

      sb.append("\n");
      return sb.toString();
   }
}
