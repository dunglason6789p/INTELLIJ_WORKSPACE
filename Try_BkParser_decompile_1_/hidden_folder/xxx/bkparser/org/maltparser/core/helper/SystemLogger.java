package org.maltparser.core.helper;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class SystemLogger {
   private static SystemLogger uniqueInstance = new SystemLogger();
   private static Logger systemLogger;
   private Level systemVerbosityLevel;
   private ConsoleAppender consoleAppender;

   private SystemLogger() {
      systemLogger = Logger.getLogger("System");
      this.consoleAppender = new ConsoleAppender(new PatternLayout("%m"), "System.err");
      this.consoleAppender.setEncoding("UTF-16");
      systemLogger.addAppender(this.consoleAppender);
      if (System.getProperty("Malt.verbosity") != null) {
         this.setSystemVerbosityLevel(System.getProperty("Malt.verbosity").toUpperCase());
      } else {
         this.setSystemVerbosityLevel("INFO");
      }

   }

   public static SystemLogger instance() {
      return uniqueInstance;
   }

   public static Logger logger() {
      return systemLogger;
   }

   public Level getSystemVerbosityLevel() {
      return this.systemVerbosityLevel;
   }

   public void setSystemVerbosityLevel(String verbosity) {
      this.systemVerbosityLevel = Level.toLevel(verbosity, Level.INFO);
      this.consoleAppender.setThreshold(this.systemVerbosityLevel);
      systemLogger.setLevel(this.systemVerbosityLevel);
   }
}
