/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.ParsingAlgorithm;

public abstract class Parser
extends ParsingAlgorithm {
    public Parser(DependencyParserConfig manager, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
        super(manager, symbolTableHandler);
    }

    public abstract DependencyStructure parse(DependencyStructure var1) throws MaltChainedException;
}

