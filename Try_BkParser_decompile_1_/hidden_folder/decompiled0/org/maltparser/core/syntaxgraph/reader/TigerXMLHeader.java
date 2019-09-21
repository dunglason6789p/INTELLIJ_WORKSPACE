/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.reader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.core.helper.Util;
import org.maltparser.core.symbol.SymbolTable;

public class TigerXMLHeader {
    private String corpusID;
    private String corpusVersion;
    private String external;
    private String metaName;
    private String metaAuthor;
    private String metaDescription;
    private String metaInDate;
    private String metaFormat;
    private String metaHistory;
    private FeatureEdgeLabel edgeLabels;
    private FeatureEdgeLabel secEdgeLabels;
    private LinkedHashMap<String, FeatureEdgeLabel> features = new LinkedHashMap();

    public boolean isTigerXMLWritable() {
        return true;
    }

    public void addFeature(String featureName, String domainName) {
        if (!this.features.containsKey(featureName)) {
            this.features.put(featureName, new FeatureEdgeLabel(featureName, domainName));
        }
    }

    public void addFeatureValue(String featureName, String name) {
        this.addFeatureValue(featureName, name, "\t");
    }

    public void addFeatureValue(String featureName, String name, String desc) {
        if (this.features.containsKey(featureName)) {
            if (desc == null || desc.length() == 0) {
                this.features.get(featureName).addValue(name, "\t");
            } else {
                this.features.get(featureName).addValue(name, desc);
            }
        }
    }

    public void addEdgeLabelValue(String name) {
        this.addEdgeLabelValue(name, "\t");
    }

    public void addEdgeLabelValue(String name, String desc) {
        if (this.edgeLabels == null) {
            this.edgeLabels = new FeatureEdgeLabel("edgelabel", Domain.EL);
        }
        if (desc == null || desc.length() == 0) {
            this.edgeLabels.addValue(name, "\t");
        } else {
            this.edgeLabels.addValue(name, desc);
        }
    }

    public void addSecEdgeLabelValue(String name) {
        this.addSecEdgeLabelValue(name, "\t");
    }

    public void addSecEdgeLabelValue(String name, String desc) {
        if (this.secEdgeLabels == null) {
            this.secEdgeLabels = new FeatureEdgeLabel("secedgelabel", Domain.SEL);
        }
        if (desc == null || desc.length() == 0) {
            this.secEdgeLabels.addValue(name, "\t");
        } else {
            this.secEdgeLabels.addValue(name, desc);
        }
    }

    public String getCorpusID() {
        return this.corpusID;
    }

    public void setCorpusID(String corpusID) {
        this.corpusID = corpusID;
    }

    public String getCorpusVersion() {
        return this.corpusVersion;
    }

    public void setCorpusVersion(String corpusVersion) {
        this.corpusVersion = corpusVersion;
    }

    public void setExternal(String external) {
        this.external = external;
    }

    public String getExternal() {
        return this.external;
    }

    public void setMeta(String metaElement, String value) {
        if (metaElement.equals("name")) {
            this.setMetaName(value);
        }
        if (metaElement.equals("author")) {
            this.setMetaAuthor(value);
        }
        if (metaElement.equals("description")) {
            this.setMetaDescription(value);
        }
        if (metaElement.equals("date")) {
            this.setMetaInDate(value);
        }
        if (metaElement.equals("format")) {
            this.setMetaFormat(value);
        }
        if (metaElement.equals("history")) {
            this.setMetaHistory(value);
        }
    }

    public String getMetaName() {
        return this.metaName;
    }

    public void setMetaName(String metaName) {
        this.metaName = metaName;
    }

    public String getMetaAuthor() {
        return this.metaAuthor;
    }

    public void setMetaAuthor(String metaAuthor) {
        this.metaAuthor = metaAuthor;
    }

