/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.SortedSet;
import org.maltparser.core.config.ConfigurationDir;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.maltparser.core.flow.system.elem.ChartAttribute;
import org.maltparser.core.flow.system.elem.ChartElement;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class GraphAnalyzerChartItem
extends ChartItem {
    private String idName;
    private String sourceName;
    private String task;
    private ConfigurationDir configDir;
    private DependencyStructure cachedSource = null;
    private BufferedWriter writer;
    private boolean closeStream = true;
    private int graphCounter = 1;

    @Override
    public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
        super.initialize(flowChartinstance, chartItemSpecification);
        for (String key : chartItemSpecification.getChartItemAttributes().keySet()) {
            if (key.equals("id")) {
                this.idName = chartItemSpecification.getChartItemAttributes().get(key);
                continue;
            }
            if (!key.equals("source")) continue;
            this.sourceName = chartItemSpecification.getChartItemAttributes().get(key);
        }
        if (this.idName == null) {
            this.idName = this.getChartElement("analyzer").getAttributes().get("id").getDefaultValue();
        } else if (this.sourceName == null) {
            this.sourceName = this.getChartElement("analyzer").getAttributes().get("source").getDefaultValue();
        }
        this.task = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "analyzer", "task").toString();
        this.configDir = (ConfigurationDir)flowChartinstance.getFlowChartRegistry(ConfigurationDir.class, this.idName);
        this.open(this.task + ".dat", OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "input", "charset").toString());
    }

    @Override
    public int preprocess(int signal) throws MaltChainedException {
        return signal;
    }

    @Override
    public int process(int signal) throws MaltChainedException {
        if (this.task.equals("projectivity")) {
            if (this.cachedSource == null) {
                this.cachedSource = (DependencyStructure)this.flowChartinstance.getFlowChartRegistry(DependencyStructure.class, this.sourceName);
            }
            try {
                this.writer.append("graph # ");
                this.writer.append(Integer.toString(this.graphCounter));
                this.writer.append('\n');
                Iterator i$ = this.cachedSource.getTokenIndices().iterator();
                while (i$.hasNext()) {
                    int index = (Integer)i$.next();
                    DependencyNode node = this.cachedSource.getDependencyNode(index);
                    this.writer.append(Integer.toString(node.getIndex()));
                    this.writer.append('\t');
                    this.writer.append(Integer.toString(node.getHead().getIndex()));
                    this.writer.append('\t');
                    this.writer.append('#');
                    this.writer.append('\t');
                    if (node.isProjective()) {
                        this.writer.append("@P");
                    } else {
                        this.writer.append("@N");
                    }
                    this.writer.append('\n');
                }
                this.writer.append('\n');
            }
            catch (IOException e) {
                throw new MaltChainedException("", e);
            }
            ++this.graphCounter;
        }
        return signal;
    }

    @Override
    public int postprocess(int signal) throws MaltChainedException {
        return signal;
    }

    @Override
    public void terminate() throws MaltChainedException {
        this.cachedSource = null;
        this.close();
    }

    private void open(String fileName, String charsetName) throws MaltChainedException {
        try {
            this.open(new OutputStreamWriter((OutputStream)new FileOutputStream(fileName), charsetName));
        }
        catch (FileNotFoundException e) {
            throw new DataFormatException("The output file '" + fileName + "' cannot be found.", e);
        }
        catch (UnsupportedEncodingException e) {
            throw new DataFormatException("The character encoding set '" + charsetName + "' isn't supported.", e);
        }
    }

    private void open(OutputStreamWriter osw) throws MaltChainedException {
        this.setWriter(new BufferedWriter(osw));
    }

    private void setWriter(BufferedWriter writer) throws MaltChainedException {
        this.close();
        this.writer = writer;
    }

    private void close() throws MaltChainedException {
        try {
            if (this.writer != null) {
                this.writer.flush();
                if (this.closeStream) {
                    this.writer.close();
                }
                this.writer = null;
            }
        }
        catch (IOException e) {
            throw new DataFormatException("Could not close the output file. ", e);
        }
    }
}

