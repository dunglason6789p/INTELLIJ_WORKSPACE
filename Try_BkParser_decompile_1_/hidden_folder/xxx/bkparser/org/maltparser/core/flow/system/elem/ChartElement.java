package org.maltparser.core.flow.system.elem;

import java.util.Iterator;
import java.util.LinkedHashMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.flow.FlowException;
import org.maltparser.core.flow.item.ChartItem;
import org.maltparser.core.flow.system.FlowChartSystem;
import org.maltparser.core.plugin.PluginLoader;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ChartElement {
   private String item;
   private Class<? extends ChartItem> chartItemClass;
   private LinkedHashMap<String, ChartAttribute> attributes = new LinkedHashMap();

   public ChartElement() {
   }

   public String getItem() {
      return this.item;
   }

   public void setItem(String item) {
      this.item = item;
   }

   public void addAttribute(String name, ChartAttribute attribute) {
      this.attributes.put(name, attribute);
   }

   public ChartAttribute getAttribute(String name) {
      return (ChartAttribute)this.attributes.get(name);
   }

   public Class<? extends ChartItem> getChartItemClass() {
      return this.chartItemClass;
   }

   public LinkedHashMap<String, ChartAttribute> getAttributes() {
      return this.attributes;
   }

   public void read(Element chartElem, FlowChartSystem flowChartSystem) throws MaltChainedException {
      this.setItem(chartElem.getAttribute("item"));
      String chartItemClassName = chartElem.getAttribute("class");
      Class clazz = null;

      try {
         if (PluginLoader.instance() != null) {
            clazz = PluginLoader.instance().getClass(chartItemClassName);
         }

         if (clazz == null) {
            clazz = Class.forName(chartItemClassName);
         }

         this.chartItemClass = clazz.asSubclass(ChartItem.class);
      } catch (ClassCastException var8) {
         throw new FlowException("The class '" + clazz.getName() + "' is not a subclass of '" + ChartItem.class.getName() + "'. ", var8);
      } catch (ClassNotFoundException var9) {
         throw new FlowException("The class " + chartItemClassName + "  could not be found. ", var9);
      }

      NodeList attrElements = chartElem.getElementsByTagName("attribute");

      for(int i = 0; i < attrElements.getLength(); ++i) {
         ChartAttribute attribute = new ChartAttribute();
         attribute.read((Element)attrElements.item(i), flowChartSystem);
         this.attributes.put(((Element)attrElements.item(i)).getAttribute("name"), attribute);
      }

   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("    ");
      sb.append(this.item);
      sb.append(' ');
      sb.append(this.chartItemClass.getName());
      sb.append('\n');
      Iterator i$ = this.attributes.keySet().iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         sb.append("       ");
         sb.append(this.attributes.get(key));
         sb.append('\n');
      }

      return sb.toString();
   }
}
