/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.reader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import org.maltparser.core.config.ConfigurationDir;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.maltparser.core.flow.system.elem.ChartAttribute;
import org.maltparser.core.flow.system.elem.ChartElement;
import org.maltparser.core.helper.URLFinder;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.io.dataformat.DataFormatManager;
import org.maltparser.core.io.dataformat.DataFormatSpecification;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.reader.SyntaxGraphReader;

public class ReadChartItem
extends ChartItem {
    private String idName;
    private String inputFormatName;
    private String inputFileName;
    private String inputCharSet;
    private String readerOptions;
    private int iterations;
    private Class<? extends SyntaxGraphReader> graphReaderClass;
    private String nullValueStrategy;
    private SyntaxGraphReader reader;
    private String targetName;
    private String optiongroupName;
    private DataFormatInstance inputDataFormatInstance;
    private TokenStructure cachedGraph = null;

    @Override
    public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
        super.initialize(flowChartinstance, chartItemSpecification);
        for (String key : chartItemSpecification.getChartItemAttributes().keySet()) {
            if (key.equals("id")) {
                this.idName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (key.equals("target")) {
                this.targetName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (!key.equals("optiongroup")) continue;
            this.optiongroupName = chartItemSpecification.getChartItemAttributes().get(key);
        }
        if (this.idName == null) {
            this.idName = this.getChartElement("read").getAttributes().get("id").getDefaultValue();
        } else if (this.targetName == null) {
            this.targetName = this.getChartElement("read").getAttributes().get("target").getDefaultValue();
        } else if (this.optiongroupName == null) {
            this.optiongroupName = this.getChartElement("read").getAttributes().get("optiongroup").getDefaultValue();
        }
        this.setInputFormatName(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), this.optiongroupName, "format").toString());
        this.setInputFileName(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), this.optiongroupName, "infile").toString());
        this.setInputCharSet(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), this.optiongroupName, "charset").toString());
        this.setReaderOptions(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), this.optiongroupName, "reader_options").toString());
        if (OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), this.optiongroupName, "iterations") != null) {
            this.setIterations((Integer)OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), this.optiongroupName, "iterations"));
        } else {
            this.setIterations(1);
        }
        this.setSyntaxGraphReaderClass((Class)OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), this.optiongroupName, "reader"));
        this.setNullValueStrategy(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "singlemalt", "null_value").toString());
        this.initInput(this.getNullValueStrategy());
        this.initReader(this.getSyntaxGraphReaderClass(), this.getInputFileName(), this.getInputCharSet(), this.getReaderOptions(), this.iterations);
    }

    @Override
    public int preprocess(int signal) throws MaltChainedException {
        return signal;
    }

    @Override
    public int process(int signal) throws MaltChainedException {
        if (this.cachedGraph == null) {
            this.cachedGraph = (TokenStructure)this.flowChartinstance.getFlowChartRegistry(TokenStructure.class, this.targetName);
        }
        int prevIterationCounter = this.reader.getIterationCounter();
        boolean moreInput = this.reader.readSentence(this.cachedGraph);
        if (!moreInput) {
            return 2;
        }
        if (prevIterationCounter < this.reader.getIterationCounter()) {
            return 3;
        }
        return 1;
    }

    @Override
    public int postprocess(int signal) throws MaltChainedException {
        return signal;
    }

    @Override
    public void terminate() throws MaltChainedException {
        if (this.reader != null) {
            this.reader.close();
            this.reader = null;
        }
        this.cachedGraph = null;
        this.inputDataFormatInstance = null;
    }

    public String getInputFormatName() {
        if (this.inputFormatName == null) {
            return "/appdata/dataformat/conllx.xml";
        }
        return this.inputFormatName;
    }

    public void setInputFormatName(String inputFormatName) {
        this.inputFormatName = inputFormatName;
    }

    public String getInputFileName() {
        if (this.inputFileName == null) {
            return "/dev/stdin";
        }
        return this.inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public String getInputCharSet() {
        if (this.inputCharSet == null) {
            return "UTF-8";
        }
        return this.inputCharSet;
    }

    public void setInputCharSet(String inputCharSet) {
        this.inputCharSet = inputCharSet;
    }

    public String getReaderOptions() {
        if (this.readerOptions == null) {
            return "";
        }
        return this.readerOptions;
    }

    public void setReaderOptions(String readerOptions) {
        this.readerOptions = readerOptions;
    }

    public int getIterations() {
        return this.iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public Class<? extends SyntaxGraphReader> getSyntaxGraphReaderClass() {
        return this.graphReaderClass;
    }

    public void setSyntaxGraphReaderClass(Class<?> graphReaderClass) throws MaltChainedException {
        try {
            if (graphReaderClass != null) {
                this.graphReaderClass = graphReaderClass.asSubclass(SyntaxGraphReader.class);
            }
        }
        catch (ClassCastException e) {
            throw new DataFormatException("The class '" + graphReaderClass.getName() + "' is not a subclass of '" + SyntaxGraphReader.class.getName() + "'. ", e);
        }
    }

    public String getNullValueStrategy() {
        if (this.nullValueStrategy == null) {
            return "one";
        }
        return this.nullValueStrategy;
    }

    public void setNullValueStrategy(String nullValueStrategy) {
        this.nullValueStrategy = nullValueStrategy;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public SyntaxGraphReader getReader() {
        return this.reader;
    }

    public DataFormatInstance getInputDataFormatInstance() {
        return this.inputDataFormatInstance;
    }

    public void initInput(String nullValueStategy) throws MaltChainedException {
        ConfigurationDir configDir = (ConfigurationDir)this.flowChartinstance.getFlowChartRegistry(ConfigurationDir.class, this.idName);
        DataFormatManager dataFormatManager = configDir.getDataFormatManager();
        SymbolTableHandler symbolTables = configDir.getSymbolTables();
        this.inputDataFormatInstance = dataFormatManager.getInputDataFormatSpec().createDataFormatInstance(symbolTables, nullValueStategy);
        configDir.addDataFormatInstance(dataFormatManager.getInputDataFormatSpec().getDataFormatName(), this.inputDataFormatInstance);
    }

    public void initReader(Class<? extends SyntaxGraphReader> syntaxGraphReader, String inputFile, String inputCharSet, String readerOptions, int iterations) throws MaltChainedException {
        try {
            URLFinder f = new URLFinder();
            this.reader = syntaxGraphReader.newInstance();
            if (inputFile == null || inputFile.length() == 0 || inputFile.equals("/dev/stdin")) {
                this.reader.open(System.in, inputCharSet);
            } else if (new File(inputFile).exists()) {
                this.reader.setNIterations(iterations);
                this.reader.open(inputFile, inputCharSet);
            } else {
                this.reader.setNIterations(iterations);
                this.reader.open(f.findURL(inputFile), inputCharSet);
            }
            this.reader.setDataFormatInstance(this.inputDataFormatInstance);
            this.reader.setOptions(readerOptions);
        }
        catch (InstantiationException e) {
            throw new DataFormatException("The data reader '" + syntaxGraphReader.getName() + "' cannot be initialized. ", e);
        }
        catch (IllegalAccessException e) {
            throw new DataFormatException("The data reader '" + syntaxGraphReader.getName() + "' cannot be initialized. ", e);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        return obj.toString().equals(this.toString());
    }

    public int hashCode() {
        return 217 + (null == this.toString() ? 0 : this.toString().hashCode());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("    read ");
        sb.append("id:");
        sb.append(this.idName);
        sb.append(' ');
        sb.append("target:");
        sb.append(this.targetName);
        sb.append(' ');
        sb.append("optiongroup:");
        sb.append(this.optiongroupName);
        return sb.toString();
    }
}

