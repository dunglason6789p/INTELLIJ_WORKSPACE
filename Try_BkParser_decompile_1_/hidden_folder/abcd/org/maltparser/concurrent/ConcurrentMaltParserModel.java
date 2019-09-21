/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.concurrent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import org.maltparser.concurrent.graph.ConcurrentDependencyGraph;
import org.maltparser.concurrent.graph.dataformat.DataFormat;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModelManager;
import org.maltparser.core.feature.system.FeatureEngine;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.io.dataformat.DataFormatManager;
import org.maltparser.core.io.dataformat.DataFormatSpecification;
import org.maltparser.core.lw.graph.LWDependencyGraph;
import org.maltparser.core.lw.graph.LWDeprojectivizer;
import org.maltparser.core.lw.parser.LWSingleMalt;
import org.maltparser.core.lw.parser.McoModel;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.plugin.PluginLoader;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.hash.HashSymbolTableHandler;
import org.maltparser.core.symbol.parse.ParseSymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public final class ConcurrentMaltParserModel {
    private final DataFormatInstance dataFormatInstance;
    private final DataFormat concurrentDataFormat;
    private final SymbolTableHandler parentSymbolTableHandler;
    private final LWSingleMalt singleMalt;
    private final int optionContainer;
    private final McoModel mcoModel;
    private final int markingStrategy;
    private final boolean coveredRoot;
    private final String defaultRootLabel;

    protected ConcurrentMaltParserModel(int _optionContainer, URL _mcoURL) throws MaltChainedException {
        this.optionContainer = _optionContainer;
        this.mcoModel = new McoModel(_mcoURL);
        String inputFormatName = OptionManager.instance().getOptionValue(this.optionContainer, "input", "format").toString().trim();
        URL inputFormatURL = null;
        try {
            inputFormatURL = this.mcoModel.getMcoEntryURL(inputFormatName);
        }
        catch (IOException e) {
            throw new MaltChainedException("Couldn't read file " + inputFormatName + " from mco-file ", e);
        }
        DataFormatManager dataFormatManager = new DataFormatManager(inputFormatURL, inputFormatURL);
        this.parentSymbolTableHandler = new HashSymbolTableHandler();
        this.dataFormatInstance = dataFormatManager.getInputDataFormatSpec().createDataFormatInstance(this.parentSymbolTableHandler, OptionManager.instance().getOptionValueString(this.optionContainer, "singlemalt", "null_value"));
        try {
            this.parentSymbolTableHandler.load(this.mcoModel.getInputStreamReader("symboltables.sym", "UTF-8"));
        }
        catch (IOException e) {
            throw new MaltChainedException("Couldn't read file symboltables.sym from mco-file ", e);
        }
        this.defaultRootLabel = OptionManager.instance().getOptionValue(this.optionContainer, "graph", "root_label").toString().trim();
        this.markingStrategy = LWDeprojectivizer.getMarkingStrategyInt(OptionManager.instance().getOptionValue(this.optionContainer, "pproj", "marking_strategy").toString().trim());
        this.coveredRoot = !OptionManager.instance().getOptionValue(this.optionContainer, "pproj", "covered_root").toString().trim().equalsIgnoreCase("none");
        FeatureModelManager featureModelManager = this.loadFeatureModelManager(this.optionContainer, this.mcoModel);
        this.singleMalt = new LWSingleMalt(this.optionContainer, this.dataFormatInstance, this.mcoModel, null, featureModelManager);
        this.concurrentDataFormat = DataFormat.parseDataFormatXMLfile(inputFormatURL);
    }

    public ConcurrentDependencyGraph parse(String[] tokens) throws MaltChainedException {
        return new ConcurrentDependencyGraph(this.concurrentDataFormat, this.internalParse(tokens), this.defaultRootLabel);
    }

    public String[] parseTokens(String[] tokens) throws MaltChainedException {
        LWDependencyGraph outputGraph = this.internalParse(tokens);
        String[] outputTokens = new String[tokens.length];
        for (int i = 0; i < outputTokens.length; ++i) {
            outputTokens[i] = outputGraph.getDependencyNode(i + 1).toString();
        }
        return outputTokens;
    }

    private LWDependencyGraph internalParse(String[] tokens) throws MaltChainedException {
        if (tokens == null || tokens.length == 0) {
            throw new MaltChainedException("Nothing to parse. ");
        }
        LWDependencyGraph parseGraph = new LWDependencyGraph(this.concurrentDataFormat, new ParseSymbolTableHandler(this.parentSymbolTableHandler), tokens, this.defaultRootLabel, false);
        this.singleMalt.parse(parseGraph);
        if (this.markingStrategy != 0 || this.coveredRoot) {
            new LWDeprojectivizer().deprojectivize(parseGraph, this.markingStrategy);
        }
        return parseGraph;
    }

    public List<String[]> parseSentences(List<String[]> inputSentences) throws MaltChainedException {
        return this.singleMalt.parseSentences(inputSentences, this.defaultRootLabel, this.markingStrategy, this.coveredRoot, this.parentSymbolTableHandler, this.concurrentDataFormat);
    }

    private FeatureModelManager loadFeatureModelManager(int optionContainer, McoModel mcoModel) throws MaltChainedException {
        FeatureEngine system = new FeatureEngine();
        system.load("/appdata/features/ParserFeatureSystem.xml");
        system.load(PluginLoader.instance());
        FeatureModelManager featureModelManager = new FeatureModelManager(system);
        String featureModelFileName = OptionManager.instance().getOptionValue(optionContainer, "guide", "features").toString().trim();
        try {
            if (featureModelFileName.endsWith(".par")) {
                String markingStrategy = OptionManager.instance().getOptionValue(optionContainer, "pproj", "marking_strategy").toString().trim();
                String coveredRoot = OptionManager.instance().getOptionValue(optionContainer, "pproj", "covered_root").toString().trim();
                featureModelManager.loadParSpecification(mcoModel.getMcoEntryURL(featureModelFileName), markingStrategy, coveredRoot);
            } else {
                featureModelManager.loadSpecification(mcoModel.getMcoEntryURL(featureModelFileName));
            }
        }
        catch (IOException e) {
            throw new MaltChainedException("Couldn't read file " + featureModelFileName + " from mco-file ", e);
        }
        return featureModelManager;
    }
}

