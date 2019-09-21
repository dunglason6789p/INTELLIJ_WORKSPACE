/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Formatter;
import java.util.regex.Pattern;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.maltparser.core.config.ConfigurationDir;
import org.maltparser.core.config.ConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureModelManager;
import org.maltparser.core.feature.system.FeatureEngine;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.helper.URLFinder;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.plugin.PluginLoader;
import org.maltparser.core.propagation.PropagationException;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.parser.AbstractParserFactory;
import org.maltparser.parser.BatchTrainer;
import org.maltparser.parser.BatchTrainerWithDiagnostics;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.DeterministicParser;
import org.maltparser.parser.DeterministicParserWithDiagnostics;
import org.maltparser.parser.Parser;
import org.maltparser.parser.ParsingAlgorithm;
import org.maltparser.parser.Trainer;
import org.maltparser.parser.guide.ClassifierGuide;

public class SingleMalt
implements DependencyParserConfig {
    public static final Class<?>[] paramTypes = new Class[]{DependencyParserConfig.class};
    public static final int LEARN = 0;
    public static final int PARSE = 1;
    protected ConfigurationDir configDir;
    protected Logger configLogger;
    protected int optionContainerIndex;
    protected ParsingAlgorithm parsingAlgorithm = null;
    protected int mode;
    protected SymbolTableHandler symbolTableHandler;
    protected DataFormatInstance dataFormatInstance;
    protected FeatureModelManager featureModelManager;
    protected long startTime;
    protected long endTime;
    protected int nIterations = 0;
    protected PropagationManager propagationManager;
    private Parser parser;
    private Trainer trainer;
    private AbstractParserFactory parserFactory;

    public void initialize(int containerIndex, DataFormatInstance dataFormatInstance, SymbolTableHandler symbolTableHandler, ConfigurationDir configDir, int mode) throws MaltChainedException {
        this.optionContainerIndex = containerIndex;
        this.mode = mode;
        this.setConfigurationDir(configDir);
        this.startTime = System.currentTimeMillis();
        this.configLogger = this.initConfigLogger(this.getOptionValue("config", "logfile").toString(), this.getOptionValue("config", "logging").toString());
        this.dataFormatInstance = dataFormatInstance;
        this.symbolTableHandler = symbolTableHandler;
        this.parserFactory = this.makeParserFactory();
        if (mode == 0) {
            this.checkOptionDependency();
        }
        this.initPropagation();
        this.initFeatureSystem();
        this.initParsingAlgorithm();
        if (this.configLogger.isInfoEnabled()) {
            URL inputFormatURL = configDir.getInputFormatURL();
            URL outputFormatURL = configDir.getOutputFormatURL();
            if (inputFormatURL != null) {
                if (outputFormatURL == null || outputFormatURL.toString().equals(inputFormatURL.toString())) {
                    int index = inputFormatURL.toString().indexOf(33);
                    if (index == -1) {
                        this.configLogger.info("  Data Format          : " + inputFormatURL.toString() + "\n");
                    } else {
                        this.configLogger.info("  Data Format          : " + inputFormatURL.toString().substring(index + 1) + "\n");
                    }
                } else {
                    int indexIn = inputFormatURL.toString().indexOf(33);
                    int indexOut = outputFormatURL.toString().indexOf(33);
                    if (indexIn == -1) {
                        this.configLogger.info("  Input Data Format    : " + inputFormatURL.toString() + "\n");
                    } else {
                        this.configLogger.info("  Input Data Format    : " + inputFormatURL.toString().substring(indexIn + 1) + "\n");
                    }
                    if (indexOut == -1) {
                        this.configLogger.info("  Output Data Format   : " + outputFormatURL.toString() + "\n");
                    } else {
                        this.configLogger.info("  Output Data Format   : " + outputFormatURL.toString().substring(indexOut + 1) + "\n");
                    }
                }
            }
        }
    }

    private void initPropagation() throws MaltChainedException {
        String propagationSpecFileName = this.getOptionValue("singlemalt", "propagation").toString();
        if (propagationSpecFileName == null || propagationSpecFileName.length() == 0) {
            return;
        }
        this.propagationManager = new PropagationManager();
        if (this.mode == 0) {
            propagationSpecFileName = this.configDir.copyToConfig(propagationSpecFileName);
            OptionManager.instance().overloadOptionValue(this.optionContainerIndex, "singlemalt", "propagation", propagationSpecFileName);
        }
        if (this.isLoggerInfoEnabled()) {
            this.logInfoMessage("  Propagation          : " + propagationSpecFileName + "\n");
        }
        this.propagationManager.loadSpecification(this.findURL(propagationSpecFileName));
        this.propagationManager.createPropagations(this.dataFormatInstance, this.symbolTableHandler);
    }

    protected void initParsingAlgorithm() throws MaltChainedException {
        boolean diagnostics = (Boolean)this.getOptionValue("singlemalt", "diagnostics");
        if (this.mode == 0) {
            if (!diagnostics) {
                this.trainer = new BatchTrainer(this, this.symbolTableHandler);
                this.parsingAlgorithm = this.trainer;
            } else {
                this.trainer = new BatchTrainerWithDiagnostics(this, this.symbolTableHandler);
                this.parsingAlgorithm = this.trainer;
            }
        } else if (this.mode == 1) {
            if (!diagnostics) {
                this.parser = new DeterministicParser(this, this.symbolTableHandler);
                this.parsingAlgorithm = this.parser;
            } else {
                this.parser = new DeterministicParserWithDiagnostics(this, this.symbolTableHandler);
                this.parsingAlgorithm = this.parser;
            }
        }
    }

    protected void initFeatureSystem() throws MaltChainedException {
        FeatureEngine system = new FeatureEngine();
        system.load("/appdata/features/ParserFeatureSystem.xml");
        system.load(PluginLoader.instance());
        this.featureModelManager = new FeatureModelManager(system);
        String featureModelFileName = this.getOptionValue("guide", "features").toString().trim();
        if (featureModelFileName.endsWith(".par")) {
            String markingStrategy = this.getOptionValue("pproj", "marking_strategy").toString().trim();
            String coveredRoot = this.getOptionValue("pproj", "covered_root").toString().trim();
            this.featureModelManager.loadParSpecification(this.findURL(featureModelFileName), markingStrategy, coveredRoot);
        } else {
            this.featureModelManager.loadSpecification(this.findURL(featureModelFileName));
        }
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
    public AbstractParserFactory getParserFactory() {
        return this.parserFactory;
    }

    @Override
    public FeatureModelManager getFeatureModelManager() {
        return this.featureModelManager;
    }

    public void process(Object[] arguments) throws MaltChainedException {
        if (this.mode == 0) {
            if (arguments.length < 2 || !(arguments[0] instanceof DependencyStructure) || !(arguments[1] instanceof DependencyStructure)) {
                throw new MaltChainedException("The single malt learn task must be supplied with at least two dependency structures. ");
            }
            DependencyStructure systemGraph = (DependencyStructure)arguments[0];
            DependencyStructure goldGraph = (DependencyStructure)arguments[1];
            if (systemGraph.hasTokens() && this.getGuide() != null) {
                this.getGuide().finalizeSentence(((Trainer)this.getAlgorithm()).parse(goldGraph, systemGraph));
            }
        } else if (this.mode == 1) {
            if (arguments.length < 1 || !(arguments[0] instanceof DependencyStructure)) {
                throw new MaltChainedException("The single malt parse task must be supplied with at least one input terminal structure and one output dependency structure. ");
            }
            DependencyStructure processGraph = (DependencyStructure)arguments[0];
            if (processGraph.hasTokens()) {
                this.parser.parse(processGraph);
            }
        }
    }

    @Override
    public void parse(DependencyStructure graph) throws MaltChainedException {
        if (graph.hasTokens()) {
            this.parser.parse(graph);
        }
    }

    @Override
    public void oracleParse(DependencyStructure goldGraph, DependencyStructure oracleGraph) throws MaltChainedException {
        if (oracleGraph.hasTokens()) {
            if (this.getGuide() != null) {
                this.getGuide().finalizeSentence(this.trainer.parse(goldGraph, oracleGraph));
            } else {
                this.trainer.parse(goldGraph, oracleGraph);
            }
        }
    }

    public void train() throws MaltChainedException {
        if (this.getGuide() == null) {
            ((Trainer)this.getAlgorithm()).train();
        }
    }

    public void terminate(Object[] arguments) throws MaltChainedException {
        this.getAlgorithm().terminate();
        if (this.getGuide() != null) {
            this.getGuide().terminate();
        }
        if (this.mode == 0) {
            this.endTime = System.currentTimeMillis();
            long elapsed = this.endTime - this.startTime;
            if (this.configLogger.isInfoEnabled()) {
                this.configLogger.info("Learning time: " + new Formatter().format("%02d:%02d:%02d", elapsed / 3600000L, elapsed % 3600000L / 60000L, elapsed % 60000L / 1000L) + " (" + elapsed + " ms)\n");
            }
        } else if (this.mode == 1) {
            this.endTime = System.currentTimeMillis();
            long elapsed = this.endTime - this.startTime;
            if (this.configLogger.isInfoEnabled()) {
                this.configLogger.info("Parsing time: " + new Formatter().format("%02d:%02d:%02d", elapsed / 3600000L, elapsed % 3600000L / 60000L, elapsed % 60000L / 1000L) + " (" + elapsed + " ms)\n");
            }
        }
        if (SystemLogger.logger() != this.configLogger && this.configLogger != null) {
            this.configLogger.removeAllAppenders();
        }
    }

    public Logger initConfigLogger(String logfile, String level) throws MaltChainedException {
        if (logfile != null && logfile.length() > 0 && !logfile.equalsIgnoreCase("stdout") && this.configDir != null) {
            this.configLogger = Logger.getLogger(logfile);
            FileAppender fileAppender = null;
            try {
                fileAppender = new FileAppender(new PatternLayout("%m"), this.configDir.getWorkingDirectory().getPath() + File.separator + logfile, true);
            }
            catch (IOException e) {
                throw new ConfigurationException("It is not possible to create a configuration log file. ", e);
            }
            fileAppender.setThreshold(Level.toLevel(level, Level.INFO));
            this.configLogger.addAppender(fileAppender);
            this.configLogger.setLevel(Level.toLevel(level, Level.INFO));
        } else {
            this.configLogger = SystemLogger.logger();
        }
        return this.configLogger;
    }

    @Override
    public boolean isLoggerInfoEnabled() {
        return this.configLogger != null && this.configLogger.isInfoEnabled();
    }

    @Override
    public boolean isLoggerDebugEnabled() {
        return this.configLogger != null && this.configLogger.isDebugEnabled();
    }

    @Override
    public void logErrorMessage(String message) {
        this.configLogger.error(message);
    }

    @Override
    public void logInfoMessage(String message) {
        this.configLogger.info(message);
    }

    @Override
    public void logInfoMessage(char character) {
        this.configLogger.info(Character.valueOf(character));
    }

    @Override
    public void logDebugMessage(String message) {
        this.configLogger.debug(message);
    }

    @Override
    public void writeInfoToConfigFile(String message) throws MaltChainedException {
        try {
            this.configDir.getInfoFileWriter().write(message);
            this.configDir.getInfoFileWriter().flush();
        }
        catch (IOException e) {
            throw new ConfigurationException("Could not write to the configuration information file. ", e);
        }
    }

    public Logger getConfigLogger() {
        return this.configLogger;
    }

    public void setConfigLogger(Logger logger) {
        this.configLogger = logger;
    }

    public ConfigurationDir getConfigurationDir() {
        return this.configDir;
    }

    public void setConfigurationDir(ConfigurationDir configDir) {
        this.configDir = configDir;
    }

    @Override
    public OutputStreamWriter getOutputStreamWriter(String fileName) throws MaltChainedException {
        return this.configDir.getOutputStreamWriter(fileName);
    }

    @Override
    public OutputStreamWriter getAppendOutputStreamWriter(String fileName) throws MaltChainedException {
        return this.configDir.getAppendOutputStreamWriter(fileName);
    }

    @Override
    public InputStreamReader getInputStreamReader(String fileName) throws MaltChainedException {
        return this.configDir.getInputStreamReader(fileName);
    }

    @Override
    public InputStream getInputStreamFromConfigFileEntry(String fileName) throws MaltChainedException {
        return this.configDir.getInputStreamFromConfigFileEntry(fileName);
    }

    @Override
    public URL getConfigFileEntryURL(String fileName) throws MaltChainedException {
        return this.configDir.getConfigFileEntryURL(fileName);
    }

    @Override
    public File getFile(String fileName) throws MaltChainedException {
        return this.configDir.getFile(fileName);
    }

    @Override
    public Object getConfigFileEntryObject(String fileName) throws MaltChainedException {
        Object object = null;
        try {
            try (ObjectInputStream input = new ObjectInputStream(this.getInputStreamFromConfigFileEntry(fileName));){
                object = input.readObject();
            }
        }
        catch (IOException e) {
            throw new ConfigurationException("Could not load object from '" + fileName + "' in mco-file", e);
        }
        return object;
    }

    @Override
    public String getConfigFileEntryString(String fileName) throws MaltChainedException {
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getInputStreamFromConfigFileEntry(fileName), "UTF-8"));
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
        }
        catch (IOException e) {
            throw new ConfigurationException("Could not load string from '" + fileName + "' in mco-file", e);
        }
        return sb.toString();
    }

    public int getMode() {
        return this.mode;
    }

    @Override
    public Object getOptionValue(String optiongroup, String optionname) throws MaltChainedException {
        return OptionManager.instance().getOptionValue(this.optionContainerIndex, optiongroup, optionname);
    }

    @Override
    public String getOptionValueString(String optiongroup, String optionname) throws MaltChainedException {
        return OptionManager.instance().getOptionValueString(this.optionContainerIndex, optiongroup, optionname);
    }

    public OptionManager getOptionManager() throws MaltChainedException {
        return OptionManager.instance();
    }

    @Override
    public SymbolTableHandler getSymbolTables() {
        return this.symbolTableHandler;
    }

    @Override
    public DataFormatInstance getDataFormatInstance() {
        return this.dataFormatInstance;
    }

    @Override
    public PropagationManager getPropagationManager() {
        return this.propagationManager;
    }

    public ParsingAlgorithm getAlgorithm() {
        return this.parsingAlgorithm;
    }

    public ClassifierGuide getGuide() {
        return this.parsingAlgorithm.getGuide();
    }

    public void checkOptionDependency() throws MaltChainedException {
        try {
            String featureModelFileName;
            if (this.configDir.getInfoFileWriter() != null) {
                this.configDir.getInfoFileWriter().write("\nDEPENDENCIES\n");
            }
            if ((featureModelFileName = this.getOptionValue("guide", "features").toString().trim()).equals("")) {
                OptionManager.instance().overloadOptionValue(this.optionContainerIndex, "guide", "features", this.getOptionValueString("singlemalt", "parsing_algorithm"));
                featureModelFileName = this.getOptionValue("guide", "features").toString().trim();
                String learner = this.getOptionValueString("guide", "learner");
                if (!learner.startsWith("lib")) {
                    learner = "lib" + learner;
                }
                featureModelFileName = featureModelFileName.replace("{learner}", learner);
                featureModelFileName = featureModelFileName.replace("{dataformat}", this.getOptionValue("input", "format").toString().trim().replace(".xml", ""));
                URLFinder f = new URLFinder();
                featureModelFileName = this.configDir.copyToConfig(f.findURLinJars(featureModelFileName));
            } else {
                featureModelFileName = this.configDir.copyToConfig(featureModelFileName);
            }
            OptionManager.instance().overloadOptionValue(this.optionContainerIndex, "guide", "features", featureModelFileName);
            if (this.configDir.getInfoFileWriter() != null) {
                this.configDir.getInfoFileWriter().write("--guide-features (  -F)                 " + this.getOptionValue("guide", "features").toString() + "\n");
            }
            if (this.getOptionValue("guide", "data_split_column").toString().equals("") && !this.getOptionValue("guide", "data_split_structure").toString().equals("")) {
                this.configLogger.warn("Option --guide-data_split_column = '' and --guide-data_split_structure != ''. Option --guide-data_split_structure is overloaded with '', this will cause the parser to induce a single model.\n ");
                OptionManager.instance().overloadOptionValue(this.optionContainerIndex, "guide", "data_split_structure", "");
                if (this.configDir.getInfoFileWriter() != null) {
                    this.configDir.getInfoFileWriter().write("--guide-data_split_structure (  -s)\n");
                }
            }
            if (!this.getOptionValue("guide", "data_split_column").toString().equals("") && this.getOptionValue("guide", "data_split_structure").toString().equals("")) {
                this.configLogger.warn("Option --guide-data_split_column != '' and --guide-data_split_structure = ''. Option --guide-data_split_column is overloaded with '', this will cause the parser to induce a single model.\n");
                OptionManager.instance().overloadOptionValue(this.optionContainerIndex, "guide", "data_split_column", "");
                if (this.configDir.getInfoFileWriter() != null) {
                    this.configDir.getInfoFileWriter().write("--guide-data_split_column (  -d)\n");
                }
            }
            String decisionSettings = this.getOptionValue("guide", "decision_settings").toString().trim();
            String markingStrategy = this.getOptionValue("pproj", "marking_strategy").toString().trim();
            String coveredRoot = this.getOptionValue("pproj", "covered_root").toString().trim();
            StringBuilder newDecisionSettings = new StringBuilder();
            decisionSettings = decisionSettings == null || decisionSettings.length() < 1 || decisionSettings.equals("default") ? "T.TRANS+A.DEPREL" : decisionSettings.toUpperCase();
            if ((markingStrategy.equalsIgnoreCase("head") || markingStrategy.equalsIgnoreCase("path") || markingStrategy.equalsIgnoreCase("head+path")) && !Pattern.matches(".*A\\.PPLIFTED.*", decisionSettings)) {
                newDecisionSettings.append("+A.PPLIFTED");
            }
            if ((markingStrategy.equalsIgnoreCase("path") || markingStrategy.equalsIgnoreCase("head+path")) && !Pattern.matches(".*A\\.PPPATH.*", decisionSettings)) {
                newDecisionSettings.append("+A.PPPATH");
            }
            if (!coveredRoot.equalsIgnoreCase("none") && !Pattern.matches(".*A\\.PPCOVERED.*", decisionSettings)) {
                newDecisionSettings.append("+A.PPCOVERED");
            }
            if (!this.getOptionValue("guide", "decision_settings").toString().equals(decisionSettings) || newDecisionSettings.length() > 0) {
                OptionManager.instance().overloadOptionValue(this.optionContainerIndex, "guide", "decision_settings", decisionSettings + newDecisionSettings.toString());
                if (this.configDir.getInfoFileWriter() != null) {
                    this.configDir.getInfoFileWriter().write("--guide-decision_settings (  -gds)                 " + this.getOptionValue("guide", "decision_settings").toString() + "\n");
                }
            }
            if (this.configDir.getInfoFileWriter() != null) {
                this.configDir.getInfoFileWriter().flush();
            }
        }
        catch (IOException e) {
            throw new ConfigurationException("Could not write to the configuration information file. ", e);
        }
    }

    private URL findURL(String propagationSpecFileName) throws MaltChainedException {
        URL url = null;
        File specFile = this.configDir.getFile(propagationSpecFileName);
        if (specFile.exists()) {
            try {
                url = new URL("file:///" + specFile.getAbsolutePath());
            }
            catch (MalformedURLException e) {
                throw new PropagationException("Malformed URL: " + specFile, e);
            }
        } else {
            url = this.configDir.getConfigFileEntryURL(propagationSpecFileName);
        }
        return url;
    }
}

