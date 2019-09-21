/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.Table;
import org.maltparser.core.symbol.nullvalue.NullValues;

public interface SymbolTable
extends Table {
    public static final int NA = -1;
    public static final int INPUT = 1;
    public static final int OUTPUT = 3;
    public static final String[] categories = new String[]{"", "INPUT", "", "OUTPUT"};
    public static final int STRING = 1;
    public static final int INTEGER = 2;
    public static final int BOOLEAN = 3;
    public static final int REAL = 4;
    public static final String[] types = new String[]{"", "STRING", "INTEGER", "BOOLEAN", "REAL"};

    public void save(BufferedWriter var1) throws MaltChainedException;

    public void load(BufferedReader var1) throws MaltChainedException;

    public int getValueCounter();

    public int getNullValueCode(NullValues.NullValueId var1) throws MaltChainedException;

    public String getNullValueSymbol(NullValues.NullValueId var1) throws MaltChainedException;

    public boolean isNullValue(String var1) throws MaltChainedException;

    public boolean isNullValue(int var1) throws MaltChainedException;
}

