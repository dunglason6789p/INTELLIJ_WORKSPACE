package org.maltparser.core.io.dataformat;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashSet;
import org.maltparser.core.helper.URLFinder;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DataFormatSpecification {
   private String dataFormatName;
   private DataFormatSpecification.DataStructure dataStructure;
   private final Map<String, DataFormatEntry> entries = new LinkedHashMap();
   private final HashSet<DataFormatSpecification.Dependency> dependencies = new HashSet();

   public DataFormatSpecification() {
   }

   public DataFormatInstance createDataFormatInstance(SymbolTableHandler symbolTables, String nullValueStrategy) throws MaltChainedException {
      return new DataFormatInstance(this.entries, symbolTables, nullValueStrategy, this);
   }

   public void parseDataFormatXMLfile(String fileName) throws MaltChainedException {
      URLFinder f = new URLFinder();
      URL url = f.findURL(fileName);
      if (url == null) {
         throw new DataFormatException("The data format specifcation file '" + fileName + "'cannot be found. ");
      } else {
         this.parseDataFormatXMLfile(url);
      }
   }

   public HashSet<DataFormatSpecification.Dependency> getDependencies() {
      return this.dependencies;
   }

   public void parseDataFormatXMLfile(URL url) throws MaltChainedException {
      if (url == null) {
         throw new DataFormatException("The data format specifcation file cannot be found. ");
      } else {
         try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Element root = db.parse(url.openStream()).getDocumentElement();
            if (!root.getNodeName().equals("dataformat")) {
               throw new DataFormatException("Data format specification file must contain one 'dataformat' element. ");
            } else {
               this.dataFormatName = root.getAttribute("name");
               if (root.getAttribute("datastructure").length() > 0) {
                  this.dataStructure = DataFormatSpecification.DataStructure.valueOf(root.getAttribute("datastructure").toUpperCase());
               } else {
                  this.dataStructure = DataFormatSpecification.DataStructure.DEPENDENCY;
               }

               NodeList cols = root.getElementsByTagName("column");
               Element col = null;
               int i = 0;

               for(int n = cols.getLength(); i < n; ++i) {
                  col = (Element)cols.item(i);
                  DataFormatEntry entry = new DataFormatEntry(col.getAttribute("name"), col.getAttribute("category"), col.getAttribute("type"), col.getAttribute("default"));
                  this.entries.put(entry.getDataFormatEntryName(), entry);
               }

               NodeList deps = root.getElementsByTagName("dependencies");
               if (deps.getLength() > 0) {
                  NodeList dep = ((Element)deps.item(0)).getElementsByTagName("dependency");
                  int i = 0;

                  for(int n = dep.getLength(); i < n; ++i) {
                     Element e = (Element)dep.item(i);
                     this.dependencies.add(new DataFormatSpecification.Dependency(e.getAttribute("name"), e.getAttribute("url"), e.getAttribute("map"), e.getAttribute("urlmap")));
                  }
               }

            }
         } catch (IOException var12) {
            throw new DataFormatException("Cannot find the file " + url.toString() + ". ", var12);
         } catch (ParserConfigurationException var13) {
            throw new DataFormatException("Problem parsing the file " + url.toString() + ". ", var13);
         } catch (SAXException var14) {
            throw new DataFormatException("Problem parsing the file " + url.toString() + ". ", var14);
         }
      }
   }

   public void addEntry(String dataFormatEntryName, String category, String type, String defaultOutput) {
      DataFormatEntry entry = new DataFormatEntry(dataFormatEntryName, category, type, defaultOutput);
      this.entries.put(entry.getDataFormatEntryName(), entry);
   }

   public DataFormatEntry getEntry(String dataFormatEntryName) {
      return (DataFormatEntry)this.entries.get(dataFormatEntryName);
   }

   public String getDataFormatName() {
      return this.dataFormatName;
   }

   public DataFormatSpecification.DataStructure getDataStructure() {
      return this.dataStructure;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Data format specification: ");
      sb.append(this.dataFormatName);
      sb.append('\n');
      Iterator i$ = this.entries.values().iterator();

      while(i$.hasNext()) {
         DataFormatEntry dfe = (DataFormatEntry)i$.next();
         sb.append(dfe);
         sb.append('\n');
      }

      return sb.toString();
   }

   public class Dependency {
      protected String dependentOn;
      protected String urlString;
      protected String map;
      protected String mapUrl;

      public Dependency(String dependentOn, String urlString, String map, String mapUrl) {
         this.setDependentOn(dependentOn);
         this.setUrlString(urlString);
         this.setMap(map);
         this.setMapUrl(mapUrl);
      }

      public String getDependentOn() {
         return this.dependentOn;
      }

      protected void setDependentOn(String dependentOn) {
         this.dependentOn = dependentOn;
      }

      public String getUrlString() {
         return this.urlString;
      }

      public void setUrlString(String urlString) {
         this.urlString = urlString;
      }

      public String getMap() {
         return this.map;
      }

      protected void setMap(String map) {
         this.map = map;
      }

      public String getMapUrl() {
         return this.mapUrl;
      }

      public void setMapUrl(String mapUrl) {
         this.mapUrl = mapUrl;
      }

      public String toString() {
         return "Dependency [dependentOn=" + this.dependentOn + ", map=" + this.map + ", mapUrl=" + this.mapUrl + ", urlString=" + this.urlString + "]";
      }
   }

   public static enum DataStructure {
      DEPENDENCY,
      PHRASE;

      private DataStructure() {
      }
   }
}
