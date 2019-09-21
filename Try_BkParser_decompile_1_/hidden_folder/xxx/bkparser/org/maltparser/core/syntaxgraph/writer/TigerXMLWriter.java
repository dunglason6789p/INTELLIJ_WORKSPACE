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
import java.util.TreeMap;
import java.util.regex.PatternSyntaxException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.Util;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.PhraseStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;
import org.maltparser.core.syntaxgraph.reader.TigerXMLHeader;

public class TigerXMLWriter implements SyntaxGraphWriter {
   private BufferedWriter writer;
   private DataFormatInstance dataFormatInstance;
   private String optionString;
   private int sentenceCount;
   private TigerXMLHeader header;
   private TigerXMLWriter.RootHandling rootHandling;
   private String sentencePrefix = "s";
   private StringBuilder sentenceID = new StringBuilder();
   private StringBuilder tmpID = new StringBuilder();
   private StringBuilder rootID = new StringBuilder();
   private int START_ID_OF_NONTERMINALS = 500;
   private boolean labeledTerminalID = false;
   private String VROOT_SYMBOL = "VROOT";
   private boolean useVROOT = false;
   private boolean closeStream = true;

   public TigerXMLWriter() {
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
      this.writeHeader();
   }

   public void writeSentence(TokenStructure syntaxGraph) throws MaltChainedException {
      if (syntaxGraph != null && this.dataFormatInstance != null) {
         if (syntaxGraph.hasTokens()) {
            ++this.sentenceCount;
            PhraseStructure phraseStructure = (PhraseStructure)syntaxGraph;

            try {
               this.sentenceID.setLength(0);
               this.sentenceID.append(this.sentencePrefix);
               if (phraseStructure.getSentenceID() != 0) {
                  this.sentenceID.append(Integer.toString(phraseStructure.getSentenceID()));
               } else {
                  this.sentenceID.append(Integer.toString(this.sentenceCount));
               }

               this.writer.write("    <s id=\"");
               this.writer.write(this.sentenceID.toString());
               this.writer.write("\">\n");
               this.setRootID(phraseStructure);
               this.writer.write("      <graph root=\"");
               this.writer.write(this.rootID.toString());
               this.writer.write("\" ");
               this.writer.write("discontinuous=\"");
               this.writer.write(Boolean.toString(!phraseStructure.isContinuous()));
               this.writer.write("\">\n");
               this.writeTerminals(phraseStructure);
               if (phraseStructure.nTokenNode() == 1 && !this.rootHandling.equals(TigerXMLWriter.RootHandling.TALBANKEN)) {
                  this.writer.write("        <nonterminals/>\n");
               } else {
                  this.writeNonTerminals(phraseStructure);
               }

               this.writer.write("      </graph>\n");
               this.writer.write("    </s>\n");
            } catch (IOException var4) {
               throw new DataFormatException("The TigerXML writer could not write to file. ", var4);
            }
         }

      }
   }

   private void setRootID(PhraseStructure phraseStructure) throws MaltChainedException {
      this.useVROOT = false;
      PhraseStructureNode root = phraseStructure.getPhraseStructureRoot();
      SymbolTableHandler symbolTables = phraseStructure.getSymbolTables();
      Iterator i$ = this.dataFormatInstance.getPhraseStructureNodeLabelColumnDescriptionSet().iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();
         if (root.hasLabel(symbolTables.getSymbolTable(column.getName())) && root.getLabelSymbol(symbolTables.getSymbolTable(column.getName())).equals(this.VROOT_SYMBOL)) {
            this.useVROOT = true;
            break;
         }
      }

