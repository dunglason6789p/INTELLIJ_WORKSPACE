package org.maltparser.core.syntaxgraph.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.PatternSyntaxException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.MappablePhraseStructureGraph;
import org.maltparser.core.syntaxgraph.PhraseStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;

public class NegraReader implements SyntaxGraphReader {
   private BufferedReader reader;
   private DataFormatInstance dataFormatInstance;
   private int sentenceCount;
   private String optionString;
   private int formatVersion;
   private NegraReader.NegraTables currentHeaderTable;
   private int currentTerminalSize;
   private int currentNonTerminalSize;
   private SortedMap<Integer, PhraseStructureNode> nonterminals;
   private StringBuilder edgelabelSymbol;
   private StringBuilder edgelabelTableName;
   private int START_ID_OF_NONTERMINALS = 500;
   private String fileName = null;
   private URL url = null;
   private String charsetName;
   private int nIterations;
   private int cIterations;
   private boolean closeStream = true;

   public NegraReader() {
      this.currentHeaderTable = NegraReader.NegraTables.UNDEF;
      this.edgelabelSymbol = new StringBuilder();
      this.edgelabelTableName = new StringBuilder();
      this.nonterminals = new TreeMap();
      this.nIterations = 1;
      this.cIterations = 1;
   }

   private void reopen() throws MaltChainedException {
      this.close();
      if (this.fileName != null) {
         this.open(this.fileName, this.charsetName);
      } else {
         if (this.url == null) {
            throw new DataFormatException("The input stream cannot be reopen. ");
         }

         this.open(this.url, this.charsetName);
      }

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

      try {
         this.open(url.openStream(), charsetName);
      } catch (IOException var4) {
         throw new DataFormatException("The URL '" + url.toString() + "' cannot be opened. ", var4);
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
      if (syntaxGraph != null && syntaxGraph instanceof PhraseStructure) {
         syntaxGraph.clear();
         PhraseStructure phraseStructure = (PhraseStructure)syntaxGraph;
         SymbolTableHandler symbolTables = phraseStructure.getSymbolTables();
         PhraseStructureNode parent = null;
         PhraseStructureNode child = null;
         this.currentHeaderTable = NegraReader.NegraTables.UNDEF;
         String line = null;
         syntaxGraph.clear();
         this.nonterminals.clear();

         try {
            while(true) {
               line = this.reader.readLine();
               if (line == null) {
                  if (syntaxGraph.hasTokens()) {
                     ++this.sentenceCount;
                     if (syntaxGraph instanceof MappablePhraseStructureGraph) {
                        ((MappablePhraseStructureGraph)syntaxGraph).getMapping().updateDependenyGraph((MappablePhraseStructureGraph)syntaxGraph, ((PhraseStructure)syntaxGraph).getPhraseStructureRoot());
                     }
                  }

                  if (this.cIterations < this.nIterations) {
                     ++this.cIterations;
                     this.reopen();
                     return true;
                  } else {
                     return false;
                  }
               }

               if (line.startsWith("#EOS")) {
                  this.currentTerminalSize = 0;
                  this.currentNonTerminalSize = 0;
                  this.currentHeaderTable = NegraReader.NegraTables.UNDEF;
                  if (syntaxGraph instanceof MappablePhraseStructureGraph) {
                     ((MappablePhraseStructureGraph)syntaxGraph).getMapping().updateDependenyGraph((MappablePhraseStructureGraph)syntaxGraph, ((PhraseStructure)syntaxGraph).getPhraseStructureRoot());
                  }

                  return true;
               }

               int start;
               if (line.startsWith("#BOS")) {
                  this.currentHeaderTable = NegraReader.NegraTables.SENTENCE;
                  int s = -1;
                  int e = -1;
                  int i = 5;

                  for(start = line.length(); i < start; ++i) {
                     if (Character.isDigit(line.charAt(i)) && s == -1) {
                        s = i;
                     }

                     if (line.charAt(i) == ' ') {
                        e = i;
                        break;
                     }
                  }

                  if (s != e && s != -1 && e != -1) {
                     phraseStructure.setSentenceID(Integer.parseInt(line.substring(s, e)));
                  }

                  ++this.sentenceCount;
               } else if (this.currentHeaderTable != NegraReader.NegraTables.SENTENCE) {
                  if (!line.startsWith("%%") && !line.startsWith("#FORMAT") && !line.startsWith("#BOT") && line.startsWith("#EOT")) {
                     this.currentHeaderTable = NegraReader.NegraTables.UNDEF;
                  }
               } else {
                  Iterator columns;
                  ColumnDescription column;
                  char[] lineChars;
                  int secedgecounter;
                  int i;
                  int n;
                  int index;
                  Edge e;
                  PhraseStructureNode parent;
                  if (line.length() >= 2 && line.charAt(0) == '#' && Character.isDigit(line.charAt(1))) {
                     columns = this.dataFormatInstance.iterator();
                     column = null;
                     ++this.currentNonTerminalSize;
                     lineChars = line.toCharArray();
                     start = 0;
                     secedgecounter = 0;
                     i = 0;

                     for(n = lineChars.length; i < n; ++i) {
                        if (lineChars[i] == '\t' && start == i) {
                           ++start;
                        } else if (lineChars[i] == '\t' || i == n - 1) {
                           if (columns.hasNext()) {
                              column = (ColumnDescription)columns.next();
                           }

                           if (column.getPosition() == 0) {
                              index = Integer.parseInt(i == n - 1 ? line.substring(start + 1) : line.substring(start + 1, i));
                              child = (PhraseStructureNode)this.nonterminals.get(index);
                              if (child == null) {
                                 if (index != 0) {
                                    child = ((PhraseStructure)syntaxGraph).addNonTerminalNode(index - this.START_ID_OF_NONTERMINALS + 1);
                                 }

                                 this.nonterminals.put(index, child);
                              }
                           } else if (column.getPosition() == 2 && child != null) {
                              syntaxGraph.addLabel((Element)child, "CAT", i == n - 1 ? line.substring(start) : line.substring(start, i));
                           } else if (column.getCategory() == 4) {
                              this.edgelabelSymbol.setLength(0);
                              this.edgelabelSymbol.append(i == n - 1 ? line.substring(start) : line.substring(start, i));
                              this.edgelabelTableName.setLength(0);
                              this.edgelabelTableName.append(column.getName());
                           } else if (column.getCategory() == 5 && child != null) {
                              index = Integer.parseInt(i == n - 1 ? line.substring(start) : line.substring(start, i));
                              parent = (PhraseStructureNode)this.nonterminals.get(index);
                              if (parent == null) {
                                 if (index == 0) {
                                    parent = phraseStructure.getPhraseStructureRoot();
                                 } else {
                                    parent = phraseStructure.addNonTerminalNode(index - this.START_ID_OF_NONTERMINALS + 1);
                                 }

                                 this.nonterminals.put(index, parent);
                              }

                              e = phraseStructure.addPhraseStructureEdge(parent, (PhraseStructureNode)child);
                              syntaxGraph.addLabel(e, this.edgelabelTableName.toString(), this.edgelabelSymbol.toString());
                           } else if (column.getCategory() == 6 && child != null) {
                              if (secedgecounter % 2 == 0) {
                                 this.edgelabelSymbol.setLength(0);
                                 this.edgelabelSymbol.append(i == n - 1 ? line.substring(start) : line.substring(start, i));
                                 ++secedgecounter;
                              } else {
                                 index = Integer.parseInt(i == n - 1 ? line.substring(start) : line.substring(start, i));
                                 if (index == 0) {
                                    parent = phraseStructure.getPhraseStructureRoot();
                                 } else if (index < this.START_ID_OF_NONTERMINALS) {
                                    parent = phraseStructure.getTokenNode(index);
                                 } else {
                                    parent = (PhraseStructureNode)this.nonterminals.get(index);
                                    if (parent == null) {
                                       parent = phraseStructure.addNonTerminalNode(index - this.START_ID_OF_NONTERMINALS + 1);
                                       this.nonterminals.put(index, parent);
                                    }
                                 }

                                 e = phraseStructure.addSecondaryEdge((ComparableNode)parent, (ComparableNode)child);
                                 e.addLabel(symbolTables.getSymbolTable(column.getName()), this.edgelabelSymbol.toString());
                                 ++secedgecounter;
                              }
                           }

                           start = i + 1;
                        }
                     }
                  } else {
                     columns = this.dataFormatInstance.iterator();
                     column = null;
                     ++this.currentTerminalSize;
                     child = syntaxGraph.addTokenNode(this.currentTerminalSize);
                     lineChars = line.toCharArray();
                     start = 0;
                     secedgecounter = 0;
                     i = 0;

                     for(n = lineChars.length; i < n; ++i) {
                        if (lineChars[i] == '\t' && start == i) {
                           ++start;
                        } else if (lineChars[i] == '\t' || i == n - 1) {
                           if (columns.hasNext()) {
                              column = (ColumnDescription)columns.next();
                           }

                           if (column.getCategory() == 1 && child != null) {
                              syntaxGraph.addLabel((Element)child, column.getName(), i == n - 1 ? line.substring(start) : line.substring(start, i));
                           } else if (column.getCategory() == 4 && child != null) {
                              this.edgelabelSymbol.setLength(0);
                              this.edgelabelSymbol.append(i == n - 1 ? line.substring(start) : line.substring(start, i));
                              this.edgelabelTableName.setLength(0);
                              this.edgelabelTableName.append(column.getName());
                           } else if (column.getCategory() == 5 && child != null) {
                              index = Integer.parseInt(i == n - 1 ? line.substring(start) : line.substring(start, i));
                              parent = (PhraseStructureNode)this.nonterminals.get(index);
                              if (parent == null) {
                                 if (index == 0) {
                                    parent = phraseStructure.getPhraseStructureRoot();
                                 } else {
                                    parent = phraseStructure.addNonTerminalNode(index - this.START_ID_OF_NONTERMINALS + 1);
                                 }

                                 this.nonterminals.put(index, parent);
                              }

                              e = phraseStructure.addPhraseStructureEdge(parent, (PhraseStructureNode)child);
                              syntaxGraph.addLabel(e, this.edgelabelTableName.toString(), this.edgelabelSymbol.toString());
                           } else if (column.getCategory() == 6 && child != null) {
                              if (secedgecounter % 2 == 0) {
                                 this.edgelabelSymbol.setLength(0);
                                 this.edgelabelSymbol.append(i == n - 1 ? line.substring(start) : line.substring(start, i));
                                 ++secedgecounter;
                              } else {
                                 index = Integer.parseInt(i == n - 1 ? line.substring(start) : line.substring(start, i));
                                 if (index == 0) {
                                    parent = phraseStructure.getPhraseStructureRoot();
                                 } else if (index < this.START_ID_OF_NONTERMINALS) {
                                    parent = phraseStructure.getTokenNode(index);
                                 } else {
                                    parent = (PhraseStructureNode)this.nonterminals.get(index);
                                    if (parent == null) {
                                       parent = phraseStructure.addNonTerminalNode(index - this.START_ID_OF_NONTERMINALS + 1);
                                       this.nonterminals.put(index, parent);
                                    }
                                 }

                                 e = phraseStructure.addSecondaryEdge((ComparableNode)parent, (ComparableNode)child);
                                 e.addLabel(symbolTables.getSymbolTable(column.getName()), this.edgelabelSymbol.toString());
                                 ++secedgecounter;
                              }
                           }

                           start = i + 1;
                        }
                     }
                  }
               }
            }
         } catch (IOException var16) {
            throw new DataFormatException("Error when reading from the input file. ", var16);
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

   public void setReader(BufferedReader reader) {
      this.reader = reader;
   }

   public int getSentenceCount() {
      return this.sentenceCount;
   }

   public void setSentenceCount(int sentenceCount) {
      this.sentenceCount = sentenceCount;
   }

   public int getFormatVersion() {
      return this.formatVersion;
   }

   public void setFormatVersion(int formatVersion) {
      this.formatVersion = formatVersion;
   }

   public DataFormatInstance getDataFormatInstance() {
      return this.dataFormatInstance;
   }

   public void setDataFormatInstance(DataFormatInstance inputDataFormatInstance) {
      this.dataFormatInstance = inputDataFormatInstance;
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
            throw new DataFormatException("Unknown NegraReader parameter: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
         }
      }

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
         throw new DataFormatException("Error when closing the input file.", var2);
      }
   }

   private static enum NegraTables {
      ORIGIN,
      EDITOR,
      WORDTAG,
      MORPHTAG,
      NODETAG,
      EDGETAG,
      SECEDGETAG,
      SENTENCE,
      UNDEF;

      private NegraTables() {
      }
   }
}
