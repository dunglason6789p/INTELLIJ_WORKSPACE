/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser;

import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParserRegistry;

public interface AlgoritmInterface {
    public ParserRegistry getParserRegistry();

    public ParserConfiguration getCurrentParserConfiguration();

    public DependencyParserConfig getManager();
}

