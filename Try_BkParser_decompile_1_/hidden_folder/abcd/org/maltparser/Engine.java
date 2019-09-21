/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser;

import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.flow.FlowChartManager;
import org.maltparser.core.flow.system.FlowChartSystem;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.helper.Util;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.plugin.PluginLoader;

public class Engine {
    private final long startTime = System.currentTimeMillis();
    private final FlowChartManager flowChartManager = new FlowChartManager();
    private final SortedMap<Integer, FlowChartInstance> flowChartInstances;

    public Engine() throws MaltChainedException {
        this.flowChartManager.getFlowChartSystem().load(this.getClass().getResource("/appdata/flow/flowchartsystem.xml"));
        this.flowChartManager.getFlowChartSystem().load(PluginLoader.instance());
        this.flowChartManager.load(this.getClass().getResource("/appdata/flow/flowcharts.xml"));
        this.flowChartManager.load(PluginLoader.instance());
        this.flowChartInstances = new TreeMap<Integer, FlowChartInstance>();
    }

    public FlowChartInstance initialize(int optionContainerIndex) throws MaltChainedException {
        String flowChartName = null;
        if (OptionManager.instance().getOptionValueNoDefault(optionContainerIndex, "config", "flowchart") != null) {
            flowChartName = OptionManager.instance().getOptionValue(optionContainerIndex, "config", "flowchart").toString();
        }
        if (flowChartName == null) {
            if (OptionManager.instance().getOptionValueNoDefault(optionContainerIndex, "singlemalt", "mode") != null) {
                flowChartName = OptionManager.instance().getOptionValue(optionContainerIndex, "singlemalt", "mode").toString();
                OptionManager.instance().overloadOptionValue(optionContainerIndex, "config", "flowchart", flowChartName);
            } else {
                flowChartName = OptionManager.instance().getOptionValue(optionContainerIndex, "config", "flowchart").toString();
            }
        }
        FlowChartInstance flowChartInstance = this.flowChartManager.initialize(optionContainerIndex, flowChartName);
        this.flowChartInstances.put(optionContainerIndex, flowChartInstance);
        return flowChartInstance;
    }

    public void process(int optionContainerIndex) throws MaltChainedException {
        FlowChartInstance flowChartInstance = (FlowChartInstance)this.flowChartInstances.get(optionContainerIndex);
        if (flowChartInstance.hasPreProcessChartItems()) {
            flowChartInstance.preprocess();
        }
        if (flowChartInstance.hasProcessChartItems()) {
            int signal = 1;
            int tic = 0;
            int sentenceCounter = 0;
            int nIteration = 1;
            flowChartInstance.setEngineRegistry("iterations", nIteration);
            System.gc();
            while (signal != 2) {
                signal = flowChartInstance.process();
                if (signal == 1) {
                    ++sentenceCounter;
                } else if (signal == 3) {
                    SystemLogger.logger().info("\n=== END ITERATION " + nIteration + " ===\n");
                    flowChartInstance.setEngineRegistry("iterations", ++nIteration);
                }
                if (sentenceCounter < 101 && sentenceCounter == 1 || sentenceCounter == 10 || sentenceCounter == 100) {
                    Util.startTicer(SystemLogger.logger(), this.startTime, 10, sentenceCounter);
                }
                if (sentenceCounter % 100 != 0) continue;
                tic = Util.simpleTicer(SystemLogger.logger(), this.startTime, 10, tic, sentenceCounter);
            }
            Util.endTicer(SystemLogger.logger(), this.startTime, 10, tic, sentenceCounter);
        }
        if (flowChartInstance.hasPostProcessChartItems()) {
            flowChartInstance.postprocess();
        }
    }

    public void terminate(int optionContainerIndex) throws MaltChainedException {
        ((FlowChartInstance)this.flowChartInstances.get(optionContainerIndex)).terminate();
    }
}

