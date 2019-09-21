/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser;

import org.maltparser.core.config.Configuration;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModelManager;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.AbstractParserFactory;

public interface DependencyParserConfig
extends Configuration {
    public void parse(DependencyStructure var1) throws MaltChainedException;

    public void oracleParse(DependencyStructure var1, DependencyStructure var2) throws MaltChainedException;

    public DataFormatInstance getDataFormatInstance();

    public FeatureModelManager getFeatureModelManager();

    public PropagationManager getPropagationManager();

    public AbstractParserFactory getParserFactory();
}

