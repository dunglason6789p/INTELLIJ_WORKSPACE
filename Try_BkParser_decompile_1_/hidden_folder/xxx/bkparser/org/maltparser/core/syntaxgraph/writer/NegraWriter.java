package org.maltparser.core.syntaxgraph.writer;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.PatternSyntaxException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.PhraseStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;

public class NegraWriter implements SyntaxGraphWriter {
   private BufferedWriter writer;
   private DataFormatInstance dataFormatInstance;
   private String optionString;
   private int sentenceCount;
   private LinkedHashMap<Integer, Integer> nonTerminalIndexMap = new LinkedHashMap();
   private int START_ID_OF_NONTERMINALS = 500;
   private boolean closeStream = true;

   public NegraWriter() {
   }

   public void open(String fileName, String charsetName) throws MaltChainedException {
      try {
         this.open(new OutputStreamWriter(new FileOutputStream(fileName), charsetName));
      } catch (FileNotFoundException var4) {
         throw new DataFormatException("The output file '" + fileName + "' cannot be found.", var4);
      } catch (UnsupportedEncodingException var5) {
         throw new DataFormatException("The character encoding set '" + charsetName + "' isn't supported.", var5);
      }
   }

   public void open(OutputStream os, String charsetName) throws MaltChainedException {
      try {
         if (os == System.out || os == System.err) {
            this.closeStream = false;
         }

         this.open(new OutputStreamWriter(os, charsetName));
      } catch (UnsupportedEncodingException var4) {
         throw new DataFormatException("The character encoding set '" + charsetName + "' isn't supported.", var4);
      }
   }

   private void open(OutputStreamWriter osw) throws MaltChainedException {
      this.setWriter(new BufferedWriter(osw));
      this.setSentenceCount(0);
   }

   public void writeProlog() throws MaltChainedException {
   }

   public void writeSentence(TokenStructure syntaxGraph) throws MaltChainedException {
      if (syntaxGraph != null && this.dataFormatInstance != null && syntaxGraph instanceof PhraseStructure && syntaxGraph.hasTokens()) {
         PhraseStructure phraseStructure = (PhraseStructure)syntaxGraph;
         ++this.sentenceCount;

         try {
            this.writer.write("#BOS ");
            if (phraseStructure.getSentenceID() != 0) {
               this.writer.write(Integer.toString(phraseStructure.getSentenceID()));
            } else {
               this.writer.write(Integer.toString(this.sentenceCount));
            }

            this.writer.write(10);
            if (phraseStructure.hasNonTerminals()) {
               this.calculateIndices(phraseStructure);
               this.writeTerminals(phraseStructure);
               this.writeNonTerminals(phraseStructure);
            } else {
               this.writeTerminals(phraseStructure);
            }

            this.writer.write("#EOS ");
            if (phraseStructure.getSentenceID() != 0) {
               this.writer.write(Integer.toString(phraseStructure.getSentenceID()));
            } else {
               this.writer.write(Integer.toString(this.sentenceCount));
            }

            this.writer.write(10);
         } catch (IOException var4) {
            throw new DataFormatException("Could not write to the output file. ", var4);
         }
      }
   }

   public void writeEpilog() throws MaltChainedException {
   }

   private void calculateIndices(PhraseStructure phraseStructure) throws MaltChainedException {
      SortedMap<Integer, Integer> heights = new TreeMap();
      Iterator i$ = phraseStructure.getNonTerminalIndices().iterator();

      int h;
      while(i$.hasNext()) {
         h = (Integer)i$.next();
         heights.put(h, ((NonTerminalNode)phraseStructure.getNonTerminalNode(h)).getHeight());
      }

      boolean done = false;
      h = 1;
      int ntid = this.START_ID_OF_NONTERMINALS;
      this.nonTerminalIndexMap.clear();

      while(!done) {
         done = true;
         Iterator i$ = phraseStructure.getNonTerminalIndices().iterator();

         while(i$.hasNext()) {
            int index = (Integer)i$.next();
            if ((Integer)heights.get(index) == h) {
               NonTerminalNode nt = (NonTerminalNode)phraseStructure.getNonTerminalNode(index);
               this.nonTerminalIndexMap.put(nt.getIndex(), ntid++);
               done = false;
            }
         }

         ++h;
      }

   }

