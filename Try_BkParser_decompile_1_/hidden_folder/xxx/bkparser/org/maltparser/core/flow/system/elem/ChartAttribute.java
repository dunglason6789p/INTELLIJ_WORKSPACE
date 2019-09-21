package org.maltparser.core.flow.system.elem;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.system.FlowChartSystem;
import org.w3c.dom.Element;

public class ChartAttribute {
   private String name;
   private String defaultValue;

   public ChartAttribute() {
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDefaultValue() {
      return this.defaultValue;
   }

   public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
   }

   public void read(Element attrElem, FlowChartSystem flowChartSystem) throws MaltChainedException {
      this.setName(attrElem.getAttribute("name"));
      this.setDefaultValue(attrElem.getAttribute("default"));
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.name);
      sb.append(' ');
      sb.append(this.defaultValue);
      return sb.toString();
   }
}
