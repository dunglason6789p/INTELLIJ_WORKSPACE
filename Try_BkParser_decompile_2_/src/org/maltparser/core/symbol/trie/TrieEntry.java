/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol.trie;

public class TrieEntry {
    private int code;

    public TrieEntry(int code, boolean known) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
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
        return this.code == ((TrieEntry)obj).code;
    }

    public int hashCode() {
        return 217 + this.code;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.code);
        return sb.toString();
    }
}