   private void writeTerminals(PhraseStructure phraseStructure) throws MaltChainedException {
      try {
         SymbolTableHandler symbolTables = phraseStructure.getSymbolTables();
         Iterator i$ = phraseStructure.getTokenIndices().iterator();

         while(i$.hasNext()) {
            int index = (Integer)i$.next();
            PhraseStructureNode terminal = phraseStructure.getTokenNode(index);
            Iterator<ColumnDescription> columns = this.dataFormatInstance.iterator();
            ColumnDescription column = null;
            int ti = 1;

            while(true) {
               SymbolTable table;
               while(columns.hasNext()) {
                  column = (ColumnDescription)columns.next();
                  if (column.getCategory() == 1) {
                     table = symbolTables.getSymbolTable(column.getName());
                     this.writer.write(terminal.getLabelSymbol(table));
                     int nTabs = 1;
                     if (ti != 1 && ti != 2) {
                        if (ti == 3) {
                           nTabs = 1;
                        } else if (ti == 4) {
                           nTabs = 2 - terminal.getLabelSymbol(table).length() / 8;
                        }
                     } else {
                        nTabs = 3 - terminal.getLabelSymbol(table).length() / 8;
                     }

                     if (nTabs < 1) {
                        nTabs = 1;
                     }

                     for(int j = 0; j < nTabs; ++j) {
                        this.writer.write(9);
                     }

                     ++ti;
                  } else if (column.getCategory() == 4) {
                     table = symbolTables.getSymbolTable(column.getName());
                     if (terminal.getParent() != null && terminal.hasParentEdgeLabel(table)) {
                        this.writer.write(terminal.getParentEdgeLabelSymbol(table));
                        this.writer.write(9);
                     } else {
                        this.writer.write("--\t");
                     }
                  } else if (column.getCategory() == 5) {
                     if (terminal.getParent() != null && terminal.getParent() != phraseStructure.getPhraseStructureRoot()) {
                        this.writer.write(Integer.toString((Integer)this.nonTerminalIndexMap.get(terminal.getParent().getIndex())));
                     } else {
                        this.writer.write(48);
                     }
                  }
               }

               table = symbolTables.getSymbolTable(column.getName());
               Iterator i$ = terminal.getIncomingSecondaryEdges().iterator();

               while(i$.hasNext()) {
                  Edge e = (Edge)i$.next();
                  if (e.hasLabel(table)) {
                     this.writer.write(9);
                     this.writer.write(e.getLabelSymbol(table));
                     this.writer.write(9);
                     if (e.getSource() instanceof NonTerminalNode) {
                        this.writer.write(Integer.toString((Integer)this.nonTerminalIndexMap.get(e.getSource().getIndex())));
                     } else {
                        this.writer.write(Integer.toString(e.getSource().getIndex()));
                     }
                  }
               }

               this.writer.write("\n");
               break;
            }
         }

      } catch (IOException var12) {
         throw new DataFormatException("The Negra writer is not able to write. ", var12);
      }
   }

