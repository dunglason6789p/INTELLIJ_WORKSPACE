/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.writer;

import java.io.OutputStream;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.syntaxgraph.TokenStructure;

public interface SyntaxGraphWriter {
    public void open(String var1, String var2) throws MaltChainedException;

    public void open(OutputStream var1, String var2) throws MaltChainedException;

    public void writeProlog() throws MaltChainedException;

    public void writeSentence(TokenStructure var1) throws MaltChainedException;

    public void writeEpilog() throws MaltChainedException;

    public DataFormatInstance getDataFormatInstance();

    public void setDataFormatInstance(DataFormatInstance var1);

    public String getOptions();

    public void setOptions(String var1) throws MaltChainedException;

    public void close() throws MaltChainedException;
}

