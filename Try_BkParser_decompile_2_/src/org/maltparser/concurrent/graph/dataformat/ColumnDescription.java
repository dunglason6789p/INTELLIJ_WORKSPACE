/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.concurrent.graph.dataformat;

public final class ColumnDescription
implements Comparable<ColumnDescription> {
    public static final int INPUT = 1;
    public static final int HEAD = 2;
    public static final int DEPENDENCY_EDGE_LABEL = 3;
    public static final int PHRASE_STRUCTURE_EDGE_LABEL = 4;
    public static final int PHRASE_STRUCTURE_NODE_LABEL = 5;
    public static final int SECONDARY_EDGE_LABEL = 6;
    public static final int IGNORE = 7;
    public static final String[] categories = new String[]{"", "INPUT", "HEAD", "DEPENDENCY_EDGE_LABEL", "PHRASE_STRUCTURE_EDGE_LABEL", "PHRASE_STRUCTURE_NODE_LABEL", "SECONDARY_EDGE_LABEL", "IGNORE"};
    public static final int STRING = 1;
    public static final int INTEGER = 2;
    public static final int BOOLEAN = 3;
    public static final int REAL = 4;
    public static final String[] types = new String[]{"", "STRING", "INTEGER", "BOOLEAN", "REAL"};
    private final int position;
    private final String name;
    private final int category;
    private final int type;
    private final String defaultOutput;
    private final boolean internal;

    public ColumnDescription(ColumnDescription columnDescription) {
        this.position = columnDescription.position;
        this.name = columnDescription.name;
        this.category = columnDescription.category;
        this.type = columnDescription.type;
        this.defaultOutput = columnDescription.defaultOutput;
        this.internal = columnDescription.internal;
    }

    public ColumnDescription(int _position, String _name, int _category, int _type, String _defaultOutput, boolean _internal) {
        this.position = _position;
        this.name = _name.toUpperCase();
        this.category = _category;
        this.type = _type;
        this.defaultOutput = _defaultOutput;
        this.internal = _internal;
    }

    public int getPosition() {
        return this.position;
    }

    public String getName() {
        return this.name;
    }

    public String getDefaultOutput() {
        return this.defaultOutput;
    }

    public int getCategory() {
        return this.category;
    }

    public String getCategoryName() {
        if (this.category < 1 || this.category > 7) {
            return "";
        }
        return categories[this.category];
    }

    public int getType() {
        return this.type;
    }

    public String getTypeName() {
        if (this.type < 1 || this.type > 4) {
            return "";
        }
        return types[this.type];
    }

    public boolean isInternal() {
        return this.internal;
    }

    @Override
    public int compareTo(ColumnDescription that) {
        int BEFORE = -1;
        boolean EQUAL = false;
        boolean AFTER = true;
        if (this == that) {
            return 0;
        }
        if (this.position < that.position) {
            return -1;
        }
        return this.position > that.position;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.category;
        result = 31 * result + (this.defaultOutput == null ? 0 : this.defaultOutput.hashCode());
        result = 31 * result + (this.internal ? 1231 : 1237);
        result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
        result = 31 * result + this.position;
        result = 31 * result + this.type;
        return result;
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
        ColumnDescription other = (ColumnDescription)obj;
        if (this.category != other.category) {
            return false;
        }
        if (this.defaultOutput == null ? other.defaultOutput != null : !this.defaultOutput.equals(other.defaultOutput)) {
            return false;
        }
        if (this.internal != other.internal) {
            return false;
        }
        if (this.name == null ? other.name != null : !this.name.equals(other.name)) {
            return false;
        }
        if (this.position != other.position) {
            return false;
        }
        return this.type == other.type;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append('\t');
        sb.append(this.category);
        sb.append('\t');
        sb.append(this.type);
        if (this.defaultOutput != null) {
            sb.append('\t');
            sb.append(this.defaultOutput);
        }
        sb.append('\t');
        sb.append(this.internal);
        return sb.toString();
    }

    public static int getCategory(String categoryName) {
        if (categoryName.equals("INPUT")) {
            return 1;
        }
        if (categoryName.equals("HEAD")) {
            return 2;
        }
        if (categoryName.equals("OUTPUT")) {
            return 3;
        }
        if (categoryName.equals("DEPENDENCY_EDGE_LABEL")) {
            return 3;
        }
        if (categoryName.equals("IGNORE")) {
            return 7;
        }
        return -1;
    }

    public static int getType(String typeName) {
        if (typeName.equals("STRING")) {
            return 1;
        }
        if (typeName.equals("INTEGER")) {
            return 2;
        }
        if (typeName.equals("BOOLEAN")) {
            return 3;
        }
        if (typeName.equals("REAL")) {
            return 4;
        }
        if (typeName.equals("ECHO")) {
            return 2;
        }
        return -1;
    }
}