      if (this.useVROOT) {
         this.rootID.setLength(0);
         this.rootID.append(this.sentenceID);
         this.rootID.append('_');
         this.rootID.append(this.VROOT_SYMBOL);
      } else if (phraseStructure.nTokenNode() == 1 && phraseStructure.nNonTerminals() == 0 && !root.isLabeled()) {
         this.rootID.setLength(0);
         this.rootID.append(this.sentenceID);
         this.rootID.append("_1");
      } else {
         this.rootID.setLength(0);
         this.rootID.append(this.sentenceID);
         this.rootID.append('_');
         this.rootID.append(Integer.toString(this.START_ID_OF_NONTERMINALS + phraseStructure.nNonTerminals()));
      }

   }

   public void writeEpilog() throws MaltChainedException {
      this.writeTail();
   }

   public BufferedWriter getWriter() {
      return this.writer;
   }

   public void setWriter(BufferedWriter writer) {
      this.writer = writer;
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

   private void writeHeader() throws MaltChainedException {
      try {
         if (this.header == null) {
            this.header = new TigerXMLHeader();
         }

         this.writer.write(this.header.toTigerXML());
      } catch (IOException var2) {
         throw new DataFormatException("The TigerXML writer could not write to file. ", var2);
      }
   }

   private void writeTerminals(PhraseStructure phraseStructure) throws MaltChainedException {
      try {
         this.writer.write("        <terminals>\n");
         Iterator i$ = phraseStructure.getTokenIndices().iterator();

         while(i$.hasNext()) {
            int index = (Integer)i$.next();
            PhraseStructureNode t = phraseStructure.getTokenNode(index);
            this.writer.write("          <t ");
            if (!this.labeledTerminalID) {
               this.tmpID.setLength(0);
               this.tmpID.append(this.sentenceID);
               this.tmpID.append('_');
               this.tmpID.append(Integer.toString(t.getIndex()));
               this.writer.write("id=\"");
               this.writer.write(this.tmpID.toString());
               this.writer.write("\" ");
            }

            Iterator i$ = this.dataFormatInstance.getInputColumnDescriptionSet().iterator();

            while(i$.hasNext()) {
               ColumnDescription column = (ColumnDescription)i$.next();
               this.writer.write(column.getName().toLowerCase());
               this.writer.write("=\"");
               this.writer.write(Util.xmlEscape(t.getLabelSymbol(phraseStructure.getSymbolTables().getSymbolTable(column.getName()))));
               this.writer.write("\" ");
            }

            this.writer.write("/>\n");
         }

         this.writer.write("        </terminals>\n");
      } catch (IOException var7) {
         throw new DataFormatException("The TigerXML writer is not able to write. ", var7);
      }
   }

   public void writeNonTerminals(PhraseStructure phraseStructure) throws MaltChainedException {
      try {
         SortedMap<Integer, Integer> heights = new TreeMap();
         Iterator i$ = phraseStructure.getNonTerminalIndices().iterator();

         int h;
         while(i$.hasNext()) {
            h = (Integer)i$.next();
            heights.put(h, ((NonTerminalNode)phraseStructure.getNonTerminalNode(h)).getHeight());
         }

         this.writer.write("        <nonterminals>\n");
         boolean done = false;

         for(h = 1; !done; ++h) {
            done = true;
            Iterator i$ = phraseStructure.getNonTerminalIndices().iterator();

            while(i$.hasNext()) {
               int index = (Integer)i$.next();
               if ((Integer)heights.get(index) == h) {
                  NonTerminalNode nt = (NonTerminalNode)phraseStructure.getNonTerminalNode(index);
                  this.tmpID.setLength(0);
                  this.tmpID.append(this.sentenceID);
                  this.tmpID.append('_');
                  this.tmpID.append(Integer.toString(nt.getIndex() + this.START_ID_OF_NONTERMINALS - 1));
                  this.writeNonTerminal(phraseStructure.getSymbolTables(), nt, this.tmpID.toString());
                  done = false;
               }
            }
         }

         this.writeNonTerminal(phraseStructure.getSymbolTables(), (NonTerminalNode)phraseStructure.getPhraseStructureRoot(), this.rootID.toString());
         this.writer.write("        </nonterminals>\n");
      } catch (IOException var8) {
         throw new DataFormatException("The TigerXML writer is not able to write. ", var8);
      }
   }

   public void writeNonTerminal(SymbolTableHandler symbolTables, NonTerminalNode nt, String id) throws MaltChainedException {
      try {
         this.writer.write("          <nt");
         this.writer.write(" id=\"");
         this.writer.write(id);
         this.writer.write("\" ");
         Iterator i$ = this.dataFormatInstance.getPhraseStructureNodeLabelColumnDescriptionSet().iterator();

         while(i$.hasNext()) {
            ColumnDescription column = (ColumnDescription)i$.next();
            if (nt.hasLabel(symbolTables.getSymbolTable(column.getName()))) {
               this.writer.write(column.getName().toLowerCase());
               this.writer.write("=");
               this.writer.write("\"");
               this.writer.write(Util.xmlEscape(nt.getLabelSymbol(symbolTables.getSymbolTable(column.getName()))));
               this.writer.write("\" ");
            }
         }

         this.writer.write(">\n");
         int i = 0;

         for(int n = nt.nChildren(); i < n; ++i) {
            PhraseStructureNode child = nt.getChild(i);
            this.writer.write("            <edge ");
            Iterator i$ = this.dataFormatInstance.getPhraseStructureEdgeLabelColumnDescriptionSet().iterator();

            while(i$.hasNext()) {
               ColumnDescription column = (ColumnDescription)i$.next();
               if (child.hasParentEdgeLabel(symbolTables.getSymbolTable(column.getName()))) {
                  this.writer.write(column.getName().toLowerCase());
                  this.writer.write("=\"");
                  this.writer.write(Util.xmlEscape(child.getParentEdgeLabelSymbol(symbolTables.getSymbolTable(column.getName()))));
                  this.writer.write("\" ");
               }
            }

            if (child instanceof TokenNode) {
               if (!this.labeledTerminalID) {
                  this.tmpID.setLength(0);
                  this.tmpID.append(this.sentenceID);
                  this.tmpID.append('_');
                  this.tmpID.append(Integer.toString(child.getIndex()));
                  this.writer.write(" idref=\"");
                  this.writer.write(this.tmpID.toString());
                  this.writer.write("\"");
               } else {
                  this.writer.write(" idref=\"");
                  this.writer.write(child.getLabelSymbol(symbolTables.getSymbolTable("ID")));
                  this.writer.write("\"");
               }
            } else {
               this.tmpID.setLength(0);
               this.tmpID.append(this.sentenceID);
               this.tmpID.append('_');
               this.tmpID.append(Integer.toString(child.getIndex() + this.START_ID_OF_NONTERMINALS - 1));
               this.writer.write(" idref=\"");
               this.writer.write(this.tmpID.toString());
               this.writer.write("\"");
            }

            this.writer.write(" />\n");
         }

         this.writer.write("          </nt>\n");
      } catch (IOException var9) {
         throw new DataFormatException("The TigerXML writer is not able to write. ", var9);
      }
   }

   private void writeTail() throws MaltChainedException {
      try {
         this.writer.write("  </body>\n");
         this.writer.write("</corpus>\n");
         this.writer.flush();
      } catch (IOException var2) {
         throw new DataFormatException("The TigerXML writer is not able to write. ", var2);
      }
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
      this.labeledTerminalID = dataFormatInstance.getInputColumnDescriptions().containsKey("id") || dataFormatInstance.getInputColumnDescriptions().containsKey("ID");
   }

   public String getOptions() {
      return this.optionString;
   }

   public void setOptions(String optionString) throws MaltChainedException {
      this.optionString = optionString;
      this.rootHandling = TigerXMLWriter.RootHandling.NORMAL;

      String[] argv;
      try {
         argv = optionString.split("[_\\p{Blank}]");
      } catch (PatternSyntaxException var6) {
         throw new DataFormatException("Could not split the TigerXML writer option '" + optionString + "'. ", var6);
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
         case 'r':
            if (argv[i].equals("n")) {
               this.rootHandling = TigerXMLWriter.RootHandling.NORMAL;
            } else if (argv[i].equals("tal")) {
               this.rootHandling = TigerXMLWriter.RootHandling.TALBANKEN;
            }
            break;
         case 's':
            try {
               this.START_ID_OF_NONTERMINALS = Integer.parseInt(argv[i]);
               break;
            } catch (NumberFormatException var5) {
               throw new MaltChainedException("The TigerXML writer option -s must be an integer value. ");
            }
         case 't':
         case 'u':
         default:
            throw new DataFormatException("Unknown TigerXML writer option: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
         case 'v':
            this.VROOT_SYMBOL = argv[i];
         }
      }

   }

   private static enum RootHandling {
      TALBANKEN,
      NORMAL;

      private RootHandling() {
      }
   }
}
