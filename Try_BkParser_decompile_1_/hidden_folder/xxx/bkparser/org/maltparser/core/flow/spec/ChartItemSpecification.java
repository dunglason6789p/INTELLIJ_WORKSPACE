package org.maltparser.core.flow.spec;

import java.util.HashMap;
import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowChartManager;
import org.maltparser.core.flow.item.ChartItem;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class ChartItemSpecification {
   private String chartItemName;
   private Class<? extends ChartItem> chartItemClass;
   private HashMap<String, String> attributes;

   public ChartItemSpecification() {
      this((String)null, (Class)null);
   }

   public ChartItemSpecification(String chartItemName, Class<? extends ChartItem> chartItemClass) {
      this.setChartItemName(chartItemName);
      this.setChartItemClass(chartItemClass);
      this.attributes = new HashMap(3);
   }

   public String getChartItemName() {
      return this.chartItemName;
   }

   public void setChartItemName(String chartItemName) {
      this.chartItemName = chartItemName;
   }

   public Class<? extends ChartItem> getChartItemClass() {
      return this.chartItemClass;
   }

   public void setChartItemClass(Class<? extends ChartItem> chartItemClass) {
      this.chartItemClass = chartItemClass;
   }

   public HashMap<String, String> getChartItemAttributes() {
      return this.attributes;
   }

   public String getChartItemAttribute(String key) {
      return (String)this.attributes.get(key);
   }

   public void addChartItemAttribute(String key, String value) {
      this.attributes.put(key, value);
   }

   public void removeChartItemAttribute(String key) {
      this.attributes.remove(key);
   }

   public void read(Element chartItemSpec, FlowChartManager flowCharts) throws MaltChainedException {
      this.chartItemName = chartItemSpec.getAttribute("item");
      this.chartItemClass = flowCharts.getFlowChartSystem().getChartElement(this.chartItemName).getChartItemClass();
      NamedNodeMap attrs = chartItemSpec.getAttributes();

      for(int i = 0; i < attrs.getLength(); ++i) {
         Attr attribute = (Attr)attrs.item(i);
         this.addChartItemAttribute(attribute.getName(), attribute.getValue());
      }

   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this.chartItemName == null ? 0 : this.chartItemName.hashCode());
      result = 31 * result + (this.attributes == null ? 0 : this.attributes.hashCode());
      result = 31 * result + (this.chartItemClass == null ? 0 : this.chartItemClass.hashCode());
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
         ChartItemSpecification other = (ChartItemSpecification)obj;
         if (this.chartItemName == null) {
            if (other.chartItemName != null) {
               return false;
            }
         } else if (!this.chartItemName.equals(other.chartItemName)) {
            return false;
         }

         if (this.attributes == null) {
            if (other.attributes != null) {
               return false;
            }
         } else if (!this.attributes.equals(other.attributes)) {
            return false;
         }

         if (this.chartItemClass == null) {
            if (other.chartItemClass != null) {
               return false;
            }
         } else if (!this.chartItemClass.equals(other.chartItemClass)) {
            return false;
         }

         return true;
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.chartItemName);
      sb.append(' ');
      Iterator i$ = this.attributes.keySet().iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         sb.append(key);
         sb.append('=');
         sb.append((String)this.attributes.get(key));
         sb.append(' ');
      }

      return sb.toString();
   }
}
