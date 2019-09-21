package org.maltparser.core.syntaxgraph.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;

public class TabReader implements SyntaxGraphReader {
   private BufferedReader reader;
   private int sentenceCount;
   private DataFormatInstance dataFormatInstance;
   private static final String IGNORE_COLUMN_SIGN = "_";
   private String fileName = null;
   private URL url = null;
   private String charsetName;
   private int nIterations = 1;
   private int cIterations = 1;
   private boolean closeStream = true;

   public TabReader() {
   }

   public void open(String fileName, String charsetName) throws MaltChainedException {
      this.setFileName(fileName);
      this.setCharsetName(charsetName);

      try {
         this.open((InputStream)(new FileInputStream(fileName)), charsetName);
      } catch (FileNotFoundException var4) {
         throw new DataFormatException("The input file '" + fileName + "' cannot be found. ", var4);
      }
   }

   public void open(URL url, String charsetName) throws MaltChainedException {
      this.setUrl(url);
      this.setCharsetName(charsetName);
      if (url == null) {
         throw new DataFormatException("The input file cannot be found. ");
      } else {
         try {
            this.open(url.openStream(), charsetName);
         } catch (IOException var4) {
            throw new DataFormatException("The URL '" + url.toString() + "' cannot be opened. ", var4);
         }
      }
   }

   public void open(InputStream is, String charsetName) throws MaltChainedException {
      try {
         if (is == System.in) {
            this.closeStream = false;
         }

         this.open(new InputStreamReader(is, charsetName));
      } catch (UnsupportedEncodingException var4) {
         throw new DataFormatException("The character encoding set '" + charsetName + "' isn't supported. ", var4);
      }
   }

   private void open(InputStreamReader isr) throws MaltChainedException {
      this.setReader(new BufferedReader(isr));
      this.setSentenceCount(0);
   }

   public void readProlog() throws MaltChainedException {
   }

   public boolean readSentence(TokenStructure syntaxGraph) throws MaltChainedException {
      if (syntaxGraph != null && this.dataFormatInstance != null) {
         syntaxGraph.clear();
         syntaxGraph.getSymbolTables().cleanUp();
         Element node = null;
         Edge edge = null;
         ArrayList tokens = new ArrayList();

         String line;
         try {
            while((line = this.reader.readLine()) != null && line.trim().length() != 0) {
               tokens.add(line.trim());
            }
         } catch (IOException var12) {
            this.close();
            throw new DataFormatException("Error when reading from the input file. ", var12);
         }

         int terminalCounter = 0;

         for(int i = 0; i < tokens.size(); ++i) {
            String token = (String)tokens.get(i);
            if (token.charAt(0) == '#') {
               syntaxGraph.addComment(token, terminalCounter + 1);
            } else {
               String[] columns = token.split("\t");
               if (!columns[0].contains("-") && !columns[0].contains(".")) {
                  ++terminalCounter;
                  node = syntaxGraph.addTokenNode(terminalCounter);
                  Iterator<ColumnDescription> columnDescriptions = this.dataFormatInstance.iterator();

                  for(int j = 0; j < columns.length; ++j) {
                     ColumnDescription columnDescription = (ColumnDescription)columnDescriptions.next();
                     if (columnDescription.getCategory() == 1 && node != null) {
                        syntaxGraph.addLabel(node, columnDescription.getName(), columns[j]);
                     } else if (columnDescription.getCategory() == 2) {
                        if (!(syntaxGraph instanceof DependencyStructure)) {
                           this.close();
                           throw new DataFormatException("The input graph is not a dependency graph and therefore it is not possible to add dependncy edges. ");
                        }

                        if (columnDescription.getCategory() != 7 && !columns[j].equals("_")) {
                           edge = ((DependencyStructure)syntaxGraph).addDependencyEdge(Integer.parseInt(columns[j]), terminalCounter);
                        }
                     } else if (columnDescription.getCategory() == 3 && edge != null) {
                        syntaxGraph.addLabel(edge, columnDescription.getName(), columns[j]);
                     }
                  }
               } else {
                  syntaxGraph.addComment(token, terminalCounter + 1);
               }
            }
         }

         if (!syntaxGraph.hasTokens()) {
            return false;
         } else {
            ++this.sentenceCount;
            return true;
         }
      } else {
         return false;
      }
   }

   public void readEpilog() throws MaltChainedException {
   }

   public BufferedReader getReader() {
      return this.reader;
   }

   public void setReader(BufferedReader reader) throws MaltChainedException {
      this.close();
      this.reader = reader;
   }

   public DataFormatInstance getDataFormatInstance() {
      return this.dataFormatInstance;
   }

   public void setDataFormatInstance(DataFormatInstance dataFormatInstance) {
      this.dataFormatInstance = dataFormatInstance;
   }

   public int getSentenceCount() throws MaltChainedException {
      return this.sentenceCount;
   }

   public void setSentenceCount(int sentenceCount) {
      this.sentenceCount = sentenceCount;
   }

   public String getOptions() {
      return null;
   }

   public void setOptions(String optionString) throws MaltChainedException {
   }

   public String getFileName() {
      return this.fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }

   public URL getUrl() {
      return this.url;
   }

   public void setUrl(URL url) {
      this.url = url;
   }

   public String getCharsetName() {
      return this.charsetName;
   }

   public void setCharsetName(String charsetName) {
      this.charsetName = charsetName;
   }

   public int getNIterations() {
      return this.nIterations;
   }

   public void setNIterations(int iterations) {
      this.nIterations = iterations;
   }

   public int getIterationCounter() {
      return this.cIterations;
   }

   public void close() throws MaltChainedException {
      try {
         if (this.reader != null) {
            if (this.closeStream) {
               this.reader.close();
            }

            this.reader = null;
         }

      } catch (IOException var2) {
         throw new DataFormatException("Error when closing the input file. ", var2);
      }
   }

   public void clear() throws MaltChainedException {
      this.close();
      this.dataFormatInstance = null;
      this.sentenceCount = 0;
   }
}
