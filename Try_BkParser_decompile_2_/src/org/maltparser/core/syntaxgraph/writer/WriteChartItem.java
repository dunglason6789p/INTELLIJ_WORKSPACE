/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.writer;

import java.io.OutputStream;
import java.io.PrintStream;
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
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.io.dataformat.DataFormatManager;
import org.maltparser.core.io.dataformat.DataFormatSpecification;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.writer.SyntaxGraphWriter;

public class WriteChartItem
extends ChartItem {
    private String idName;
    private String outputFormatName;
    private String outputFileName;
    private String outputCharSet;
    private String writerOptions;
    private Class<? extends SyntaxGraphWriter> graphWriterClass;
    private String nullValueStrategy;
    private SyntaxGraphWriter writer;
    private String sourceName;
    private String optiongroupName;
    private DataFormatInstance outputDataFormatInstance;
    private TokenStructure cachedGraph = null;

    @Override
    public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
        super.initialize(flowChartinstance, chartItemSpecification);
        for (String key : chartItemSpecification.getChartItemAttributes().keySet()) {
            if (key.equals("id")) {
                this.idName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (key.equals("source")) {
                this.sourceName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (!key.equals("optiongroup")) continue;
            this.optiongroupName = chartItemSpecification.getChartItemAttributes().get(key);
        }
        if (this.idName == null) {
            this.idName = this.getChartElement("write").getAttributes().get("id").getDefaultValue();
        } else if (this.sourceName == null) {
            this.sourceName = this.getChartElement("write").getAttributes().get("source").getDefaultValue();
        } else if (this.optiongroupName == null) {
            this.optiongroupName = this.getChartElement("write").getAttributes().get("optiongroup").getDefaultValue();
        }
        this.setOutputFormatName(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), this.optiongroupName, "format").toString());
        this.setOutputFileName(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), this.optiongroupName, "outfile").toString());
        this.setOutputCharSet(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), this.optiongroupName, "charset").toString());
        this.setWriterOptions(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), this.optiongroupName, "writer_options").toString());
        this.setSyntaxGraphWriterClass((Class)OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), this.optiongroupName, "writer"));
        this.setNullValueStrategy(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "singlemalt", "null_value").toString());
        this.initOutput(this.getNullValueStrategy());
        this.initWriter(this.getSyntaxGraphWriterClass(), this.getOutputFileName(), this.getOutputCharSet(), this.getWriterOptions());
    }

    @Override
    public int preprocess(int signal) throws MaltChainedException {
        return signal;
    }

    @Override
    public int process(int signal) throws MaltChainedException {
        if (this.cachedGraph == null) {
            this.cachedGraph = (TokenStructure)this.flowChartinstance.getFlowChartRegistry(TokenStructure.class, this.sourceName);
            this.writer.writeProlog();
        }
        this.writer.writeSentence(this.cachedGraph);
        if (signal == 2) {
            this.writer.writeEpilog();
        }
        return signal;
    }

    @Override
    public int postprocess(int signal) throws MaltChainedException {
        return signal;
    }

    @Override
    public void terminate() throws MaltChainedException {
        if (this.writer != null) {
            this.writer.close();
            this.writer = null;
        }
        this.outputDataFormatInstance = null;
        this.cachedGraph = null;
    }

    public String getOutputFormatName() {
        if (this.outputFormatName == null) {
            return "/appdata/dataformat/conllx.xml";
        }
        return this.outputFormatName;
    }

    public void setOutputFormatName(String outputFormatName) {
        this.outputFormatName = outputFormatName;
    }

    public String getOutputFileName() {
        if (this.outputFileName == null) {
            return "/dev/stdout";
        }
        return this.outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public String getOutputCharSet() {
        if (this.outputCharSet == null) {
            return "UTF-8";
        }
        return this.outputCharSet;
    }

    public void setOutputCharSet(String outputCharSet) {
        this.outputCharSet = outputCharSet;
    }

    public String getWriterOptions() {
        if (this.writerOptions == null) {
            return "";
        }
        return this.writerOptions;
    }

    public void setWriterOptions(String writerOptions) {
        this.writerOptions = writerOptions;
    }

    public Class<? extends SyntaxGraphWriter> getSyntaxGraphWriterClass() {
        return this.graphWriterClass;
    }

    public void setSyntaxGraphWriterClass(Class<?> graphWriterClass) throws MaltChainedException {
        try {
            if (graphWriterClass != null) {
                this.graphWriterClass = graphWriterClass.asSubclass(SyntaxGraphWriter.class);
            }
        }
        catch (ClassCastException e) {
            throw new DataFormatException("The class '" + graphWriterClass.getName() + "' is not a subclass of '" + SyntaxGraphWriter.class.getName() + "'. ", e);
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

    public void initOutput(String nullValueStategy) throws MaltChainedException {
        ConfigurationDir configDir = (ConfigurationDir)this.flowChartinstance.getFlowChartRegistry(ConfigurationDir.class, this.idName);
        DataFormatManager dataFormatManager = configDir.getDataFormatManager();
        SymbolTableHandler symbolTables = configDir.getSymbolTables();
        if (configDir.sizeDataFormatInstance() == 0 || dataFormatManager.getInputDataFormatSpec() != dataFormatManager.getOutputDataFormatSpec()) {
            this.outputDataFormatInstance = dataFormatManager.getOutputDataFormatSpec().createDataFormatInstance(symbolTables, nullValueStategy);
            configDir.addDataFormatInstance(dataFormatManager.getInputDataFormatSpec().getDataFormatName(), this.outputDataFormatInstance);
        } else {
            this.outputDataFormatInstance = configDir.getDataFormatInstance(dataFormatManager.getInputDataFormatSpec().getDataFormatName());
        }
    }

    public void initWriter(Class<? extends SyntaxGraphWriter> syntaxGraphWriterClass, String outputFile, String outputCharSet, String writerOption) throws MaltChainedException {
        try {
            this.writer = syntaxGraphWriterClass.newInstance();
            if (outputFile == null || outputFile.length() == 0 || outputFile.equals("/dev/stdout")) {
                this.writer.open(System.out, outputCharSet);
            } else {
                this.writer.open(outputFile, outputCharSet);
            }
            this.writer.setDataFormatInstance(this.outputDataFormatInstance);
            this.writer.setOptions(writerOption);
        }
        catch (InstantiationException e) {
            throw new DataFormatException("The data writer '" + syntaxGraphWriterClass.getName() + "' cannot be initialized. ", e);
        }
        catch (IllegalAccessException e) {
            throw new DataFormatException("The data writer '" + syntaxGraphWriterClass.getName() + "' cannot be initialized. ", e);
        }
    }

    public Class<? extends SyntaxGraphWriter> getGraphWriterClass() {
        return this.graphWriterClass;
    }

    public SyntaxGraphWriter getWriter() {
        return this.writer;
    }

    public String getSourceName() {
        return this.sourceName;
    }

    public DataFormatInstance getOutputDataFormatInstance() {
        return this.outputDataFormatInstance;
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
        sb.append("    write ");
        sb.append("id:");
        sb.append(this.idName);
        sb.append(' ');
        sb.append("source:");
        sb.append(this.sourceName);
        sb.append(' ');
        sb.append("optiongroup:");
        sb.append(this.optiongroupName);
        return sb.toString();
    }
}

