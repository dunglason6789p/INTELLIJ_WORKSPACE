package org.maltparser.core.syntaxgraph.writer;

import java.io.OutputStream;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.syntaxgraph.TokenStructure;

public interface SyntaxGraphWriter {
   void open(String var1, String var2) throws MaltChainedException;

   void open(OutputStream var1, String var2) throws MaltChainedException;

   void writeProlog() throws MaltChainedException;

   void writeSentence(TokenStructure var1) throws MaltChainedException;

   void writeEpilog() throws MaltChainedException;

   DataFormatInstance getDataFormatInstance();

   void setDataFormatInstance(DataFormatInstance var1);

   String getOptions();

   void setOptions(String var1) throws MaltChainedException;

   void close() throws MaltChainedException;
}
