package org.maltparser.core.syntaxgraph.reader;

import java.io.InputStream;
import java.net.URL;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.syntaxgraph.TokenStructure;

public interface SyntaxGraphReader {
   void open(String var1, String var2) throws MaltChainedException;

   void open(URL var1, String var2) throws MaltChainedException;

   void open(InputStream var1, String var2) throws MaltChainedException;

   void readProlog() throws MaltChainedException;

   boolean readSentence(TokenStructure var1) throws MaltChainedException;

   void readEpilog() throws MaltChainedException;

   int getSentenceCount() throws MaltChainedException;

   DataFormatInstance getDataFormatInstance();

   void setDataFormatInstance(DataFormatInstance var1);

   String getOptions();

   void setOptions(String var1) throws MaltChainedException;

   void close() throws MaltChainedException;

   int getNIterations();

   void setNIterations(int var1);

   int getIterationCounter();
}
