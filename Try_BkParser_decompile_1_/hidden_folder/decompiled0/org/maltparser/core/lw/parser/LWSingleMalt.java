/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.lw.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.maltparser.concurrent.graph.dataformat.DataFormat;
import org.maltparser.core.config.ConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModelManager;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.lw.graph.LWDependencyGraph;
import org.maltparser.core.lw.graph.LWDeprojectivizer;
import org.maltparser.core.lw.parser.LWDecisionModel;
import org.maltparser.core.lw.parser.LWDeterministicParser;
import org.maltparser.core.lw.parser.McoModel;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.parse.ParseSymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.AbstractParserFactory;
import org.maltparser.parser.DependencyParserConfig;

public final class LWSingleMalt
implements DependencyParserConfig {
    public static final Class<?>[] paramTypes = new Class[]{DependencyParserConfig.class};
    private final McoModel mcoModel;
    private final int optionContainerIndex;
    private final DataFormatInstance dataFormatInstance;
    private final PropagationManager propagationManager;
    private final FeatureModelManager featureModelManager;
    private final AbstractParserFactory parserFactory;
    private final String decisionSettings;
    private final int kBestSize;
    private final String classitem_separator;
    private final URL featureModelURL;
    private final String dataSplitColumn;
    private final String dataSplitStructure;
    private final boolean excludeNullValues;
    private final LWDecisionModel decisionModel;

    public LWSingleMalt(int containerIndex, DataFormatInstance dataFormatInstance, McoModel _mcoModel, PropagationManager _propagationManager, FeatureModelManager _featureModelManager) throws MaltChainedException {
        this.optionContainerIndex = containerIndex;
        this.mcoModel = _mcoModel;
        this.dataFormatInstance = dataFormatInstance;
        this.propagationManager = _propagationManager;
        this.featureModelManager = _featureModelManager;
        this.parserFactory = this.makeParserFactory();
        this.decisionSettings = this.getOptionValue("guide", "decision_settings").toString().trim();
        this.kBestSize = (Integer)this.getOptionValue("guide", "kbest");
        this.classitem_separator = this.getOptionValue("guide", "classitem_separator").toString().trim();
        this.featureModelURL = this.getConfigFileEntryURL(this.getOptionValue("guide", "features").toString().trim());
        this.dataSplitColumn = this.getOptionValue("guide", "data_split_column").toString().trim();
        this.dataSplitStructure = this.getOptionValue("guide", "data_split_structure").toString().trim();
        this.excludeNullValues = this.getOptionValue("singlemalt", "null_value").toString().equalsIgnoreCase("none");
        this.decisionModel = new LWDecisionModel(this.mcoModel, this.excludeNullValues, this.getOptionValueString("guide", "learner"));
    }

    private AbstractParserFactory makeParserFactory() throws MaltChainedException {
        Class clazz = (Class)this.getOptionValue("singlemalt", "parsing_algorithm");
        try {
            Object[] arguments = new Object[]{this};
            return (AbstractParserFactory)clazz.getConstructor(paramTypes).newInstance(arguments);
        }
        catch (NoSuchMethodException e) {
            throw new ConfigurationException("The parser factory '" + clazz.getName() + "' cannot be initialized. ", e);
        }
        catch (InstantiationException e) {
            throw new ConfigurationException("The parser factory '" + clazz.getName() + "' cannot be initialized. ", e);
        }
        catch (IllegalAccessException e) {
            throw new ConfigurationException("The parser factory '" + clazz.getName() + "' cannot be initialized. ", e);
        }
        catch (InvocationTargetException e) {
            throw new ConfigurationException("The parser factory '" + clazz.getName() + "' cannot be initialized. ", e);
        }
    }

    @Override
    public FeatureModelManager getFeatureModelManager() {
        return this.featureModelManager;
    }

    @Override
    public AbstractParserFactory getParserFactory() {
        return this.parserFactory;
    }

    @Override
    public void parse(DependencyStructure graph) throws MaltChainedException {
        if (graph.hasTokens()) {
            LWDeterministicParser parser = new LWDeterministicParser(this, graph.getSymbolTables());
            parser.parse(graph);
        }
    }

    public List<String[]> parseSentences(List<String[]> inputSentences, String defaultRootLabel, int markingStrategy, boolean coveredRoot, SymbolTableHandler parentSymbolTableHandler, DataFormat concurrentDataFormat) throws MaltChainedException {
        List<String[]> outputSentences = Collections.synchronizedList(new ArrayList());
        ParseSymbolTableHandler parseSymbolTableHandler = new ParseSymbolTableHandler(parentSymbolTableHandler);
        LWDependencyGraph parseGraph = new LWDependencyGraph(concurrentDataFormat, parseSymbolTableHandler);
        LWDeterministicParser parser = new LWDeterministicParser(this, parseSymbolTableHandler);
        for (int i = 0; i < inputSentences.size(); ++i) {
            String[] tokens = inputSentences.get(i);
            parseGraph.resetTokens(tokens, defaultRootLabel, false);
            parser.parse(parseGraph);
            if (markingStrategy != 0 || coveredRoot) {
                new LWDeprojectivizer().deprojectivize(parseGraph, markingStrategy);
            }
            String[] outputTokens = new String[tokens.length];
            for (int j = 0; j < outputTokens.length; ++j) {
                outputTokens[j] = parseGraph.getDependencyNode(j + 1).toString();
            }
            outputSentences.add(outputTokens);
        }
        return outputSentences;
    }

    @Override
    public void oracleParse(DependencyStructure goldGraph, DependencyStructure oracleGraph) throws MaltChainedException {
    }

    public void terminate(Object[] arguments) throws MaltChainedException {
    }

    @Override
    public boolean isLoggerInfoEnabled() {
        return false;
    }

    @Override
    public boolean isLoggerDebugEnabled() {
        return false;
    }

    @Override
    public void logErrorMessage(String message) {
    }

    @Override
    public void logInfoMessage(String message) {
    }

    @Override
    public void logInfoMessage(char character) {
    }

    @Override
    public void logDebugMessage(String message) {
    }

    @Override
    public void writeInfoToConfigFile(String message) throws MaltChainedException {
    }

    @Override
    public OutputStreamWriter getOutputStreamWriter(String fileName) throws MaltChainedException {
        return null;
    }

    @Override
    public OutputStreamWriter getAppendOutputStreamWriter(String fileName) throws MaltChainedException {
        return null;
    }

    @Override
    public InputStreamReader getInputStreamReader(String fileName) throws MaltChainedException {
        try {
            return this.mcoModel.getInputStreamReader(fileName, "UTF-8");
        }
        catch (IOException e) {
            throw new ConfigurationException("Couldn't read file " + fileName + " from mco-file ", e);
        }
    }

    @Override
    public InputStream getInputStreamFromConfigFileEntry(String fileName) throws MaltChainedException {
        try {
            return this.mcoModel.getInputStream(fileName);
        }
        catch (IOException e) {
            throw new ConfigurationException("Couldn't read file " + fileName + " from mco-file ", e);
        }
    }

    @Override
    public URL getConfigFileEntryURL(String fileName) throws MaltChainedException {
        try {
            return this.mcoModel.getMcoEntryURL(fileName);
        }
        catch (IOException e) {
            throw new ConfigurationException("Couldn't read file " + fileName + " from mco-file ", e);
        }
    }

    @Override
    public Object getConfigFileEntryObject(String fileName) throws MaltChainedException {
        return this.mcoModel.getMcoEntryObject(fileName);
    }

    @Override
    public String getConfigFileEntryString(String fileName) throws MaltChainedException {
        return this.mcoModel.getMcoEntryString(fileName);
    }

    @Override
    public File getFile(String fileName) throws MaltChainedException {
        return new File(System.getProperty("user.dir") + File.separator + fileName);
    }

    @Override
    public Object getOptionValue(String optiongroup, String optionname) throws MaltChainedException {
        return OptionManager.instance().getOptionValue(this.optionContainerIndex, optiongroup, optionname);
    }

    @Override
    public String getOptionValueString(String optiongroup, String optionname) throws MaltChainedException {
        return OptionManager.instance().getOptionValueString(this.optionContainerIndex, optiongroup, optionname);
    }

    @Override
    public SymbolTableHandler getSymbolTables() {
        return null;
    }

    @Override
    public DataFormatInstance getDataFormatInstance() {
        return this.dataFormatInstance;
    }

    @Override
    public PropagationManager getPropagationManager() {
        return this.propagationManager;
    }

    public String getDecisionSettings() {
        return this.decisionSettings;
    }

    public int getkBestSize() {
        return this.kBestSize;
    }

    public String getClassitem_separator() {
        return this.classitem_separator;
    }

    public URL getFeatureModelURL() {
        return this.featureModelURL;
    }

    public String getDataSplitColumn() {
        return this.dataSplitColumn;
    }

    public String getDataSplitStructure() {
        return this.dataSplitStructure;
    }

    public boolean isExcludeNullValues() {
        return this.excludeNullValues;
    }

    public LWDecisionModel getDecisionModel() {
        return this.decisionModel;
    }
}

