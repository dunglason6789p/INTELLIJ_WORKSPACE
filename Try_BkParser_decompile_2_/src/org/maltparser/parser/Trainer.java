/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.ParsingAlgorithm;
import org.maltparser.parser.guide.OracleGuide;

public abstract class Trainer
extends ParsingAlgorithm {
    public Trainer(DependencyParserConfig manager, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
        super(manager, symbolTableHandler);
    }

    public abstract DependencyStructure parse(DependencyStructure var1, DependencyStructure var2) throws MaltChainedException;

    public abstract OracleGuide getOracleGuide();

    public abstract void train() throws MaltChainedException;
}

