package org.maltparser.core.syntaxgraph.writer;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.regex.PatternSyntaxException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.PhraseStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class BracketWriter implements SyntaxGraphWriter {
   private BracketWriter.PennWriterFormat format;
   private BufferedWriter writer;
   private DataFormatInstance dataFormatInstance;
   private SortedMap<String, ColumnDescription> inputColumns;
   private SortedMap<String, ColumnDescription> edgeLabelColumns;
   private SortedMap<String, ColumnDescription> phraseLabelColumns;
   private char STARTING_BRACKET = '(';
   private String EMPTY_EDGELABEL = "??";
   private char CLOSING_BRACKET = ')';
   private char INPUT_SEPARATOR = ' ';
   private char EDGELABEL_SEPARATOR = '-';
   private char SENTENCE_SEPARATOR = '\n';
   private String optionString;
   private boolean closeStream = true;

   public BracketWriter() {
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
   }

   public void writeEpilog() throws MaltChainedException {
   }

   public void writeProlog() throws MaltChainedException {
   }

   public void writeSentence(TokenStructure syntaxGraph) throws MaltChainedException {
      if (syntaxGraph != null && this.dataFormatInstance != null) {
         if (syntaxGraph instanceof PhraseStructure && syntaxGraph.hasTokens()) {
            if (this.format == BracketWriter.PennWriterFormat.PRETTY) {
               this.writeElement(syntaxGraph.getSymbolTables(), ((PhraseStructure)syntaxGraph).getPhraseStructureRoot(), 0);
            } else {
               this.writeElement(syntaxGraph.getSymbolTables(), ((PhraseStructure)syntaxGraph).getPhraseStructureRoot());
            }

            try {
               this.writer.write(this.SENTENCE_SEPARATOR);
               this.writer.flush();
            } catch (IOException var3) {
               this.close();
               throw new DataFormatException("Could not write to the output file. ", var3);
            }
         }

      }
   }

   private void writeElement(SymbolTableHandler symbolTables, PhraseStructureNode element) throws MaltChainedException {
      try {
         SymbolTable table;
         int i;
         Iterator i$;
         String inputColumn;
         Iterator i$;
         String edgeLabelColumn;
         if (element instanceof TokenNode) {
            PhraseStructureNode t = element;
            table = null;
            this.writer.write(this.STARTING_BRACKET);
            i = 0;

            for(i$ = this.inputColumns.keySet().iterator(); i$.hasNext(); ++i) {
               inputColumn = (String)i$.next();
               if (i != 0) {
                  this.writer.write(this.INPUT_SEPARATOR);
               }

               table = symbolTables.getSymbolTable(((ColumnDescription)this.inputColumns.get(inputColumn)).getName());
               if (t.hasLabel(table)) {
                  this.writer.write(t.getLabelSymbol(table));
               }

               if (i == 0) {
                  i$ = this.edgeLabelColumns.keySet().iterator();

                  while(i$.hasNext()) {
                     edgeLabelColumn = (String)i$.next();
                     table = symbolTables.getSymbolTable(((ColumnDescription)this.edgeLabelColumns.get(edgeLabelColumn)).getName());
                     if (t.hasParentEdgeLabel(table) && !t.getParent().isRoot() && !t.getParentEdgeLabelSymbol(table).equals(this.EMPTY_EDGELABEL)) {
                        this.writer.write(this.EDGELABEL_SEPARATOR);
                        this.writer.write(t.getParentEdgeLabelSymbol(table));
                     }
                  }
               }
            }

            this.writer.write(this.CLOSING_BRACKET);
         } else {
            NonTerminalNode nt = (NonTerminalNode)element;
            this.writer.write(this.STARTING_BRACKET);
            table = null;
            i = 0;

            for(i$ = this.phraseLabelColumns.keySet().iterator(); i$.hasNext(); ++i) {
               inputColumn = (String)i$.next();
               if (i != 0) {
                  this.writer.write(this.INPUT_SEPARATOR);
               }

               table = symbolTables.getSymbolTable(((ColumnDescription)this.phraseLabelColumns.get(inputColumn)).getName());
               if (nt.hasLabel(table)) {
                  this.writer.write(nt.getLabelSymbol(table));
               }

               if (i == 0) {
                  i$ = this.edgeLabelColumns.keySet().iterator();

                  while(i$.hasNext()) {
                     edgeLabelColumn = (String)i$.next();
                     table = symbolTables.getSymbolTable(((ColumnDescription)this.edgeLabelColumns.get(edgeLabelColumn)).getName());
                     if (nt.hasParentEdgeLabel(table) && !nt.getParent().isRoot() && !nt.getParentEdgeLabelSymbol(table).equals(this.EMPTY_EDGELABEL)) {
                        this.writer.write(this.EDGELABEL_SEPARATOR);
                        this.writer.write(nt.getParentEdgeLabelSymbol(table));
                     }
                  }
               }
            }

            i$ = ((NonTerminalNode)element).getChildren().iterator();

            while(i$.hasNext()) {
               PhraseStructureNode node = (PhraseStructureNode)i$.next();
               this.writeElement(symbolTables, node);
            }

            this.writer.write(this.CLOSING_BRACKET);
         }

      } catch (IOException var10) {
         throw new DataFormatException("Could not write to the output file. ", var10);
      }
   }

   private String getIndentation(int depth) {
      StringBuilder sb = new StringBuilder("");

      for(int i = 0; i < depth; ++i) {
         sb.append("\t");
      }

      return sb.toString();
   }

   private void writeElement(SymbolTableHandler symbolTables, PhraseStructureNode element, int depth) throws MaltChainedException {
      try {
         SymbolTable table;
         int i;
         Iterator i$;
         String inputColumn;
         Iterator i$;
         String edgeLabelColumn;
         if (element instanceof TokenNode) {
            PhraseStructureNode t = element;
            table = null;
            this.writer.write("\n" + this.getIndentation(depth) + this.STARTING_BRACKET);
            i = 0;

            for(i$ = this.inputColumns.keySet().iterator(); i$.hasNext(); ++i) {
               inputColumn = (String)i$.next();
               if (i != 0) {
                  this.writer.write(this.INPUT_SEPARATOR);
               }

               table = symbolTables.getSymbolTable(((ColumnDescription)this.inputColumns.get(inputColumn)).getName());
               if (t.hasLabel(table)) {
                  this.writer.write(this.encodeString(t.getLabelSymbol(table)));
               }

               if (i == 0) {
                  i$ = this.edgeLabelColumns.keySet().iterator();

                  while(i$.hasNext()) {
                     edgeLabelColumn = (String)i$.next();
                     table = symbolTables.getSymbolTable(((ColumnDescription)this.edgeLabelColumns.get(edgeLabelColumn)).getName());
                     if (t.hasParentEdgeLabel(table) && !t.getParent().isRoot() && !t.getParentEdgeLabelSymbol(table).equals(this.EMPTY_EDGELABEL)) {
                        this.writer.write(this.EDGELABEL_SEPARATOR);
                        this.writer.write(t.getParentEdgeLabelSymbol(table));
                     }
                  }
               }
            }

            this.writer.write(this.CLOSING_BRACKET);
         } else {
            NonTerminalNode nt = (NonTerminalNode)element;
            this.writer.write("\n" + this.getIndentation(depth) + this.STARTING_BRACKET);
            table = null;
            i = 0;

            for(i$ = this.phraseLabelColumns.keySet().iterator(); i$.hasNext(); ++i) {
               inputColumn = (String)i$.next();
               if (i != 0) {
                  this.writer.write(this.INPUT_SEPARATOR);
               }

               table = symbolTables.getSymbolTable(((ColumnDescription)this.phraseLabelColumns.get(inputColumn)).getName());
               if (nt.hasLabel(table)) {
                  this.writer.write(nt.getLabelSymbol(table));
               }

               if (i == 0) {
                  i$ = this.edgeLabelColumns.keySet().iterator();

                  while(i$.hasNext()) {
                     edgeLabelColumn = (String)i$.next();
                     table = symbolTables.getSymbolTable(((ColumnDescription)this.edgeLabelColumns.get(edgeLabelColumn)).getName());
                     if (nt.hasParentEdgeLabel(table) && !nt.getParent().isRoot() && !nt.getParentEdgeLabelSymbol(table).equals(this.EMPTY_EDGELABEL)) {
                        this.writer.write(this.EDGELABEL_SEPARATOR);
                        this.writer.write(nt.getParentEdgeLabelSymbol(table));
                     }
                  }
               }
            }

            i$ = ((NonTerminalNode)element).getChildren().iterator();

            while(i$.hasNext()) {
               PhraseStructureNode node = (PhraseStructureNode)i$.next();
               this.writeElement(symbolTables, node, depth + 1);
            }

            this.writer.write("\n" + this.getIndentation(depth) + this.CLOSING_BRACKET);
         }

      } catch (IOException var11) {
         throw new DataFormatException("Could not write to the output file. ", var11);
      }
   }

   public BufferedWriter getWriter() {
      return this.writer;
   }

   public void setWriter(BufferedWriter writer) throws MaltChainedException {
      this.close();
      this.writer = writer;
   }

   public DataFormatInstance getDataFormatInstance() {
      return this.dataFormatInstance;
   }

   public void setDataFormatInstance(DataFormatInstance dataFormatInstance) {
      this.dataFormatInstance = dataFormatInstance;
      this.inputColumns = dataFormatInstance.getInputColumnDescriptions();
      this.edgeLabelColumns = dataFormatInstance.getPhraseStructureEdgeLabelColumnDescriptions();
      this.phraseLabelColumns = dataFormatInstance.getPhraseStructureNodeLabelColumnDescriptions();
   }

   public String getOptions() {
      return this.optionString;
   }

   public void setOptions(String optionString) throws MaltChainedException {
      this.optionString = optionString;
      this.format = BracketWriter.PennWriterFormat.DEFAULT;

      String[] argv;
      try {
         argv = optionString.split("[_\\p{Blank}]");
      } catch (PatternSyntaxException var4) {
         throw new DataFormatException("Could not split the bracket writer option '" + optionString + "'. ", var4);
      }

      for(int i = 0; i < argv.length - 1; ++i) {
         if (argv[i].charAt(0) != '-') {
            throw new DataFormatException("The argument flag should start with the following character '-', not with " + argv[i].charAt(0));
         }

         ++i;
         if (i >= argv.length) {
            throw new DataFormatException("The last argument does not have any value. ");
         }

         switch(argv[i - 1].charAt(1)) {
         case 'f':
            if (argv[i].equals("p")) {
               this.format = BracketWriter.PennWriterFormat.PRETTY;
            } else if (argv[i].equals("p")) {
               this.format = BracketWriter.PennWriterFormat.DEFAULT;
            }
            break;
         default:
            throw new DataFormatException("Unknown bracket writer option: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
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

   private String encodeString(String string) {
      return string.replace("(", "-LRB-").replace(")", "-RRB-").replace("[", "-LSB-").replace("]", "-RSB-").replace("{", "-LCB-").replace("}", "-RCB-");
   }

   private static enum PennWriterFormat {
      DEFAULT,
      PRETTY;

      private PennWriterFormat() {
      }
   }
}