   private void writeNonTerminals(PhraseStructure phraseStructure) throws MaltChainedException {
      SymbolTableHandler symbolTables = phraseStructure.getSymbolTables();
      Iterator i$ = this.nonTerminalIndexMap.keySet().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         NonTerminalNode nonTerminal = (NonTerminalNode)phraseStructure.getNonTerminalNode(index);
         if (nonTerminal == null || nonTerminal.isRoot()) {
            return;
         }

         try {
            this.writer.write(35);
            this.writer.write(Integer.toString((Integer)this.nonTerminalIndexMap.get(index)));
            this.writer.write("\t\t\t--\t\t\t");
            if (nonTerminal.hasLabel(symbolTables.getSymbolTable("CAT"))) {
               this.writer.write(nonTerminal.getLabelSymbol(symbolTables.getSymbolTable("CAT")));
            } else {
               this.writer.write("--");
            }

            this.writer.write("\t--\t\t");
            if (nonTerminal.hasParentEdgeLabel(symbolTables.getSymbolTable("LABEL"))) {
               this.writer.write(nonTerminal.getParentEdgeLabelSymbol(symbolTables.getSymbolTable("LABEL")));
            } else {
               this.writer.write("--");
            }

            this.writer.write(9);
            if (nonTerminal.getParent() != null && !nonTerminal.getParent().isRoot()) {
               this.writer.write(Integer.toString((Integer)this.nonTerminalIndexMap.get(nonTerminal.getParent().getIndex())));
            } else {
               this.writer.write(48);
            }

            Iterator i$ = nonTerminal.getIncomingSecondaryEdges().iterator();

            while(i$.hasNext()) {
               Edge e = (Edge)i$.next();
               if (e.hasLabel(symbolTables.getSymbolTable("SECEDGELABEL"))) {
                  this.writer.write(9);
                  this.writer.write(e.getLabelSymbol(symbolTables.getSymbolTable("SECEDGELABEL")));
                  this.writer.write(9);
                  if (e.getSource() instanceof NonTerminalNode) {
                     this.writer.write(Integer.toString((Integer)this.nonTerminalIndexMap.get(e.getSource().getIndex())));
                  } else {
                     this.writer.write(Integer.toString(e.getSource().getIndex()));
                  }
               }
            }

            this.writer.write("\n");
         } catch (IOException var8) {
            throw new DataFormatException("The Negra writer is not able to write the non-terminals. ", var8);
         }
      }

   }

   public BufferedWriter getWriter() {
      return this.writer;
   }

   public void setWriter(BufferedWriter writer) {
      this.writer = writer;
   }

   public int getSentenceCount() {
      return this.sentenceCount;
   }

   public void setSentenceCount(int sentenceCount) {
      this.sentenceCount = sentenceCount;
   }

   public DataFormatInstance getDataFormatInstance() {
      return this.dataFormatInstance;
   }

   public void setDataFormatInstance(DataFormatInstance dataFormatInstance) {
      this.dataFormatInstance = dataFormatInstance;
   }

   public String getOptions() {
      return this.optionString;
   }

   public void setOptions(String optionString) throws MaltChainedException {
      this.optionString = optionString;

      String[] argv;
      try {
         argv = optionString.split("[_\\p{Blank}]");
      } catch (PatternSyntaxException var6) {
         throw new DataFormatException("Could not split the penn writer option '" + optionString + "'. ", var6);
      }

      int i = 0;

      while(i < argv.length - 1) {
         if (argv[i].charAt(0) != '-') {
            throw new DataFormatException("The argument flag should start with the following character '-', not with " + argv[i].charAt(0));
         }

         ++i;
         if (i >= argv.length) {
            throw new DataFormatException("The last argument does not have any value. ");
         }

         switch(argv[i - 1].charAt(1)) {
         case 's':
            try {
               this.START_ID_OF_NONTERMINALS = Integer.parseInt(argv[i]);
            } catch (NumberFormatException var5) {
               throw new MaltChainedException("The TigerXML Reader option -s must be an integer value. ");
            }

            ++i;
            break;
         default:
            throw new DataFormatException("Unknown svm parameter: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
         }
      }

   }

   public void close() throws MaltChainedException {
      try {
         if (this.writer != null) {
            this.writer.flush();
            if (this.closeStream) {
               this.writer.close();
            }

            this.writer = null;
         }

      } catch (IOException var2) {
         throw new DataFormatException("Could not close the output file. ", var2);
      }
   }
}
