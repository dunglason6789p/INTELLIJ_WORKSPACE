/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.guide;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.DependencyParserConfig;

public interface Guide {
    public void finalizeSentence(DependencyStructure var1) throws MaltChainedException;

    public void terminate() throws MaltChainedException;

    public DependencyParserConfig getConfiguration();

    public String getGuideName();

    public void setGuideName(String var1);
}

