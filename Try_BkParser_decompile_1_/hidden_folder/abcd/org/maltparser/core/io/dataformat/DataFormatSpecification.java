/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.io.dataformat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashSet;
import org.maltparser.core.helper.URLFinder;
import org.maltparser.core.io.dataformat.DataFormatEntry;
import org.maltparser.core.io.dataformat.DataFormatException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DataFormatSpecification {
    private String dataFormatName;
    private DataStructure dataStructure;
    private final Map<String, DataFormatEntry> entries = new LinkedHashMap<String, DataFormatEntry>();
    private final HashSet<Dependency> dependencies = new HashSet();

    public DataFormatInstance createDataFormatInstance(SymbolTableHandler symbolTables, String nullValueStrategy) throws MaltChainedException {
        return new DataFormatInstance(this.entries, symbolTables, nullValueStrategy, this);
    }

    public void parseDataFormatXMLfile(String fileName) throws MaltChainedException {
        URLFinder f = new URLFinder();
        URL url = f.findURL(fileName);
        if (url == null) {
            throw new DataFormatException("The data format specifcation file '" + fileName + "'cannot be found. ");
        }
        this.parseDataFormatXMLfile(url);
    }

    public HashSet<Dependency> getDependencies() {
        return this.dependencies;
    }

    public void parseDataFormatXMLfile(URL url) throws MaltChainedException {
        if (url == null) {
            throw new DataFormatException("The data format specifcation file cannot be found. ");
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Element root = db.parse(url.openStream()).getDocumentElement();
            if (root.getNodeName().equals("dataformat")) {
                this.dataFormatName = root.getAttribute("name");
                this.dataStructure = root.getAttribute("datastructure").length() > 0 ? DataStructure.valueOf(root.getAttribute("datastructure").toUpperCase()) : DataStructure.DEPENDENCY;
            } else {
                throw new DataFormatException("Data format specification file must contain one 'dataformat' element. ");
            }
            NodeList cols = root.getElementsByTagName("column");
            Element col = null;
            int n = cols.getLength();
            for (int i = 0; i < n; ++i) {
                col = (Element)cols.item(i);
                DataFormatEntry entry = new DataFormatEntry(col.getAttribute("name"), col.getAttribute("category"), col.getAttribute("type"), col.getAttribute("default"));
                this.entries.put(entry.getDataFormatEntryName(), entry);
            }
            NodeList deps = root.getElementsByTagName("dependencies");
            if (deps.getLength() > 0) {
                NodeList dep = ((Element)deps.item(0)).getElementsByTagName("dependency");
                int n2 = dep.getLength();
                for (int i = 0; i < n2; ++i) {
                    Element e = (Element)dep.item(i);
                    this.dependencies.add(new Dependency(e.getAttribute("name"), e.getAttribute("url"), e.getAttribute("map"), e.getAttribute("urlmap")));
                }
            }
        }
        catch (IOException e) {
            throw new DataFormatException("Cannot find the file " + url.toString() + ". ", e);
        }
        catch (ParserConfigurationException e) {
            throw new DataFormatException("Problem parsing the file " + url.toString() + ". ", e);
        }
        catch (SAXException e) {
            throw new DataFormatException("Problem parsing the file " + url.toString() + ". ", e);
        }
    }

    public void addEntry(String dataFormatEntryName, String category, String type, String defaultOutput) {
        DataFormatEntry entry = new DataFormatEntry(dataFormatEntryName, category, type, defaultOutput);
        this.entries.put(entry.getDataFormatEntryName(), entry);
    }

    public DataFormatEntry getEntry(String dataFormatEntryName) {
        return this.entries.get(dataFormatEntryName);
    }

    public String getDataFormatName() {
        return this.dataFormatName;
    }

    public DataStructure getDataStructure() {
        return this.dataStructure;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Data format specification: ");
        sb.append(this.dataFormatName);
        sb.append('\n');
        for (DataFormatEntry dfe : this.entries.values()) {
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
        
    }

}

