package org.maltparser.core.flow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.spec.ChartItemSpecification;
import org.maltparser.core.flow.spec.ChartSpecification;

public class FlowChartInstance {
   private FlowChartManager flowChartManager;
   private int optionContainerIndex;
   private String name;
   private ChartSpecification chartSpecification;
   private final LinkedHashSet<ChartItem> preProcessChartItems;
   private final LinkedHashSet<ChartItem> processChartItems;
   private final LinkedHashSet<ChartItem> postProcessChartItems;
   private final HashMap<String, Object> flowChartRegistry;
   private final HashMap<String, Object> engineRegistry;
   private final StringBuilder flowChartRegistryKey;

   public FlowChartInstance(int optionContainerIndex, ChartSpecification chartSpecification, FlowChartManager flowChartManager) throws MaltChainedException {
      this.setFlowChartManager(flowChartManager);
      this.setOptionContainerIndex(optionContainerIndex);
      this.setChartSpecification(chartSpecification);
      this.flowChartRegistry = new HashMap();
      this.engineRegistry = new HashMap();
      this.flowChartRegistryKey = new StringBuilder();
      this.preProcessChartItems = new LinkedHashSet();
      Iterator i$ = chartSpecification.getPreProcessChartItemSpecifications().iterator();

      ChartItemSpecification chartItemSpecification;
      while(i$.hasNext()) {
         chartItemSpecification = (ChartItemSpecification)i$.next();
         this.preProcessChartItems.add(this.initChartItem(chartItemSpecification));
      }

      this.processChartItems = new LinkedHashSet();
      i$ = chartSpecification.getProcessChartItemSpecifications().iterator();

      while(i$.hasNext()) {
         chartItemSpecification = (ChartItemSpecification)i$.next();
         this.processChartItems.add(this.initChartItem(chartItemSpecification));
      }

      this.postProcessChartItems = new LinkedHashSet();
      i$ = chartSpecification.getPostProcessChartItemSpecifications().iterator();

      while(i$.hasNext()) {
         chartItemSpecification = (ChartItemSpecification)i$.next();
         this.postProcessChartItems.add(this.initChartItem(chartItemSpecification));
      }

   }

   protected ChartItem initChartItem(ChartItemSpecification chartItemSpecification) throws MaltChainedException {
      ChartItem chartItem = null;

      try {
         chartItem = (ChartItem)chartItemSpecification.getChartItemClass().newInstance();
         chartItem.initialize(this, chartItemSpecification);
         return chartItem;
      } catch (InstantiationException var4) {
         throw new FlowException("The chart item '" + chartItemSpecification.getChartItemName() + "' could not be created. ", var4);
      } catch (IllegalAccessException var5) {
         throw new FlowException("The chart item '" + chartItemSpecification.getChartItemName() + "' could not be created. ", var5);
      }
   }

   private void setFlowChartRegistryKey(Class<?> entryClass, String identifier) {
      this.flowChartRegistryKey.setLength(0);
      this.flowChartRegistryKey.append(identifier.toString());
      this.flowChartRegistryKey.append(entryClass.toString());
   }

   public void addFlowChartRegistry(Class<?> entryClass, String identifier, Object entry) {
      this.setFlowChartRegistryKey(entryClass, identifier);
      this.flowChartRegistry.put(this.flowChartRegistryKey.toString(), entry);
   }

   public void removeFlowChartRegistry(Class<?> entryClass, String identifier) {
      this.setFlowChartRegistryKey(entryClass, identifier);
      this.flowChartRegistry.remove(this.flowChartRegistryKey.toString());
   }

   public Object getFlowChartRegistry(Class<?> entryClass, String identifier) {
      this.setFlowChartRegistryKey(entryClass, identifier);
      return this.flowChartRegistry.get(this.flowChartRegistryKey.toString());
   }

   public void setEngineRegistry(String key, Object value) {
      this.engineRegistry.put(key, value);
   }

   public Object getEngineRegistry(String key) {
      return this.engineRegistry.get(key);
   }

   public FlowChartManager getFlowChartManager() {
      return this.flowChartManager;
   }

   protected void setFlowChartManager(FlowChartManager flowChartManager) {
      this.flowChartManager = flowChartManager;
   }

   public int getOptionContainerIndex() {
      return this.optionContainerIndex;
   }

   protected void setOptionContainerIndex(int optionContainerIndex) {
      this.optionContainerIndex = optionContainerIndex;
   }

   public ChartSpecification getChartSpecification() {
      return this.chartSpecification;
   }

   protected void setChartSpecification(ChartSpecification chartSpecification) {
      this.chartSpecification = chartSpecification;
   }

   public LinkedHashSet<ChartItem> getPreProcessChartItems() {
      return this.preProcessChartItems;
   }

   public LinkedHashSet<ChartItem> getProcessChartItems() {
      return this.processChartItems;
   }

   public LinkedHashSet<ChartItem> getPostProcessChartItems() {
      return this.postProcessChartItems;
   }

   public boolean hasPreProcessChartItems() {
      return this.preProcessChartItems.size() != 0;
   }

   public boolean hasProcessChartItems() {
      return this.processChartItems.size() != 0;
   }

   public boolean hasPostProcessChartItems() {
      return this.postProcessChartItems.size() != 0;
   }