    public String getMetaDescription() {
        return this.metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getMetaInDate() {
        return this.metaInDate;
    }

    public String getMetaCurrentDate() {
        return this.getMetaCurrentDate("yyyy-MM-dd HH:mm:ss");
    }

    public String getMetaCurrentDate(String format) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public void setMetaInDate(String metaInDate) {
        this.metaInDate = metaInDate;
    }

    public String getMetaFormat() {
        return this.metaFormat;
    }

    public void setMetaFormat(String metaFormat) {
        this.metaFormat = metaFormat;
    }

    public String getMetaHistory() {
        return this.metaHistory;
    }

    public void setMetaHistory(String metaHistory) {
        this.metaHistory = metaHistory;
    }

    public String toTigerXML() {
        StringBuilder sb = new StringBuilder();
        if (this.getCorpusVersion() == null) {
            sb.append("<corpus id=\"");
            sb.append(this.getCorpusID() == null ? "GeneratedByMaltParser" : this.getCorpusID());
            sb.append("\">\n");
        } else {
            sb.append("<corpus id=\"");
            sb.append(this.getCorpusID() == null ? "GeneratedByMaltParser" : this.getCorpusID());
            sb.append("\" version=\"");
            sb.append(this.getCorpusVersion());
            sb.append("\">\n");
        }
        sb.append("  <head>\n");
        sb.append("    <meta>\n");
        sb.append("      <name>");
        sb.append(this.getMetaName() == null ? "GeneratedByMaltParser" : Util.xmlEscape(this.getMetaName()));
        sb.append("</name>\n");
        sb.append("      <author>MaltParser</author>\n");
        sb.append("      <date>");
        sb.append(this.getMetaCurrentDate());
        sb.append("</date>\n");
        sb.append("      <description>");
        sb.append(Util.xmlEscape("Unfortunately, you have to add the annotations header data yourself. Maybe in later releases this will be fixed. "));
        sb.append("</description>\n");
        sb.append("    </meta>\n");
        sb.append("    <annotation/>\n");
        sb.append("  </head>\n");
        sb.append("  <body>\n");
        return sb.toString();
    }

    public String toString() {
        return this.toTigerXML();
    }

    protected class FeatureEdgeLabel {
        private String name;
        private Domain domain;
        private SortedMap<String, String> values;
        private SymbolTable table;

        public FeatureEdgeLabel(String name, String domainName) {
            this.setName(name);
            this.setDomain(domainName);
        }

        public FeatureEdgeLabel(String name, Domain domain) {
            this.setName(name);
            this.setDomain(domain);
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDomain(String domainName) {
            this.domain = Domain.valueOf(domainName);
        }

        public void setDomain(Domain domain) {
            this.domain = domain;
        }

        public String getDomainName() {
            return this.domain.toString();
        }

        public Domain getDomain() {
            return this.domain;
        }

        public SymbolTable getTable() {
            return this.table;
        }

        public void setTable(SymbolTable table) {
            this.table = table;
        }

        public void addValue(String name) {
            this.addValue(name, "\t");
        }

        public void addValue(String name, String desc) {
            if (this.values == null) {
                this.values = new TreeMap<String, String>();
            }
            this.values.put(name, desc);
        }

        public String toTigerXML() {
            StringBuilder sb = new StringBuilder();
            if (this.domain == Domain.T || this.domain == Domain.FREC || this.domain == Domain.NT) {
                sb.append("      <feature domain=\"");
                sb.append(this.getDomainName());
                sb.append("\" name=\"");
                sb.append(this.getName());
                sb.append(this.values == null ? "\" />\n" : "\">\n");
            }
            if (this.domain == Domain.EL) {
                sb.append(this.values != null ? "      <edgelabel>\n" : "      <edgelabel />\n");
            }
            if (this.domain == Domain.SEL) {
                sb.append(this.values != null ? "      <secedgelabel>\n" : "      <secedgelabel />\n");
            }
            if (this.values != null) {
                for (String name : this.values.keySet()) {
                    sb.append("        <value name=\"");
                    sb.append(name);
                    if (((String)this.values.get(name)).equals("\t")) {
                        sb.append("\" />\n");
                        continue;
                    }
                    sb.append("\">");
                    sb.append(Util.xmlEscape((String)this.values.get(name)));
                    sb.append("</value>\n");
                }
            }
            if ((this.domain == Domain.T || this.domain == Domain.FREC || this.domain == Domain.NT) && this.values != null) {
                sb.append("      </feature>\n");
            }
            if (this.domain == Domain.EL && this.values != null) {
                sb.append("      </edgelabel>\n");
            }
            if (this.domain == Domain.SEL && this.values != null) {
                sb.append("      </secedgelabel>\n");
            }
            return sb.toString();
        }

        public String toString() {
            return this.toTigerXML();
        }
    }

    public static enum Domain {
        T,
        NT,
        FREC,
        EL,
        SEL;
        
    }

}

