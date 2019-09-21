/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.io.dataformat;

public class DataFormatEntry {
    private String dataFormatEntryName;
    private String category;
    private String type;
    private String defaultOutput;
    private int cachedHash;

    public DataFormatEntry(String dataFormatEntryName, String category, String type, String defaultOutput) {
        this.setDataFormatEntryName(dataFormatEntryName);
        this.setCategory(category);
        this.setType(type);
        this.setDefaultOutput(defaultOutput);
    }

    public String getDataFormatEntryName() {
        return this.dataFormatEntryName;
    }

    public void setDataFormatEntryName(String dataFormatEntryName) {
        this.dataFormatEntryName = dataFormatEntryName.toUpperCase();
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category.toUpperCase();
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type.toUpperCase();
    }

    public String getDefaultOutput() {
        return this.defaultOutput;
    }

    public void setDefaultOutput(String defaultOutput) {
        this.defaultOutput = defaultOutput;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        DataFormatEntry objC = (DataFormatEntry)obj;
        return (this.dataFormatEntryName == null ? objC.dataFormatEntryName == null : this.dataFormatEntryName.equals(objC.dataFormatEntryName)) && (this.type == null ? objC.type == null : this.type.equals(objC.type)) && (this.category == null ? objC.category == null : this.category.equals(objC.category)) && (this.defaultOutput == null ? objC.defaultOutput == null : this.defaultOutput.equals(objC.defaultOutput));
    }

    public int hashCode() {
        if (this.cachedHash == 0) {
            int hash = 7;
            hash = 31 * hash + (null == this.dataFormatEntryName ? 0 : this.dataFormatEntryName.hashCode());
            hash = 31 * hash + (null == this.type ? 0 : this.type.hashCode());
            hash = 31 * hash + (null == this.category ? 0 : this.category.hashCode());
            this.cachedHash = hash = 31 * hash + (null == this.defaultOutput ? 0 : this.defaultOutput.hashCode());
        }
        return this.cachedHash;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.dataFormatEntryName);
        sb.append("\t");
        sb.append(this.category);
        sb.append("\t");
        sb.append(this.type);
        if (this.defaultOutput != null) {
            sb.append("\t");
            sb.append(this.defaultOutput);
        }
        return sb.toString();
    }
}

