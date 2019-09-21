package org.maltparser.core.syntaxgraph;

import java.util.Iterator;
import org.maltparser.core.config.ConfigurationDir;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.flow.FlowException;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.maltparser.core.flow.system.elem.ChartAttribute;
import org.maltparser.core.helper.HashSet;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.io.dataformat.DataFormatManager;
import org.maltparser.core.io.dataformat.DataFormatSpecification;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.ds2ps.LosslessMapping;

public class SyntaxGraphChartItem extends ChartItem {
   private String idName;
   private String structureName;
   private String taskName;
   private TokenStructure graph;

   public SyntaxGraphChartItem() {
   }

   public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
      super.initialize(flowChartinstance, chartItemSpecification);
      Iterator i$ = chartItemSpecification.getChartItemAttributes().keySet().iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         if (key.equals("id")) {
            this.idName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         } else if (key.equals("structure")) {
            this.structureName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         } else if (key.equals("task")) {
            this.taskName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         }
      }

      if (this.idName == null) {
         this.idName = ((ChartAttribute)this.getChartElement("graph").getAttributes().get("id")).getDefaultValue();
      } else if (this.structureName == null) {
         this.structureName = ((ChartAttribute)this.getChartElement("graph").getAttributes().get("structure")).getDefaultValue();
      } else if (this.taskName == null) {
         this.taskName = ((ChartAttribute)this.getChartElement("graph").getAttributes().get("task")).getDefaultValue();
      }

   }

   public int preprocess(int signal) throws MaltChainedException {
      if (this.taskName.equals("create")) {
         boolean phrase = false;
         boolean dependency = false;
         ConfigurationDir configDir = (ConfigurationDir)this.flowChartinstance.getFlowChartRegistry(ConfigurationDir.class, this.idName);
         DataFormatInstance dataFormatInstance = null;
         DataFormatManager dataFormatManager = configDir.getDataFormatManager();
         SymbolTableHandler symbolTables = configDir.getSymbolTables();
         Iterator i$ = configDir.getDataFormatInstanceKeys().iterator();

         String nullValueStategy;
         DataFormatInstance mapping;
         while(i$.hasNext()) {
            nullValueStategy = (String)i$.next();
            mapping = configDir.getDataFormatInstance(nullValueStategy);
            if (mapping.getDataFormarSpec().getDataStructure() == DataFormatSpecification.DataStructure.PHRASE) {
               phrase = true;
            }

            if (mapping.getDataFormarSpec().getDataStructure() == DataFormatSpecification.DataStructure.DEPENDENCY) {
               dependency = true;
               dataFormatInstance = mapping;
            }
         }

         if (!dependency && OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "config", "flowchart").toString().equals("learn")) {
            dependency = true;
            HashSet<DataFormatSpecification.Dependency> deps = dataFormatManager.getInputDataFormatSpec().getDependencies();
            nullValueStategy = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "singlemalt", "null_value").toString();
            Iterator i$ = deps.iterator();

            while(i$.hasNext()) {
               DataFormatSpecification.Dependency dep = (DataFormatSpecification.Dependency)i$.next();
               dataFormatInstance = dataFormatManager.getDataFormatSpec(dep.getDependentOn()).createDataFormatInstance(symbolTables, nullValueStategy);
               configDir.addDataFormatInstance(dataFormatManager.getOutputDataFormatSpec().getDataFormatName(), dataFormatInstance);
            }
         }

         if (dependency && !phrase) {
            this.graph = new DependencyGraph(symbolTables);
            this.flowChartinstance.addFlowChartRegistry(DependencyStructure.class, this.structureName, this.graph);
         } else if (dependency && phrase) {
            this.graph = new MappablePhraseStructureGraph(symbolTables);
            DataFormatInstance inFormat = configDir.getDataFormatInstance(dataFormatManager.getInputDataFormatSpec().getDataFormatName());
            DataFormatInstance outFormat = configDir.getDataFormatInstance(dataFormatManager.getOutputDataFormatSpec().getDataFormatName());
            if (inFormat == null || outFormat == null) {
               throw new FlowException("Couldn't determine the input and output data format. ");
            }

            mapping = null;
            LosslessMapping mapping;
            if (inFormat.getDataFormarSpec().getDataStructure() == DataFormatSpecification.DataStructure.DEPENDENCY) {
               mapping = new LosslessMapping(inFormat, outFormat, symbolTables);
            } else {
               mapping = new LosslessMapping(outFormat, inFormat, symbolTables);
            }

            if (inFormat.getDataFormarSpec().getDataStructure() == DataFormatSpecification.DataStructure.PHRASE) {
               mapping.setHeadRules(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "graph", "head_rules").toString());
            }

            ((MappablePhraseStructureGraph)this.graph).setMapping(mapping);
            this.flowChartinstance.addFlowChartRegistry(DependencyStructure.class, this.structureName, this.graph);
            this.flowChartinstance.addFlowChartRegistry(PhraseStructure.class, this.structureName, this.graph);
         } else if (!dependency && phrase) {
            this.graph = new PhraseStructureGraph(symbolTables);
            this.flowChartinstance.addFlowChartRegistry(PhraseStructure.class, this.structureName, this.graph);
         } else {
            this.graph = new Sentence(symbolTables);
         }

         if (dataFormatInstance != null) {
            ((DependencyStructure)this.graph).setDefaultRootEdgeLabels(OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "graph", "root_label").toString(), dataFormatInstance.getDependencyEdgeLabelSymbolTables(symbolTables));
         }

         this.flowChartinstance.addFlowChartRegistry(TokenStructure.class, this.structureName, this.graph);
      }

      return signal;
   }

   public int process(int signal) throws MaltChainedException {
      return signal;
   }

   public int postprocess(int signal) throws MaltChainedException {
      return signal;
   }

   public void terminate() throws MaltChainedException {
      if (this.graph != null) {
         this.graph.clear();
         this.graph = null;
      }

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
      sb.append("    graph ");
      sb.append("id:");
      sb.append(this.idName);
      sb.append(' ');
      sb.append("task:");
      sb.append(this.taskName);
      sb.append(' ');
      sb.append("structure:");
      sb.append(this.structureName);
      return sb.toString();
   }
}
