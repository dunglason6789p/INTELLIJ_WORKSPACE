/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.transition;

public class Transition
implements Comparable<Transition> {
    private final int code;
    private final String symbol;
    private final boolean labeled;
    private final int cachedHash;

    public Transition(int code, String symbol, boolean labeled) {
        this.code = code;
        this.symbol = symbol;
        this.labeled = labeled;
        int prime = 31;
        int result = 31 + code;
        result = 31 * result + (labeled ? 1231 : 1237);
        this.cachedHash = 31 * result + (symbol == null ? 0 : symbol.hashCode());
    }

    public int getCode() {
        return this.code;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public boolean isLabeled() {
        return this.labeled;
    }

    @Override
    public int compareTo(Transition that) {
        int BEFORE = -1;
        boolean EQUAL = false;
        boolean AFTER = true;
        if (this.code < that.code) {
            return -1;
        }
        return this.code > that.code;
    }

    public int hashCode() {
        return this.cachedHash;
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
        Transition other = (Transition)obj;
        if (this.code != other.code) {
            return false;
        }
        if (this.labeled != other.labeled) {
            return false;
        }
        return !(this.symbol == null ? other.symbol != null : !this.symbol.equals(other.symbol));
    }

    public String toString() {
        return this.symbol + " [" + this.code + "] " + this.labeled;
    }
}

