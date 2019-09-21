package org.maltparser.core.flow.system;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.flow.FlowException;
import org.maltparser.core.flow.system.elem.ChartElement;
import org.maltparser.core.helper.URLFinder;
import org.maltparser.core.plugin.Plugin;
import org.maltparser.core.plugin.PluginLoader;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FlowChartSystem {
   private HashMap<String, ChartElement> chartElements = new HashMap();

   public FlowChartSystem() {
   }

   public void load(String urlstring) throws MaltChainedException {
      URLFinder f = new URLFinder();
      this.load(f.findURL(urlstring));
   }

   public void load(PluginLoader plugins) throws MaltChainedException {
      Iterator i$ = plugins.iterator();

      while(i$.hasNext()) {
         Plugin plugin = (Plugin)i$.next();
         URL url = null;

         try {
            url = new URL("jar:" + plugin.getUrl() + "!/appdata/plugin.xml");
         } catch (MalformedURLException var6) {
            throw new FeatureException("Malformed URL: 'jar:" + plugin.getUrl() + "!plugin.xml'", var6);
         }

         try {
            InputStream is = url.openStream();
            is.close();
         } catch (IOException var7) {
            continue;
         }

         this.load(url);
      }

   }

   public void load(URL specModelURL) throws MaltChainedException {
      try {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         Element root = null;
         root = db.parse(specModelURL.openStream()).getDocumentElement();
         if (root == null) {
            throw new FlowException("The flow chart system file '" + specModelURL.getFile() + "' cannot be found. ");
         } else {
            this.readChartElements(root);
         }
      } catch (IOException var5) {
         throw new FlowException("The flow chart system file '" + specModelURL.getFile() + "' cannot be found. ", var5);
      } catch (ParserConfigurationException var6) {
         throw new FlowException("Problem parsing the file " + specModelURL.getFile() + ". ", var6);
      } catch (SAXException var7) {
         throw new FlowException("Problem parsing the file " + specModelURL.getFile() + ". ", var7);
      }
   }

   public void readChartElements(Element root) throws MaltChainedException {
      NodeList chartElem = root.getElementsByTagName("chartelement");

      for(int i = 0; i < chartElem.getLength(); ++i) {
         ChartElement chartElement = new ChartElement();
         chartElement.read((Element)chartElem.item(i), this);
         this.chartElements.put(((Element)chartElem.item(i)).getAttribute("item"), chartElement);
      }

   }

   public ChartElement getChartElement(String name) {
      return (ChartElement)this.chartElements.get(name);
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("CHART ELEMENTS:\n");
      Iterator i$ = this.chartElements.keySet().iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         sb.append(this.chartElements.get(key));
         sb.append('\n');
      }

      return sb.toString();
   }
}
