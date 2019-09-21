/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.twoplanar;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.feature.function.Function;
import org.maltparser.parser.AbstractParserFactory;
import org.maltparser.parser.AlgoritmInterface;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParserRegistry;
import org.maltparser.parser.algorithm.twoplanar.TwoPlanarAddressFunction;
import org.maltparser.parser.algorithm.twoplanar.TwoPlanarConfig;

public abstract class TwoPlanarFactory
implements AbstractParserFactory {
    protected final DependencyParserConfig manager;

    public TwoPlanarFactory(DependencyParserConfig _manager) {
        this.manager = _manager;
    }

    @Override
    public ParserConfiguration makeParserConfiguration() throws MaltChainedException {
        if (this.manager.isLoggerInfoEnabled()) {
            this.manager.logInfoMessage("  Parser configuration : Two-Planar with no_covered_roots = " + this.manager.getOptionValue("planar", "no_covered_roots").toString().toUpperCase() + ", " + "acyclicity = " + this.manager.getOptionValue("planar", "acyclicity").toString().toUpperCase() + ", planar root handling = " + this.manager.getOptionValue("2planar", "planar_root_handling").toString().toUpperCase() + ", reduce on switch = " + this.manager.getOptionValue("2planar", "reduceonswitch").toString().toUpperCase() + "\n");
        }
        return new TwoPlanarConfig(this.manager.getOptionValue("planar", "no_covered_roots").toString(), this.manager.getOptionValue("planar", "acyclicity").toString(), this.manager.getOptionValue("2planar", "reduceonswitch").toString(), this.manager.getOptionValue("multiplanar", "planar_root_handling").toString());
    }

    @Override
    public Function makeFunction(String subFunctionName, FeatureRegistry registry) throws MaltChainedException {
        AlgoritmInterface algorithm = ((ParserRegistry)registry).getAlgorithm();
        return new TwoPlanarAddressFunction(subFunctionName, algorithm);
    }
}

