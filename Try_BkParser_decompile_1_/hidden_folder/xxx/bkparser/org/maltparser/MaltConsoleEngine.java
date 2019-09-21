package org.maltparser;

import java.util.Date;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.SystemInfo;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.options.OptionManager;

public class MaltConsoleEngine {
   public static final int OPTION_CONTAINER = 0;

   public MaltConsoleEngine() {
      try {
         OptionManager.instance().loadOptionDescriptionFile();
         OptionManager.instance().generateMaps();
      } catch (MaltChainedException var2) {
         if (SystemLogger.logger().isDebugEnabled()) {
            SystemLogger.logger().debug("", var2);
         } else {
            SystemLogger.logger().error(var2.getMessageChain());
         }

         System.exit(1);
      }

   }

   public void startEngine(String[] args) {
      try {
         OptionManager om = OptionManager.instance();
         boolean hasArg = om.parseCommandLine((String[])args, 0);
         String verbosity = null;
         if (hasArg) {
            verbosity = (String)OptionManager.instance().getOptionValue(0, "system", "verbosity");
         } else {
            verbosity = (String)OptionManager.instance().getOptionDefaultValue("system", "verbosity");
         }

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
      } catch (MaltChainedException var5) {
         if (SystemLogger.logger().isDebugEnabled()) {
            SystemLogger.logger().debug("", var5);
         } else {
            SystemLogger.logger().error(var5.getMessageChain());
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
