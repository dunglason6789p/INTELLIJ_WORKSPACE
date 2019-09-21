package org.maltparser.core.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.maltparser.core.flow.system.elem.ChartAttribute;
import org.maltparser.core.helper.SystemInfo;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.options.OptionManager;

public class ConfigDirChartItem extends ChartItem {
   private String idName;
   private String taskName;
   private String optionFileName;
   private URL configDirURL;
   private String configDirName;
   private ConfigurationDir configDir;
   private String outCharSet;
   private String inCharSet;

   public ConfigDirChartItem() {
   }

   public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
      super.initialize(flowChartinstance, chartItemSpecification);
      Iterator i$ = chartItemSpecification.getChartItemAttributes().keySet().iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         if (key.equals("id")) {
            this.idName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         } else if (key.equals("task")) {
            this.taskName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         }
      }

      if (this.idName == null) {
         this.idName = ((ChartAttribute)this.getChartElement("configdir").getAttributes().get("id")).getDefaultValue();
      } else if (this.taskName == null) {
         this.taskName = ((ChartAttribute)this.getChartElement("configdir").getAttributes().get("task")).getDefaultValue();
      }

      if (OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "config", "url") != null && OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "config", "url").toString().length() > 0) {
         try {
            this.configDirURL = new URL(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "config", "url").toString());
         } catch (MalformedURLException var5) {
            throw new ConfigurationException("The URL '" + OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "config", "url").toString() + "' is malformed. ", var5);
         }
      }

      if (OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "config", "name").toString().endsWith(".mco")) {
         int index = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "config", "name").toString().lastIndexOf(46);
         this.configDirName = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "config", "name").toString().substring(0, index);
      } else {
         this.configDirName = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "config", "name").toString();
      }

      try {
         if (OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "system", "option_file") != null) {
            this.optionFileName = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "system", "option_file").toString();
         }
      } catch (ConfigurationException var6) {
         throw new ConfigurationException("The option file '" + this.optionFileName + "' could not be copied. ", var6);
      }

      if (OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "output", "charset") != null) {
         this.outCharSet = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "output", "charset").toString();
      } else {
         this.outCharSet = "UTF-8";
      }

      if (OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "input", "charset") != null) {
         this.inCharSet = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "input", "charset").toString();
      } else {
         this.inCharSet = "UTF-8";
      }

      this.configDir = (ConfigurationDir)flowChartinstance.getFlowChartRegistry(ConfigurationDir.class, this.idName);
      if (this.configDir == null) {
         if (this.configDirURL != null) {
            this.configDir = new ConfigurationDir(this.configDirURL);
         } else {
            this.configDir = new ConfigurationDir(this.configDirName, this.idName, this.getOptionContainerIndex());
         }

         flowChartinstance.addFlowChartRegistry(ConfigurationDir.class, this.idName, this.configDir);
      }

      if (this.taskName.equals("versioning")) {
         this.configDir.versioning();
      } else if (this.taskName.equals("loadsavedoptions")) {
         this.configDir.initCreatedByMaltParserVersionFromInfoFile();
         if (this.configDir.getCreatedByMaltParserVersion() == null) {
            SystemLogger.logger().warn("Couln't determine which version of MaltParser that created the parser model: " + this.configDirName + ".mco\n MaltParser will terminate\n");
            System.exit(1);
         } else if (!this.configDir.getCreatedByMaltParserVersion().substring(0, 3).equals(SystemInfo.getVersion().substring(0, 3)) && (!this.configDir.getCreatedByMaltParserVersion().substring(0, 3).equals("1.7") && !this.configDir.getCreatedByMaltParserVersion().substring(0, 3).equals("1.8") && !this.configDir.getCreatedByMaltParserVersion().substring(0, 3).equals("1.9") || !SystemInfo.getVersion().substring(0, 3).equals("1.9"))) {
            SystemLogger.logger().error("The parser model '" + this.configDirName + ".mco' is created by MaltParser " + this.configDir.getCreatedByMaltParserVersion() + ".\n");
            SystemLogger.logger().error("You have to re-train the parser model to be able to parse with current version of MaltParser.\n");
            System.exit(1);
         }

         OptionManager.instance().loadOptions(this.getOptionContainerIndex(), this.configDir.getInputStreamReaderFromConfigFile("savedoptions.sop"));
         this.configDir.initDataFormat();
      } else if (this.taskName.equals("createdir")) {
         this.configDir.setCreatedByMaltParserVersion(SystemInfo.getVersion());
         this.configDir.createConfigDirectory();
         if (this.optionFileName != null && this.optionFileName.length() > 0) {
            this.configDir.copyToConfig(new File(this.optionFileName));
         }

         this.configDir.initDataFormat();
      }

   }

   public int preprocess(int signal) throws MaltChainedException {
      if (this.taskName.equals("unpack")) {
         SystemLogger.logger().info("Unpacking the parser model '" + this.configDirName + ".mco' ...\n");
         this.configDir.unpackConfigFile();
      } else if (this.taskName.equals("info")) {
         this.configDir.echoInfoFile();
      } else if (this.taskName.equals("loadsymboltables")) {
         this.configDir.getSymbolTables().load(this.configDir.getInputStreamReaderFromConfigFileEntry("symboltables.sym", this.inCharSet));
      }

      return signal;
   }

   public int process(int signal) throws MaltChainedException {
      return signal;
   }

   public int postprocess(int signal) throws MaltChainedException {
      if (this.taskName.equals("createfile")) {
         OptionManager.instance().saveOptions(this.getOptionContainerIndex(), this.configDir.getOutputStreamWriter("savedoptions.sop"));
         this.configDir.createConfigFile();
      } else if (this.taskName.equals("deletedir")) {
         this.configDir.terminate();
         this.configDir.deleteConfigDirectory();
      } else if (this.taskName.equals("savesymboltables")) {
         this.configDir.getSymbolTables().save(this.configDir.getOutputStreamWriter("symboltables.sym", this.outCharSet));
      }

      return signal;
   }

   public void terminate() throws MaltChainedException {
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return this.getClass() != obj.getClass() ? false : obj.toString().equals(this.toString());
      }
   }

   public int hashCode() {
      return 217 + (null == this.toString() ? 0 : this.toString().hashCode());
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("    configdir ");
      sb.append("id:");
      sb.append(this.idName);
      sb.append(' ');
      sb.append("task:");
      sb.append(this.taskName);
      return sb.toString();
   }
}