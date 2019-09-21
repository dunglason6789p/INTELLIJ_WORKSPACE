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
import java.util.regex.PatternSyntaxException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.MappablePhraseStructureGraph;
import org.maltparser.core.syntaxgraph.PhraseStructure;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class TigerXMLReader implements SyntaxGraphReader {
   private XMLStreamReader reader;
   private int sentenceCount;
   private DataFormatInstance dataFormatInstance;
   private StringBuffer ntid = new StringBuffer();
   private final StringBuilder graphRootID = new StringBuilder();
   private String optionString;
   private String fileName = null;
   private URL url = null;
   private String charsetName;
   private int nIterations = 1;
   private int cIterations = 1;
   private int START_ID_OF_NONTERMINALS = 500;
   private boolean closeStream = true;

   public TigerXMLReader() {
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
      try {
         XMLInputFactory factory = XMLInputFactory.newInstance();
         this.setReader(factory.createXMLStreamReader(new BufferedReader(isr)));
      } catch (XMLStreamException var3) {
         throw new DataFormatException("XML input file could be opened. ", var3);
      }

      this.setSentenceCount(0);
   }

   public void readProlog() throws MaltChainedException {
   }

   public boolean readSentence(TokenStructure syntaxGraph) throws MaltChainedException {
      if (syntaxGraph != null && syntaxGraph instanceof PhraseStructure) {
         syntaxGraph.clear();
         PhraseStructure phraseStructure = (PhraseStructure)syntaxGraph;
         PhraseStructureNode parent = null;
         TokenNode child = null;

         try {
            while(true) {
               while(true) {
                  while(true) {
                     SortedMap inputTables;
                     Iterator i$;
                     String name;
                     do {
                        while(true) {
                           int event = this.reader.next();
                           if (event == 1) {
                              break;
                           }

                           if (event == 2) {
                              if (this.reader.getLocalName().length() != 0 && this.reader.getLocalName().charAt(0) != 'e') {
                                 if (this.reader.getLocalName().charAt(0) == 'n') {
                                    if (this.reader.getLocalName().equals("nt")) {
                                       this.ntid.setLength(0);
                                    } else if (this.reader.getLocalName().equals("nonterminals") && phraseStructure.nTokenNode() == 1 && phraseStructure.nNonTerminals() == 0 && ((NonTerminalNode)phraseStructure.getPhraseStructureRoot()).nChildren() == 0) {
                                       Edge e = phraseStructure.addPhraseStructureEdge(phraseStructure.getPhraseStructureRoot(), phraseStructure.getTokenNode(1));
                                       inputTables = this.dataFormatInstance.getPhraseStructureEdgeLabelSymbolTables(phraseStructure.getSymbolTables());
                                       i$ = inputTables.keySet().iterator();

                                       while(i$.hasNext()) {
                                          name = (String)i$.next();
                                          e.addLabel((SymbolTable)inputTables.get(name), "--");
                                       }
                                    }
                                 } else if (this.reader.getLocalName().charAt(0) != 't') {
                                    if (this.reader.getLocalName().charAt(0) == 's') {
                                       if (this.reader.getLocalName().equals("s")) {
                                          if (syntaxGraph.hasTokens()) {
                                             ++this.sentenceCount;
                                          }

                                          if (syntaxGraph instanceof MappablePhraseStructureGraph) {
                                             ((MappablePhraseStructureGraph)syntaxGraph).getMapping().updateDependenyGraph((MappablePhraseStructureGraph)syntaxGraph, ((PhraseStructure)syntaxGraph).getPhraseStructureRoot());
                                          }

                                          return true;
                                       }
                                    } else if (this.reader.getLocalName().charAt(0) != 'v' && !this.reader.getLocalName().equals("body") && !this.reader.getLocalName().equals("author") && !this.reader.getLocalName().equals("date") && !this.reader.getLocalName().equals("description") && !this.reader.getLocalName().equals("format") && this.reader.getLocalName().equals("history")) {
                                    }
                                 }
                              }
                           } else {
                              if (event == 8) {
                                 if (syntaxGraph.hasTokens()) {
                                    ++this.sentenceCount;
                                 }

                                 if (this.cIterations < this.nIterations) {
                                    ++this.cIterations;
                                    this.reopen();
                                    return true;
                                 }

                                 return false;
                              }

                              if (event == 4) {
                              }
                           }
                        }
                     } while(this.reader.getLocalName().length() == 0);

                     int index;
                     if (this.reader.getLocalName().charAt(0) == 'e') {
                        if (this.reader.getLocalName().length() == 4) {
                           int childid = true;
                           index = this.reader.getAttributeValue((String)null, "idref").indexOf(95);

                           int childid;
                           try {
                              if (index != -1) {
                                 childid = Integer.parseInt(this.reader.getAttributeValue((String)null, "idref").substring(index + 1));
                              } else {
                                 childid = Integer.parseInt(this.reader.getAttributeValue((String)null, "idref"));
                              }

                              if (childid == -1) {
                                 throw new SyntaxGraphException("The tiger reader couldn't recognize the idref attribute '" + this.reader.getAttributeValue((String)null, "idref") + "' of the edge element. ");
                              }
                           } catch (NumberFormatException var12) {
                              throw new SyntaxGraphException("The tiger reader couldn't recognize the idref attribute '" + this.reader.getAttributeValue((String)null, "idref") + "' of the edge element. ");
                           }

                           Object child;
                           if (childid < this.START_ID_OF_NONTERMINALS) {
                              child = phraseStructure.getTokenNode(childid);
                           } else {
                              child = phraseStructure.getNonTerminalNode(childid - this.START_ID_OF_NONTERMINALS + 1);
                           }

                           Edge e = phraseStructure.addPhraseStructureEdge(parent, (PhraseStructureNode)child);
                           SortedMap<String, SymbolTable> inputTables = this.dataFormatInstance.getPhraseStructureEdgeLabelSymbolTables(phraseStructure.getSymbolTables());
                           Iterator i$ = inputTables.keySet().iterator();

                           while(i$.hasNext()) {
                              String name = (String)i$.next();
                              e.addLabel((SymbolTable)inputTables.get(name), this.reader.getAttributeValue((String)null, name.toLowerCase()));
                           }
                        } else if (this.reader.getLocalName().equals("edgelabel")) {
                        }
                     } else {
                        String id;
                        if (this.reader.getLocalName().charAt(0) == 'n') {
                           if (this.reader.getLocalName().length() != 2) {
                              if (this.reader.getLocalName().equals("name")) {
                              }
                           } else {
                              id = this.reader.getAttributeValue((String)null, "id");
                              if (this.graphRootID.length() == id.length() && this.graphRootID.toString().equals(id)) {
                                 parent = phraseStructure.getPhraseStructureRoot();
                              } else {
                                 index = id.indexOf(95);
                                 if (index != -1) {
                                    parent = phraseStructure.addNonTerminalNode(Integer.parseInt(id.substring(index + 1)) - this.START_ID_OF_NONTERMINALS + 1);
                                 }
                              }

                              inputTables = this.dataFormatInstance.getPhraseStructureNodeLabelSymbolTables(phraseStructure.getSymbolTables());
                              i$ = inputTables.keySet().iterator();

                              while(i$.hasNext()) {
                                 name = (String)i$.next();
                                 parent.addLabel((SymbolTable)inputTables.get(name), this.reader.getAttributeValue((String)null, name.toLowerCase()));
                              }
                           }
                        } else if (this.reader.getLocalName().charAt(0) == 't') {
                           if (this.reader.getLocalName().length() == 1) {
                              SortedMap<String, SymbolTable> inputTables = this.dataFormatInstance.getInputSymbolTables(phraseStructure.getSymbolTables());
                              child = syntaxGraph.addTokenNode();
                              Iterator i$ = inputTables.keySet().iterator();

                              while(i$.hasNext()) {
                                 String name = (String)i$.next();
                                 child.addLabel((SymbolTable)inputTables.get(name), this.reader.getAttributeValue((String)null, name.toLowerCase()));
                              }
                           }
                        } else if (this.reader.getLocalName().charAt(0) == 's') {
                           if (this.reader.getLocalName().length() == 1) {
                              id = this.reader.getAttributeValue((String)null, "id");
                              boolean indexable = false;
                              int index = -1;
                              if (id != null && id.length() > 0) {
                                 int i = 0;

                                 for(int n = id.length(); i < n; ++i) {
                                    if (Character.isDigit(id.charAt(i))) {
                                       if (index == -1) {
                                          index = i;
                                       }

                                       indexable = true;
                                    }
                                 }
                              }

                              if (indexable) {
                                 phraseStructure.setSentenceID(Integer.parseInt(id.substring(index)));
                              } else {
                                 phraseStructure.setSentenceID(this.sentenceCount + 1);
                              }
                           }
                        } else if (this.reader.getLocalName().charAt(0) != 'v') {
                           if (this.reader.getLocalName().equals("graph")) {
                              this.graphRootID.setLength(0);
                              this.graphRootID.append(this.reader.getAttributeValue((String)null, "root"));
                           } else if (!this.reader.getLocalName().equals("corpus") && !this.reader.getLocalName().equals("feature") && !this.reader.getLocalName().equals("secedgelabel") && !this.reader.getLocalName().equals("author") && !this.reader.getLocalName().equals("date") && !this.reader.getLocalName().equals("description") && !this.reader.getLocalName().equals("format") && this.reader.getLocalName().equals("history")) {
                           }
                        }
                     }
                  }
               }
            }
         } catch (XMLStreamException var13) {
            throw new DataFormatException("", var13);
         }
      } else {
         return false;
      }
   }

   public int getSentenceCount() {
      return this.sentenceCount;
   }

   public void setSentenceCount(int sentenceCount) {
      this.sentenceCount = sentenceCount;
   }

   public XMLStreamReader getReader() {
      return this.reader;
   }

   public void setReader(XMLStreamReader reader) {
      this.reader = reader;
   }

   public void readEpilog() throws MaltChainedException {
   }

   public void close() throws MaltChainedException {
      try {
         if (this.reader != null) {
            if (this.closeStream) {
               this.reader.close();
            }

            this.reader = null;
         }

      } catch (XMLStreamException var2) {
         throw new DataFormatException("The XML input file could be closed. ", var2);
      }
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
         throw new DataFormatException("Could not split the TigerXML reader option '" + optionString + "'. ", var6);
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
            throw new DataFormatException("Unknown TigerXMLReader parameter: '" + argv[i - 1] + "' with value '" + argv[i] + "'. ");
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
}
