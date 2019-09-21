/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.covington;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.feature.function.Function;
import org.maltparser.parser.AbstractParserFactory;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParserRegistry;
import org.maltparser.parser.algorithm.covington.CovingtonAddressFunction;
import org.maltparser.parser.algorithm.covington.CovingtonConfig;

public abstract class CovingtonFactory
implements AbstractParserFactory {
    protected final DependencyParserConfig manager;

    public CovingtonFactory(DependencyParserConfig _manager) {
        this.manager = _manager;
    }

    @Override
    public ParserConfiguration makeParserConfiguration() throws MaltChainedException {
        boolean allowRoot = (Boolean)this.manager.getOptionValue("covington", "allow_root");
        boolean allowShift = (Boolean)this.manager.getOptionValue("covington", "allow_shift");
        if (this.manager.isLoggerInfoEnabled()) {
            this.manager.logInfoMessage("  Parser configuration : Covington with allow_root=" + allowRoot + " and allow_shift=" + allowShift + "\n");
        }
        CovingtonConfig config = new CovingtonConfig(allowRoot, allowShift);
        return config;
    }

    @Override
    public Function makeFunction(String subFunctionName, FeatureRegistry registry) throws MaltChainedException {
        AlgoritmInterface algorithm = ((ParserRegistry)registry).getAlgorithm();
        return new CovingtonAddressFunction(subFunctionName, algorithm);
    }
}

