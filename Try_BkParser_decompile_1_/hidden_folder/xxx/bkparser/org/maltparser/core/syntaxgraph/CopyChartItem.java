package org.maltparser.core.syntaxgraph;

import java.util.ArrayList;
import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartInstance;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.maltparser.core.flow.system.elem.ChartAttribute;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class CopyChartItem extends ChartItem {
   private String idName;
   private String targetName;
   private String sourceName;
   private String taskName;
   private boolean usePartialTree;
   private TokenStructure cachedSource = null;
   private TokenStructure cachedTarget = null;

   public CopyChartItem() {
   }

   public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
      super.initialize(flowChartinstance, chartItemSpecification);
      Iterator i$ = chartItemSpecification.getChartItemAttributes().keySet().iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         if (key.equals("id")) {
            this.idName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         } else if (key.equals("target")) {
            this.targetName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         } else if (key.equals("source")) {
            this.sourceName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         } else if (key.equals("task")) {
            this.taskName = (String)chartItemSpecification.getChartItemAttributes().get(key);
         }
      }

      if (this.idName == null) {
         this.idName = ((ChartAttribute)this.getChartElement("copy").getAttributes().get("id")).getDefaultValue();
      } else if (this.targetName == null) {
         this.targetName = ((ChartAttribute)this.getChartElement("copy").getAttributes().get("target")).getDefaultValue();
      } else if (this.sourceName == null) {
         this.sourceName = ((ChartAttribute)this.getChartElement("copy").getAttributes().get("source")).getDefaultValue();
      } else if (this.taskName == null) {
         this.taskName = ((ChartAttribute)this.getChartElement("copy").getAttributes().get("task")).getDefaultValue();
      }

      this.usePartialTree = OptionManager.instance().getOptionValue(this.getOptionContainerIndex(), "singlemalt", "use_partial_tree").toString().equals("true");
   }

   public int preprocess(int signal) throws MaltChainedException {
      return signal;
   }

   public int process(int signal) throws MaltChainedException {
      if (this.taskName.equals("terminals")) {
         if (this.cachedSource == null) {
            this.cachedSource = (TokenStructure)this.flowChartinstance.getFlowChartRegistry(TokenStructure.class, this.sourceName);
         }

         if (this.cachedTarget == null) {
            this.cachedTarget = (TokenStructure)this.flowChartinstance.getFlowChartRegistry(TokenStructure.class, this.targetName);
         }

         this.copyTerminalStructure(this.cachedSource, this.cachedTarget);
         if (this.usePartialTree && this.cachedSource instanceof DependencyStructure && this.cachedTarget instanceof DependencyStructure) {
            this.copyPartialDependencyStructure((DependencyStructure)this.cachedSource, (DependencyStructure)this.cachedTarget);
         }
      }

      return signal;
   }

   public int postprocess(int signal) throws MaltChainedException {
      return signal;
   }

   public void terminate() throws MaltChainedException {
      this.cachedSource = null;
      this.cachedTarget = null;
   }

   public void copyTerminalStructure(TokenStructure sourceGraph, TokenStructure targetGraph) throws MaltChainedException {
      targetGraph.clear();
      Iterator i$ = sourceGraph.getTokenIndices().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         DependencyNode gnode = sourceGraph.getTokenNode(index);
         DependencyNode pnode = targetGraph.addTokenNode(gnode.getIndex());
         Iterator i$ = gnode.getLabelTypes().iterator();

         while(i$.hasNext()) {
            SymbolTable table = (SymbolTable)i$.next();
            pnode.addLabel(table, gnode.getLabelSymbol(table));
         }
      }

      if (sourceGraph.hasComments()) {
         for(int i = 1; i <= sourceGraph.nTokenNode() + 1; ++i) {
            ArrayList<String> commentList = sourceGraph.getComment(i);
            if (commentList != null) {
               for(int j = 0; j < commentList.size(); ++j) {
                  targetGraph.addComment((String)commentList.get(j), i);
               }
            }
         }
      }

   }

   public void copyPartialDependencyStructure(DependencyStructure sourceGraph, DependencyStructure targetGraph) throws MaltChainedException {
      SymbolTable partHead = this.cachedSource.getSymbolTables().getSymbolTable("PARTHEAD");
      SymbolTable partDeprel = this.cachedSource.getSymbolTables().getSymbolTable("PARTDEPREL");
      if (partHead != null && partDeprel != null) {
         SymbolTable deprel = this.cachedTarget.getSymbolTables().getSymbolTable("DEPREL");
         Iterator i$ = sourceGraph.getTokenIndices().iterator();

         while(i$.hasNext()) {
            int index = (Integer)i$.next();
            DependencyNode snode = sourceGraph.getTokenNode(index);
            DependencyNode tnode = targetGraph.getTokenNode(index);
            if (snode != null && tnode != null) {
               int spartheadindex = Integer.parseInt(snode.getLabelSymbol(partHead));
               String spartdeprel = snode.getLabelSymbol(partDeprel);
               if (spartheadindex > 0) {
                  Edge tedge = targetGraph.addDependencyEdge(spartheadindex, snode.getIndex());
                  tedge.addLabel(deprel, spartdeprel);
               }
            }
         }

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
      sb.append("    copy ");
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
