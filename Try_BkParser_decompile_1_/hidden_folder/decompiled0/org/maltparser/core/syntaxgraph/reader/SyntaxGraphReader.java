/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.reader;

import java.io.InputStream;
import java.net.URL;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.syntaxgraph.TokenStructure;

public interface SyntaxGraphReader {
    public void open(String var1, String var2) throws MaltChainedException;

    public void open(URL var1, String var2) throws MaltChainedException;

    public void open(InputStream var1, String var2) throws MaltChainedException;

    public void readProlog() throws MaltChainedException;

    public boolean readSentence(TokenStructure var1) throws MaltChainedException;

    public void readEpilog() throws MaltChainedException;

    public int getSentenceCount() throws MaltChainedException;

    public DataFormatInstance getDataFormatInstance();

    public void setDataFormatInstance(DataFormatInstance var1);

    public String getOptions();

    public void setOptions(String var1) throws MaltChainedException;

    public void close() throws MaltChainedException;

    public int getNIterations();

    public void setNIterations(int var1);

    public int getIterationCounter();
}

