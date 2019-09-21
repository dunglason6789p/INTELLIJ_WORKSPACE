package org.maltparser.core.syntaxgraph.writer;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class TabWriter implements SyntaxGraphWriter {
   private BufferedWriter writer;
   private DataFormatInstance dataFormatInstance;
   private final StringBuilder output = new StringBuilder();
   private boolean closeStream = true;
   private final char TAB = '\t';
   private final char NEWLINE = '\n';

   public TabWriter() {
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

   public void writeProlog() throws MaltChainedException {
   }

   public void writeComments(TokenStructure syntaxGraph, int at_index) throws MaltChainedException {
      ArrayList<String> commentList = syntaxGraph.getComment(at_index);
      if (commentList != null) {
         try {
            for(int i = 0; i < commentList.size(); ++i) {
               this.writer.write((String)commentList.get(i));
               this.writer.write(10);
            }
         } catch (IOException var5) {
            this.close();
            throw new DataFormatException("Could not write to the output file. ", var5);
         }
      }

   }

   public void writeSentence(TokenStructure syntaxGraph) throws MaltChainedException {
      if (syntaxGraph != null && this.dataFormatInstance != null && syntaxGraph.hasTokens()) {
         Iterator<ColumnDescription> columns = this.dataFormatInstance.iterator();
         SymbolTableHandler symbolTables = syntaxGraph.getSymbolTables();
         Iterator i$ = syntaxGraph.getTokenIndices().iterator();

         while(i$.hasNext()) {
            int i = (Integer)i$.next();
            this.writeComments(syntaxGraph, i);

            try {
               for(ColumnDescription column = null; columns.hasNext(); this.output.setLength(0)) {
                  column = (ColumnDescription)columns.next();
                  if (column.getCategory() == 1) {
                     TokenNode node = syntaxGraph.getTokenNode(i);
                     if (!column.getName().equals("ID")) {
                        if (node.hasLabel(symbolTables.getSymbolTable(column.getName()))) {
                           this.output.append(node.getLabelSymbol(symbolTables.getSymbolTable(column.getName())));
                           if (this.output.length() != 0) {
                              this.writer.write(this.output.toString());
                           } else {
                              this.writer.write(95);
                           }
                        } else {
                           this.writer.write(95);
                        }
                     } else {
                        this.writer.write(Integer.toString(i));
                     }
                  } else if (column.getCategory() == 2 && syntaxGraph instanceof DependencyStructure) {
                     if (((DependencyStructure)syntaxGraph).getDependencyNode(i).hasHead()) {
                        this.writer.write(Integer.toString(((DependencyStructure)syntaxGraph).getDependencyNode(i).getHead().getIndex()));
                     } else {
                        this.writer.write(Integer.toString(0));
                     }
                  } else if (column.getCategory() == 3 && syntaxGraph instanceof DependencyStructure) {
                     if (((DependencyStructure)syntaxGraph).getDependencyNode(i).hasHead() && ((DependencyStructure)syntaxGraph).getDependencyNode(i).hasHeadEdgeLabel(symbolTables.getSymbolTable(column.getName()))) {
                        this.output.append(((DependencyStructure)syntaxGraph).getDependencyNode(i).getHeadEdgeLabelSymbol(symbolTables.getSymbolTable(column.getName())));
                     } else {
                        this.output.append(((DependencyStructure)syntaxGraph).getDefaultRootEdgeLabelSymbol(symbolTables.getSymbolTable(column.getName())));
                     }

                     if (this.output.length() != 0) {
                        this.writer.write(this.output.toString());
                     }
                  } else {
                     this.writer.write(column.getDefaultOutput());
                  }

                  if (columns.hasNext()) {
                     this.writer.write(9);
                  }
               }

               this.writer.write(10);
               columns = this.dataFormatInstance.iterator();
            } catch (IOException var9) {
               this.close();
               throw new DataFormatException("Could not write to the output file. ", var9);
            }
         }

         this.writeComments(syntaxGraph, syntaxGraph.nTokenNode() + 1);

         try {
            this.writer.write(10);
            this.writer.flush();
         } catch (IOException var8) {
            this.close();
            throw new DataFormatException("Could not write to the output file. ", var8);
         }
      }
   }

   public void writeEpilog() throws MaltChainedException {
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
   }

   public String getOptions() {
      return null;
   }

   public void setOptions(String optionString) throws MaltChainedException {
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
