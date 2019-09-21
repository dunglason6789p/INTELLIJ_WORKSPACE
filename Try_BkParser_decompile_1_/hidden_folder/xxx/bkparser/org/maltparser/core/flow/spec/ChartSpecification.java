package org.maltparser.core.flow.spec;

import java.util.Iterator;
import java.util.LinkedHashSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartManager;
import org.maltparser.core.flow.FlowException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ChartSpecification {
   private String name;
   private LinkedHashSet<ChartItemSpecification> preProcessChartItemSpecifications = new LinkedHashSet(7);
   private LinkedHashSet<ChartItemSpecification> processChartItemSpecifications = new LinkedHashSet(7);
   private LinkedHashSet<ChartItemSpecification> postProcessChartItemSpecifications = new LinkedHashSet(7);

   public ChartSpecification() {
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public LinkedHashSet<ChartItemSpecification> getPreProcessChartItemSpecifications() {
      return this.preProcessChartItemSpecifications;
   }

   public void addPreProcessChartItemSpecifications(ChartItemSpecification chartItemSpecification) {
      this.preProcessChartItemSpecifications.add(chartItemSpecification);
   }

   public void removePreProcessChartItemSpecifications(ChartItemSpecification chartItemSpecification) {
      this.preProcessChartItemSpecifications.remove(chartItemSpecification);
   }

   public LinkedHashSet<ChartItemSpecification> getProcessChartItemSpecifications() {
      return this.processChartItemSpecifications;
   }

   public void addProcessChartItemSpecifications(ChartItemSpecification chartItemSpecification) {
      this.processChartItemSpecifications.add(chartItemSpecification);
   }

   public void removeProcessChartItemSpecifications(ChartItemSpecification chartItemSpecification) {
      this.processChartItemSpecifications.remove(chartItemSpecification);
   }

   public LinkedHashSet<ChartItemSpecification> getPostProcessChartItemSpecifications() {
      return this.postProcessChartItemSpecifications;
   }

   public void addPostProcessChartItemSpecifications(ChartItemSpecification chartItemSpecification) {
      this.postProcessChartItemSpecifications.add(chartItemSpecification);
   }

   public void removePostProcessChartItemSpecifications(ChartItemSpecification chartItemSpecification) {
      this.postProcessChartItemSpecifications.remove(chartItemSpecification);
   }

   public void read(Element chartElem, FlowChartManager flowCharts) throws MaltChainedException {
      this.setName(chartElem.getAttribute("name"));
      NodeList flowChartProcessList = chartElem.getElementsByTagName("preprocess");
      if (flowChartProcessList.getLength() == 1) {
         this.readChartItems((Element)flowChartProcessList.item(0), flowCharts, this.preProcessChartItemSpecifications);
      } else if (flowChartProcessList.getLength() > 1) {
         throw new FlowException("The flow chart '" + this.getName() + "' has more than one preprocess elements. ");
      }

      flowChartProcessList = chartElem.getElementsByTagName("process");
      if (flowChartProcessList.getLength() == 1) {
         this.readChartItems((Element)flowChartProcessList.item(0), flowCharts, this.processChartItemSpecifications);
      } else if (flowChartProcessList.getLength() > 1) {
         throw new FlowException("The flow chart '" + this.getName() + "' has more than one process elements. ");
      }

      flowChartProcessList = chartElem.getElementsByTagName("postprocess");
      if (flowChartProcessList.getLength() == 1) {
         this.readChartItems((Element)flowChartProcessList.item(0), flowCharts, this.postProcessChartItemSpecifications);
      } else if (flowChartProcessList.getLength() > 1) {
         throw new FlowException("The flow chart '" + this.getName() + "' has more than one postprocess elements. ");
      }

   }

   private void readChartItems(Element chartElem, FlowChartManager flowCharts, LinkedHashSet<ChartItemSpecification> chartItemSpecifications) throws MaltChainedException {
      NodeList flowChartItemList = chartElem.getElementsByTagName("chartitem");

      for(int i = 0; i < flowChartItemList.getLength(); ++i) {
         ChartItemSpecification chartItemSpecification = new ChartItemSpecification();
         chartItemSpecification.read((Element)flowChartItemList.item(i), flowCharts);
         chartItemSpecifications.add(chartItemSpecification);
      }

   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
      result = 31 * result + (this.postProcessChartItemSpecifications == null ? 0 : this.postProcessChartItemSpecifications.hashCode());
      result = 31 * result + (this.preProcessChartItemSpecifications == null ? 0 : this.preProcessChartItemSpecifications.hashCode());
      result = 31 * result + (this.processChartItemSpecifications == null ? 0 : this.processChartItemSpecifications.hashCode());
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
         ChartSpecification other = (ChartSpecification)obj;
         if (this.name == null) {
            if (other.name != null) {
               return false;
            }
         } else if (!this.name.equals(other.name)) {
            return false;
         }

         if (this.postProcessChartItemSpecifications == null) {
            if (other.postProcessChartItemSpecifications != null) {
               return false;
            }
         } else if (!this.postProcessChartItemSpecifications.equals(other.postProcessChartItemSpecifications)) {
            return false;
         }

         if (this.preProcessChartItemSpecifications == null) {
            if (other.preProcessChartItemSpecifications != null) {
               return false;
            }
         } else if (!this.preProcessChartItemSpecifications.equals(other.preProcessChartItemSpecifications)) {
            return false;
         }

         if (this.processChartItemSpecifications == null) {
            if (other.processChartItemSpecifications != null) {
               return false;
            }
         } else if (!this.processChartItemSpecifications.equals(other.processChartItemSpecifications)) {
            return false;
         }

         return true;
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.name);
      sb.append('\n');
      Iterator i$;
      ChartItemSpecification key;
      if (this.preProcessChartItemSpecifications.size() > 0) {
         sb.append("  preprocess:");
         sb.append('\n');
         i$ = this.preProcessChartItemSpecifications.iterator();

         while(i$.hasNext()) {
            key = (ChartItemSpecification)i$.next();
            sb.append(key);
            sb.append('\n');
         }
      }

      if (this.processChartItemSpecifications.size() > 0) {
         sb.append("  process:");
         sb.append('\n');
         i$ = this.processChartItemSpecifications.iterator();

         while(i$.hasNext()) {
            key = (ChartItemSpecification)i$.next();
            sb.append(key);
            sb.append('\n');
         }
      }

      if (this.postProcessChartItemSpecifications.size() > 0) {
         sb.append("  postprocess:");
         sb.append('\n');
         i$ = this.postProcessChartItemSpecifications.iterator();

         while(i$.hasNext()) {
            key = (ChartItemSpecification)i$.next();
            sb.append(key);
            sb.append('\n');
         }
      }

      return sb.toString();
   }
}