   public int preprocess() throws MaltChainedException {
      LinkedHashSet<ChartItem> chartItems = this.getPreProcessChartItems();
      if (chartItems.size() == 0) {
         return 2;
      } else {
         int signal = 1;
         Iterator i$ = chartItems.iterator();

         do {
            if (!i$.hasNext()) {
               return signal;
            }

            ChartItem chartItem = (ChartItem)i$.next();
            signal = chartItem.preprocess(signal);
         } while(signal != 2);

         return signal;
      }
   }

   public int process() throws MaltChainedException {
      LinkedHashSet<ChartItem> chartItems = this.getProcessChartItems();
      if (chartItems.size() == 0) {
         return 2;
      } else {
         int signal = 1;

         ChartItem chartItem;
         for(Iterator i$ = chartItems.iterator(); i$.hasNext(); signal = chartItem.process(signal)) {
            chartItem = (ChartItem)i$.next();
         }

         return signal;
      }
   }

   public int postprocess() throws MaltChainedException {
      LinkedHashSet<ChartItem> chartItems = this.getPostProcessChartItems();
      if (chartItems.size() == 0) {
         return 2;
      } else {
         int signal = 1;
         Iterator i$ = chartItems.iterator();

         do {
            if (!i$.hasNext()) {
               return signal;
            }

            ChartItem chartItem = (ChartItem)i$.next();
            signal = chartItem.postprocess(signal);
         } while(signal != 2);

         return signal;
      }
   }

   public void terminate() throws MaltChainedException {
      LinkedHashSet<ChartItem> chartItems = this.getPreProcessChartItems();
      Iterator i$ = chartItems.iterator();

      ChartItem chartItem;
      while(i$.hasNext()) {
         chartItem = (ChartItem)i$.next();
         chartItem.terminate();
      }

      chartItems = this.getProcessChartItems();
      i$ = chartItems.iterator();

      while(i$.hasNext()) {
         chartItem = (ChartItem)i$.next();
         chartItem.terminate();
      }

      chartItems = this.getPostProcessChartItems();
      i$ = chartItems.iterator();

      while(i$.hasNext()) {
         chartItem = (ChartItem)i$.next();
         chartItem.terminate();
      }

      this.flowChartRegistry.clear();
      this.engineRegistry.clear();
      this.flowChartRegistryKey.setLength(0);
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + this.optionContainerIndex;
      result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
      result = 31 * result + (this.chartSpecification == null ? 0 : this.chartSpecification.hashCode());
      result = 31 * result + (this.flowChartRegistry == null ? 0 : this.flowChartRegistry.hashCode());
      result = 31 * result + (this.postProcessChartItems == null ? 0 : this.postProcessChartItems.hashCode());
      result = 31 * result + (this.preProcessChartItems == null ? 0 : this.preProcessChartItems.hashCode());
      result = 31 * result + (this.processChartItems == null ? 0 : this.processChartItems.hashCode());
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         FlowChartInstance other = (FlowChartInstance)obj;
         if (this.optionContainerIndex != other.optionContainerIndex) {
            return false;
         } else {
            if (this.name == null) {
               if (other.name != null) {
                  return false;
               }
            } else if (!this.name.equals(other.name)) {
               return false;
            }

            if (this.chartSpecification == null) {
               if (other.chartSpecification != null) {
                  return false;
               }
            } else if (!this.chartSpecification.equals(other.chartSpecification)) {
               return false;
            }

            if (this.flowChartRegistry == null) {
               if (other.flowChartRegistry != null) {
                  return false;
               }
            } else if (!this.flowChartRegistry.equals(other.flowChartRegistry)) {
               return false;
            }

            if (this.postProcessChartItems == null) {
               if (other.postProcessChartItems != null) {
                  return false;
               }
            } else if (!this.postProcessChartItems.equals(other.postProcessChartItems)) {
               return false;
            }

            if (this.preProcessChartItems == null) {
               if (other.preProcessChartItems != null) {
                  return false;
               }
            } else if (!this.preProcessChartItems.equals(other.preProcessChartItems)) {
               return false;
            }

            if (this.processChartItems == null) {
               if (other.processChartItems != null) {
                  return false;
               }
            } else if (!this.processChartItems.equals(other.processChartItems)) {
               return false;
            }

            return true;
         }
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.name);
      sb.append('\n');
      Iterator i$;
      ChartItem key;
      if (this.preProcessChartItems.size() > 0) {
         sb.append("  preprocess:");
         sb.append('\n');
         i$ = this.preProcessChartItems.iterator();

         while(i$.hasNext()) {
            key = (ChartItem)i$.next();
            sb.append(key);
            sb.append('\n');
         }
      }

      if (this.processChartItems.size() > 0) {
         sb.append("  process:");
         sb.append('\n');
         i$ = this.processChartItems.iterator();

         while(i$.hasNext()) {
            key = (ChartItem)i$.next();
            sb.append(key);
            sb.append('\n');
         }
      }

      if (this.postProcessChartItems.size() > 0) {
         sb.append("  postprocess:");
         sb.append('\n');
         i$ = this.postProcessChartItems.iterator();

         while(i$.hasNext()) {
            key = (ChartItem)i$.next();
            sb.append(key);
            sb.append('\n');
         }
      }

      return sb.toString();
   }
}
