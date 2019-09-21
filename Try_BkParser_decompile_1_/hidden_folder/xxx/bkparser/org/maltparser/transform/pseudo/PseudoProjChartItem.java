package org.maltparser.transform.pseudo;

import java.util.Iterator;
import org.maltparser.core.config.ConfigurationDir;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.maltparser.core.flow.system.elem.ChartAttribute;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;

public class PseudoProjChartItem extends ChartItem {
   private String idName;
   private String targetName;
   private String sourceName;
   private String taskName;
   private String marking_strategy;
   private String covered_root;
   private String lifting_order;
   private PseudoProjectivity pproj;
   private boolean pprojActive = false;
   private TokenStructure cachedGraph = null;

   public PseudoProjChartItem() {
   }

   public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
      super.initialize(flowChartinstance, chartItemSpecification);
      Iterator i$ = chartItemSpecification.getChartItemAttributes().keySet().iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         if (key.equals("target")) {
            this.targetName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         } else if (key.equals("source")) {
            this.sourceName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         } else if (key.equals("id")) {
            this.idName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         } else if (key.equals("task")) {
            this.taskName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         }
      }

      if (this.targetName == null) {
         this.targetName = ((ChartAttribute)this.getChartElement("pseudoproj").getAttributes().get("target")).getDefaultValue();
      } else if (this.sourceName == null) {
         this.sourceName = ((ChartAttribute)this.getChartElement("pseudoproj").getAttributes().get("source")).getDefaultValue();
      } else if (this.idName == null) {
         this.idName = ((ChartAttribute)this.getChartElement("pseudoproj").getAttributes().get("id")).getDefaultValue();
      } else if (this.taskName == null) {
         this.taskName = ((ChartAttribute)this.getChartElement("pseudoproj").getAttributes().get("task")).getDefaultValue();
      }

      PseudoProjectivity tmppproj = (PseudoProjectivity)flowChartinstance.getFlowChartRegistry(PseudoProjectivity.class, this.idName);
      if (tmppproj == null) {
         this.pproj = new PseudoProjectivity();
         flowChartinstance.addFlowChartRegistry(PseudoProjectivity.class, this.idName, this.pproj);
      } else {
         this.pproj = tmppproj;
      }

   }

   public int preprocess(int signal) throws MaltChainedException {
      if (this.taskName.equals("init")) {
         ConfigurationDir configDir = (ConfigurationDir)this.flowChartinstance.getFlowChartRegistry(ConfigurationDir.class, this.idName);
         DataFormatInstance dataFormatInstance = configDir.getInputDataFormatInstance();
         this.marking_strategy = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "pproj", "marking_strategy").toString().trim();
         this.covered_root = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "pproj", "covered_root").toString().trim();
         this.lifting_order = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "pproj", "lifting_order").toString().trim();
         if (!this.marking_strategy.equalsIgnoreCase("none") || !this.covered_root.equalsIgnoreCase("none")) {
            this.pproj.initialize(this.marking_strategy, this.covered_root, this.lifting_order, SystemLogger.logger(), dataFormatInstance, configDir.getSymbolTables());
         }

         if (!this.marking_strategy.equalsIgnoreCase("none") || !this.covered_root.equalsIgnoreCase("none")) {
            this.pprojActive = true;
         }
      }

      return signal;
   }

   public int process(int signal) throws MaltChainedException {
      if (this.cachedGraph == null) {
         this.marking_strategy = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "pproj", "marking_strategy").toString().trim();
         this.covered_root = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "pproj", "covered_root").toString().trim();
         this.lifting_order = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "pproj", "lifting_order").toString().trim();
         this.cachedGraph = (TokenStructure)this.flowChartinstance.getFlowChartRegistry(TokenStructure.class, this.sourceName);
         if (!this.marking_strategy.equalsIgnoreCase("none") || !this.covered_root.equalsIgnoreCase("none")) {
            this.pprojActive = true;
         }
      }

      if (this.pprojActive && this.cachedGraph instanceof DependencyStructure) {
         if (this.taskName.equals("proj")) {
            this.pproj.projectivize((DependencyStructure)this.cachedGraph);
         } else if (this.taskName.equals("merge")) {
            this.pproj.mergeArclabels((DependencyStructure)this.cachedGraph);
         } else if (this.taskName.equals("deproj")) {
            this.pproj.deprojectivize((DependencyStructure)this.cachedGraph);
         } else if (this.taskName.equals("split")) {
            this.pproj.splitArclabels((DependencyStructure)this.cachedGraph);
         }
      }

      return signal;
   }

   public int postprocess(int signal) throws MaltChainedException {
      return signal;
   }

   public void terminate() throws MaltChainedException {
      this.pproj = null;
      this.pprojActive = false;
      this.cachedGraph = null;
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
      sb.append("    pseudoproj ");
      sb.append("id:");
      sb.append(this.idName);
      sb.append(' ');
      sb.append("task:");
      sb.append(this.taskName);
      sb.append(' ');
      sb.append("source:");
      sb.append(this.sourceName);
      sb.append(' ');
      sb.append("target:");
      sb.append(this.targetName);
      return sb.toString();
   }
}
