/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser;

import java.util.Date;
import org.maltparser.Engine;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.helper.SystemInfo;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.options.OptionDescriptions;
import org.maltparser.core.options.OptionManager;

public class MaltConsoleEngine {
    public static final int OPTION_CONTAINER = 0;

    public MaltConsoleEngine() {
        try {
            OptionManager.instance().loadOptionDescriptionFile();
            OptionManager.instance().generateMaps();
        }
        catch (MaltChainedException e) {
            if (SystemLogger.logger().isDebugEnabled()) {
                SystemLogger.logger().debug("", e);
            } else {
                SystemLogger.logger().error(e.getMessageChain());
            }
            System.exit(1);
        }
    }

    public void startEngine(String[] args) {
        try {
            OptionManager om = OptionManager.instance();
            boolean hasArg = om.parseCommandLine(args, 0);
            String verbosity = null;
            verbosity = hasArg ? (String)OptionManager.instance().getOptionValue(0, "system", "verbosity") : (String)OptionManager.instance().getOptionDefaultValue("system", "verbosity");
            if (verbosity != null) {
                SystemLogger.instance().setSystemVerbosityLevel(verbosity.toUpperCase());
            }
            if (!hasArg || om.getNumberOfOptionValues(0) == 0) {
                SystemLogger.logger().info(SystemInfo.header());
                SystemLogger.logger().info(SystemInfo.shortHelp());
                return;
            }
            if (om.getOptionValue(0, "system", "help") != null) {
                SystemLogger.logger().info(SystemInfo.header());
                SystemLogger.logger().info(om.getOptionDescriptions());
                return;
            }
            if (om.getOptionValue(0, "system", "option_file") != null && om.getOptionValue(0, "system", "option_file").toString().length() > 0) {
                om.parseOptionInstanceXMLfile((String)om.getOptionValue(0, "system", "option_file"));
            }
            this.maltParser();
        }
        catch (MaltChainedException e) {
            if (SystemLogger.logger().isDebugEnabled()) {
                SystemLogger.logger().debug("", e);
            } else {
                SystemLogger.logger().error(e.getMessageChain());
            }
            System.exit(1);
        }
    }

    private void maltParser() throws MaltChainedException {
        if (SystemLogger.logger() != null && SystemLogger.logger().isInfoEnabled()) {
            SystemLogger.logger().info(SystemInfo.header() + "\n");
            SystemLogger.logger().info("Started: " + new Date(System.currentTimeMillis()) + "\n");
        }
        Engine engine = new Engine();
        engine.initialize(0);
        engine.process(0);
        engine.terminate(0);
        if (SystemLogger.logger().isInfoEnabled()) {
            SystemLogger.logger().info("Finished: " + new Date(System.currentTimeMillis()) + "\n");
        }
    }
}

